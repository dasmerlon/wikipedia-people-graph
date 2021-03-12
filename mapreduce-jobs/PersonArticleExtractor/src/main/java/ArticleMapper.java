import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Die Klasse ArticleMapper erbt von der Mapper Klasse und definiert den Mapper für den
 * Map-Only Job. Der Input Value, Output Key und Output Value wird als Text-Objekt festgelegt.
 * Der Mapper bekommt als Input einen ganzen Wikipediaartikel im XML Format und gibt den Artikel
 * als Output Key zurück, wenn dieser ein Personenartikel ist. Der Output Value bleibt leer.
 * <p>
 * Der Output enthält ganze Personenartikel ohne Referenzen innerhalb des Artikels und ohne Userkommentare.
 * Zudem sind im Output einige Sonderzeichen, die als HTML-Codierung enthalten waren, durch ihre eigentlichen
 * Zeichen ersetzt.
 */
public class ArticleMapper extends Mapper<Object, Text, Text, Text> {

    private final Text personPage = new Text();
    private final Text none = new Text();

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

        // Wir speichern den Input Value als String page ab. Dieser String wird bei Zeilenumbrüchen
        // gesplittet, damit wir über die Zeilen iterieren können.
        String page = value.toString();
        String[] lines = page.split("\\r?\\n");

        // categoryRegex ist der Regex-Ausdruck mit dem wir Personenartikel identifizieren wollen.
        // Personen mit einem bekannten Geburtsjahr besitzen in ihrem Artikel die Kategorie "YYYY (BC) births".
        // Wir ignorieren Personen, die kein bekanntes Geburtsjahr haben.
        String categoryRegex = "(.*)\\d{1,4}(\\sBC\\s|\\s)births]]";

        // Die Regex-Ausdrücke referenceRegex und commentRegex beschreiben die Referenzen und Userkommentare, welche
        // wir aus den Personenartikeln rausfiltern wollen, da diese für uns uninteressant sind.
        String referenceRegex = "(\\&lt\\;ref[\\s\\S]*?\\&lt\\;\\/ref\\&gt\\;)|(\\{\\{(sfn|efn|ref)[\\s\\S]*?\\}\\})";
        String commentRegex = "(\\&lt\\;\\!--[\\s\\S]*?--\\&gt\\;)";

        // Wir iterieren über alle Zeilen des Wikipediaartikels und entfernen zunächst alle Leerzeichen am
        // Anfang und am Ende.
        for (String line : lines) {
            line = line.trim();

            // Wir überprüfen, ob die Zeile die births-Kategorie enthält, welche Aufschluss darüber gibt,
            // dass es sich um einen Personenartikel handelt. Wenn es ein Personenartikel ist, entfernen
            // wir alle Referenzen und Userkommentare aus dem Artikel, decodieren gewissen HTML-Code und
            // geben anschließend den Personenartikel als Output Key aus.
            if (line.startsWith("[[Category:") && line.matches(categoryRegex)) {
                page = page.replaceAll(referenceRegex + "|" + commentRegex, "").trim();
                page = htmlDecoder(page);
                personPage.set(page);
                context.write(personPage, none);
            }
        }
    }


    /**
     * Diese Methode ersetzt gewisse Sonderzeichen, die als HTML-Codierung in der page enthalten sind,
     * durch ihre eigentlichen Zeichen.
     *
     * @param page der übergebene Personenartikel
     * @return den Personenartikel mit decodiertem HTML-Code
     */
    private String htmlDecoder(String page) {

        // Zu Beginn ersetzen wir den HTML-Code für das &-Zeichen, da die Wikipediaartikel teilweise
        // doppelt codiert sind und das &-Zeichen im HTML-Code verwendet wird. Beispielsweise findet sich
        // in den Wikipediaartikeln "&amp;nbsp;", was nach einmaliger Decodierung in "&nbsp;" resultiert.
        page = page.replace("&amp;", "&");

        // In der HashMap legen wir alle relevanten Sonderzeichen zusammen mit ihrer HTML-Codierung fest.
        Map<String, Character> htmlMapping = new HashMap<>();
        htmlMapping.put("&amp;", '&');
        htmlMapping.put("&lt;", '<');
        htmlMapping.put("&gt;", '>');
        htmlMapping.put("&quot;", '"');
        htmlMapping.put("&mdash;", '—');
        htmlMapping.put("&ensp;", ' ');
        htmlMapping.put("&emsp;", ' ');
        htmlMapping.put("&thinsp;", ' ');
        htmlMapping.put("&nbsp;", ' ');
        htmlMapping.put("&ndash;", '-');

        // Für jedes Key-Value-Paar aus der HashMap ersetzen wir in dem Personenartikel den HashKey
        // mit dem HashValue.
        for (Map.Entry<String, Character> entry : htmlMapping.entrySet()) {
            String hashKey = entry.getKey();
            Character hashValue = entry.getValue();
            String newChar = Character.toString(hashValue);
            page = page.replace(hashKey, newChar);
        }

        return page;
    }
}