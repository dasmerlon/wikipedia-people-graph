import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Die Klasse RelationshipMapper erbt von der Mapper Klasse und definiert den Mapper für den
 * Map-Only Job. Der Input Value, Output Key und Output Value wird als Text-Objekt festgelegt.
 * Der Mapper bekommt als Input einen ganzen Wikipediaartikel einer Person im XML Format, aus
 * dem er den Wikipediatitel der Person, den Titel einer im Artikel erwähnten Person und den Satz,
 * in dem die Person erwähnt wurde, als Output Key extrahiert. Der Output Value bleibt leer.
 * <p>
 * Der Output des Mappers ist wie folgt aufgebaut:
 * PERSON1>>>>PERSON2>>>>SENTENCE
 */
public class RelationshipMapper extends Mapper<Object, Text, Text, Text> {

    private final Text relationship = new Text();
    private final Text none = new Text();
    private final HashSet<String> names = new HashSet<>();

    /**
     * Diese Methode beinhaltet die Logik eines Mappers. Aus dem Input value, der den Personenartikel enthält,
     * werden die Werte für den Output Key ermittelt. Der Output Key enthält eine Reihe von Personeninformationen.
     *
     * @param key     der Offset des Seitenanfangs innerhalb der Datei
     * @param value   der Inhalt der Seite als Text-Objekt
     * @param context der Kontext, der die Konfigurationen des Jobs enthält
     * @throws IOException          if Input or Output operations have failed or were interrupted
     * @throws InterruptedException if thread were interrupted, either before or during the activity
     */
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

        // Wenn das HashSet noch nicht erstellt wurde, holt sich der Mapper die Textdatei mit den
        // Personennamen aus dem Cache, liest sie Zeile für Zeile ein und fügt den Namen zum HashSet hinzu.
        // Wenn das HashSet einmal erstellt wurde, kann es immer wieder verwendet werden.
        if (names.isEmpty()) {
            URI[] uris = context.getCacheFiles();
            FileSystem fs = FileSystem.get(context.getConfiguration());
            Path path = new Path(uris[0].toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(path)));
            String name = reader.readLine();
            while (name != null) {
                names.add(name.trim());
                name = reader.readLine();
            }
        }

        // Wir speichern den Input Value als String page ab. Dieser String wird bei Zeilenumbrüchen
        // gesplittet, damit wir über die Zeilen iterieren können.
        String page = value.toString();
        String[] lines = page.split("\\r?\\n");

        // Wir setzen zu Beginn den Titel auf null, um später abbrechen zu können, wenn kein Titel gefunden wurde.
        // Die ArrayList relationships dient zum Sammeln aller Personen, die bereits im Artikel erwähnt und daher
        // vom Mapper schon ausgegeben wurde.
        String title = null;
        ArrayList<String> relationships = new ArrayList<>();

        // Wir iterieren über alle Zeilen des Personenartikels und entfernen zunächst alle geschweiften Klammern
        // und ihren Inhalt, da diese für den Artikeltext nicht relevant sind. Anschließend entfernen wir alle
        // Leerzeichen am Anfang und am Ende.
        for (String line : lines) {
            line = line.replaceAll("\\{\\{(.*?)\\}\\}", " ");
            line = line.trim();

            // Es gibt Sandboxseiten, die wir herausfiltern wollen. Diese Seiten haben im Titel den Wikipedia-User
            // angegeben. Wenn es sich also um eine Sandboxseite handelt, bearbeiten wir diese Seite nicht weiter.
            if (line.startsWith("<title>") && line.toLowerCase().contains("user:")) {
                return;
            }

            // Der Titel von Personenartikeln enthält den Namen und gegebenenfalls eine eindeutige
            // Beschreibung. Wir prüfen, ob die Zeile den Titel enthält und setzen ihn als neuen title.
            if (line.startsWith("<title>")) {
                title = line.replace("<title>", "");
                title = title.replace("</title>", "");
                continue;
            }

            // Wir definieren einige Boolean-Ausdrücke für die folgende if-Abfrage.
            boolean hasBrackets = line.startsWith("{{") || line.startsWith("}}");
            boolean hasSymbols = line.startsWith("|") || line.startsWith("*") || line.startsWith("=");

            // Wir wollen nur die Zeilen bearbeiten, die auch den Artikeltext enthalten. Daher überprüfen wir,
            // ob die Zeile gewisse Zeichen enthält bzw. mit ihnen startet, die darauf schließen lassen, dass
            // es sich nicht um einen Artikeltext handelt. Da uns auch nur Verlinkungen interessieren, welche
            // innerhalb eckiger Klammern stehen, fangen wir hier auch schon Zeilen ab, die keine enthalten.
            // Wenn eines dieser Bedingungen zutrifft, überspringen wir die Zeile.
            if (!line.contains("[[")
                    || hasBrackets
                    || hasSymbols
                    || line.startsWith("<")
                    || title == null
                    || line.startsWith("poly")
                    || line.contains("alt=")
                    || line.contains(("File:"))) {
                continue;
            }

            extractRelationship(line, relationships, title, context);
        }
    }


    /**
     * Diese Methode ist dafür zuständig, dass der Mapper die Hauptperson, die erwähnte Person und den Satz
     * ausgibt. Dafür untersucht sie alle Verlinkungen einer gegebenen Zeile und prüft, ob es sich dabei um
     * eine Person mit eigenem Wikipediaartikel handelt. Wenn dies der Fall ist und die Person zudem zum ersten
     * Mal erwähnt wird, übergibt die Methode die Daten als Output Key aus.
     *
     * @param line          die Zeile, die eine Verlinkung (also eckige Klammern) enthält
     * @param relationships die ArrayList, die alle Personen enthält, zu denen es schon eine Beziehung gibt
     * @param title         der Wikipediatitel, bzw. der Name der ersten Person
     * @param context       der Kontext, der die Konfigurationen des Jobs enthält
     * @throws IOException          if Input or Output operations have failed or were interrupted
     * @throws InterruptedException if thread were interrupted, either before or during the activity
     */
    private void extractRelationship(String line, ArrayList<String> relationships, String title, Context context)
            throws IOException, InterruptedException {

        // Wir splitten die Zeile an Punkten, wenn nach ihnen ein Leerzeichen und ein Großbuchstabe folgt,
        // um einzelne Sätze zu bekommen.
        String[] sentences = line.split("\\.\\s+(?=[A-Z])");

        // Für jeden Satz suchen wir nach dem Inhalt zwischen zwei eckigen Klammern.
        for (String sentence : sentences) {
            Matcher linkMatcher = Pattern.compile("(?<=\\[\\[)(.*?)(?=\\]\\])").matcher(sentence);

            // Solange ein neuer Link-Inhalt gefunden wird, splitten wir an dem Pipe-Symbol und speichern
            // das erste Element.
            while (linkMatcher.find()) {
                String link = sentence.substring(linkMatcher.start(), linkMatcher.end());
                String person = link.split("\\|")[0];

                // Wir prüfen, ob das HashSet den Link-Inhalt enthält, d.h. der Link-Inhalt eine Person mit
                // eigenem Wikipediaartikel ist und ob diese Person noch nicht in der ArrayList relationships
                // enthalten ist. Wenn dies der Fall ist, fügen wir die Person zur ArrayList, damit erneute
                // Erwähnungen ignoriert werden. Anschließend bearbeiten wir den Satz, indem wir die
                // Inhalte zwischen geöffneten eckigen Klammern und einem Pipe-Symbol, eckige Klammern
                // und Apostrophe entfernen.
                // Zum Schluss gibt der Mapper den Titel, die erwähnte Person und den Satz als Output Key aus.
                if (names.contains(person) && !relationships.contains(person)) {
                    relationships.add(person);
                    String cleanSentence = sentence.replaceAll("(?<=\\[\\[)[^,\\]]+\\|", "");
                    cleanSentence = cleanSentence.replace("[[", "")
                            .replace("]]", "").replace("'", "");
                    relationship.set(title + ">>>>" + person + ">>>>" + cleanSentence + ".");
                    context.write(relationship, none);
                }
            }
        }
    }
}