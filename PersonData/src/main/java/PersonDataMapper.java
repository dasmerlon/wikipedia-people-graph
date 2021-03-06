import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Die Klasse PersonDataMapper erbt von der Mapper Klasse und definiert den Mapper für den
 * Map-Only Job. Der Input Value, Output Key und Output Value wird als Text-Objekt festgelegt.
 * Der Mapper bekommt einen ganzen Wikipediaartikel einer Person im XML Format, aus dem er
 * den Titel (Output Key) und bestimmte Personeninformationen (Output Value) extrahiert.
 */
public class PersonDataMapper extends Mapper<Object, Text, Text, Text> {

    private final Text name = new Text();
    private final Text infos = new Text();

    /**
     * Diese Methode beinhaltet die Logik eines Mappers. Aus dem Input value, der den Personenartikel enthält,
     * werden die Werte für den Output Key und Output Value ermittelt. Der Output Key ist dabei
     * immer der Titel des Artikels und der Output Value enthält eine Reihe von Personeninformationen.
     *
     * @param key     der Offset des Seitenanfangs innerhalb der Datei
     * @param value   der Inhalt der Seite als Text-Objekt
     * @param context der Kontext, der die Konfigurationen des Jobs enthält
     * @throws IOException          if Input or Output operations have failed or were interrupted
     * @throws InterruptedException if thread were interrupted, either before or during the activity
     */
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        //TODO: evtl. description mit Wikilink? https://en.wikipedia.org/wiki/name_name

        // Das String-Array personData enthält die Informationen einer Person, die wir aus der
        // Wikipedia-Infobox erhalten wollen. Das Leerzeichen am Ende schließt ungewollte Matches aus,
        // wie zb. "image1".
        String[] personData = {
                "image ",
                "name ",
                "birth_name ",
                "other_names ",
                "birth_date ",
                "birth_place ",
                "death_date ",
                "death_place ",
                "death_cause ",
                "nationality ",
                "education ",
                "occupation ",
                "known_for ",
                "order ",
                "office ",
                "term_start ",
                "term_end ",
                "party "
        };

        // Wir speichern den Input Value als String page ab. Dieser String wird bei Zeilenumbrüchen
        // gesplittet, damit wir über die Zeilen iterieren können. Eine ArrayList dient zum Sammeln aller
        // Informationen.
        String page = value.toString();
        String[] lines = page.split("\\r?\\n");
        ArrayList<String> infoList = new ArrayList<>();

        // Wir iterieren über alle Zeilen des Personenartikels und entfernen zunächst alle Leerzeichen am
        // Anfang und am Ende. Danach werden die Zeilen überprüft, ob sie den Titel, die Kurzbeschreibung
        // oder eine Personeninformation enthalten.
        for (String line : lines) {
            line = line.trim();

            // Der Titel von Personenartikeln enthält den Namen und gegebenenfalls eine eindeutige
            // Beschreibung. Wir prüfen, ob die Zeile den Titel enthält und übergeben diesen,
            // nachdem wir das XML aus der Zeile entfernt haben, zusammen mit einem eindeutigen
            // Ending Delimiter als Output Key.
            if (line.startsWith("<title>")) {
                String title = line.replace("<title>", "");
                title = title.replace("</title>", "");
                name.set(title + "<<<ENDTITLE<<<");
                continue;
            }

            // Die Kurzbeschreibung einer Person steht am Ende einer Zeile in zwei geschweiften
            // Klammern nach "short description" und einem Pipe-Symbol. Wir entfernen die geschweiften
            // Klammern am Ende und splitten die Zeile an dem Pipe-Symbol. Anschließend fügen wir
            // die Kurzbeschreibung zur ArrayList infoList hinzu.
            if (line.toLowerCase().contains("{{short description|")) {
                line = line.replace("}}", "");
                String[] shortDesc = line.split(Pattern.quote("|"));
                // Wenn die Zeile nicht gesplittet werden konnte, wird die Kurzbeschreibung ignoriert.
                if (shortDesc.length < 2) {
                    continue;
                }
                infoList.add("Short Description: " + shortDesc[1].trim());
                continue;
            }

            // Die Personeninformationen stehen nach einem Pipe-Symbol und enthalten ein Gleichheitszeichen.
            // Meistens beginnt die Zeile mit dem Pipe-Symbol, aber nicht immer. Zwischen dem Pipe-Symbol und
            // dem InfoKey ist manchmal ein Leerzeichen. Wir finden die Position des ersten Gleichheitszeichens
            // und speichern den Substring nach dem Gleich als InfoValue. Der InfoValue wird mithilfe der
            // Methode parseInfoValue bearbeitet und anschließend zusammen mit dem InfoKey zu der infoList
            // hinzugefügt.
            for (String infoKey : personData) {
                if ((line.contains("| " + infoKey) || line.contains("|" + infoKey)) && line.contains("=")) {
                    int position = line.indexOf("=");
                    String infoValue = line.substring(position + 1).trim();
                    infoValue = parseInfoValue(infoKey, infoValue);
                    if (infoValue == null) {
                        continue;
                    }
                    infoList.add(infoKey.trim() + ": " + infoValue);
                }
            }
        }

        // Die Elemente der ArrayList werden mit einem eindeutigen Delimiter voneinander abgegrenzt und zu
        // einem String zusammengefasst, welcher als Output Value übergeben wird. Der Mapper gibt anschließend
        // den Output Key name und den Output Value infos als Text-Objekt aus.
        String information = String.join(">>>NEXT>>>", infoList);
        infos.set(information);
        context.write(name, infos);
    }


    /**
     * Diese Methode erhält einen infoKey und den dazugehörigen infoValue. Sie parst, abhängig vom infoKey,
     * den infoValue in einer bestimmten Weise, damit unnötige Inhalte aus dem infoValue entfernt werden.
     *
     * @param infoKey   der infoKey, der die Art der Information beschreibt
     * @param infoValue der infoValue, der die eigentliche Information enthält
     * @return den geparsten infoValue
     */
    private String parseInfoValue(String infoKey, String infoValue) {
        if (infoValue == null || infoValue.isEmpty()) {
            return null;
        }

        // Je nach infoKey, führen wir verschiedene Operationen auf dem infoValue aus. Wenn es für den
        // infoKey keinen Case gibt, wird der infoValue unverändert wieder zurückgegeben.
        switch (infoKey) {
            case "image ":
                // Der infoValue enthält den Namen der Bilddatei. Wir ersetzen alle Leerzeichen durch
                // einen Underscore und hängen ihn an einen URL-Anfang. Die dadurch entstehende URL ist
                // die URL des Bildes.
                infoValue = infoValue.replaceAll("\\s", "_");
                return "https://commons.wikimedia.org/wiki/Special:FilePath/" + infoValue;

            case "birth_date ":
            case "death_date ":
                // Der infoValue enthält den Geburts- bzw. Sterbetag als Substring in Form von Year|Month|Day.
                // Wir extrahieren das Datum und geben es formatiert zurück.
                //TODO: das genaue Datum ist nicht immer bekannt ist, nur Jahr
                infoValue = extractSubstring(infoValue, "(\\d{1,4}\\|\\d{1,2}\\|\\d{1,2})");
                return dateFormatter(infoValue);

            case "term_start ":
            case "term_end ":
                return dateFormatter(infoValue);

            case "birth_place ":
                return infoValue;
            //TODO: weitere Fälle hinzufügen
        }
        return infoValue;
    }


    /**
     * Diese Methode extrahiert einen Substring aus einem gegebenen String, der durch einen gegebenen
     * Regex-Ausdruck definiert ist. Wenn es mehrere Substrings gibt, die durch den Regex-Ausdruck
     * beschrieben werden, wird nur der erste Substring zurückgegeben.
     *
     * @param text  der String, der den Substring enthält
     * @param regex der Regex-Ausdruck, der den Substring beschreibt
     * @return den extrahierten Substring
     */
    private String extractSubstring(String text, String regex) {
        // Wir kompilieren den Regex-Ausdruck zu einem Pattern und matchen diesen auf den String.
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        // Wenn das Pattern gefunden wurde, geben wir den Substring zurück. Die Methode find() findet
        // nur das erste Auftreten des Patterns. Alle danach werden ignoriert.
        if (!matcher.find()) {
            return null;
        }
        return text.substring(matcher.start(), matcher.end());
    }


    /**
     * Diese Methode formatiert Daten, die ein bestimmtes Format haben, in ein einheitliches Format.
     *
     * @param date das Datum, was formatiert werden soll
     * @return das formatierte Datum
     */
    private String dateFormatter(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }

        // Wir legen unser gewünschtes Datumsformat als formatter fest.
        SimpleDateFormat formatter = new SimpleDateFormat("y-MM-dd");
        // Die patternList enthält die Datumsformate, die das gegebene Datum haben kann.
        String[] patternList = {
                "d MMM y",
                "MMM d, y",
                "dd MMM y",
                "MMM dd, y",
                "y|M|d",
                "y|MM|d",
                "y|M|dd",
                "y|MM|dd"
        };

        // Wir versuchen für jedes Datumsformat das gegebene Datum zu parsen. Wenn das Datum geparst
        // werden konnte, wird es formatiert und zurückgegeben.
        for (String pattern : patternList) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern);
            try {
                Date parsedDate = parser.parse(date);
                return formatter.format(parsedDate);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}