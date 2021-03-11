import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Die Klasse TitleMapper erbt von der Mapper Klasse und definiert den Mapper für den
 * Map-Only Job. Der Input Value, Output Key und Output Value wird als Text-Objekt festgelegt.
 * Der Mapper bekommt einen ganzen Wikipediaartikel im XML Format, aus dem er den Titel als
 * Output Key extrahiert. Der Output Value bleibt leer.
 */
public class TitleMapper extends Mapper<Object, Text, Text, Text> {

    private final Text pageTitle = new Text();
    private final Text empty = new Text();

    /**
     * Diese Methode beinhaltet die Logik eines Mappers. Aus dem Input value, der den Wikipediaartikel
     * enthält, wird der Titel für den Output Key ermittelt.
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

        // Wir iterieren über alle Zeilen des Personenartikels und entfernen zunächst alle Leerzeichen am
        // Anfang und am Ende. Danach werden die Zeilen überprüft, ob sie den Titel enthalten.
        for (String line : lines) {
            line = line.trim();

            // Wir prüfen, ob die Zeile den Titel enthält und setzen diesen als Output Key,
            // nachdem wir das XML aus der Zeile entfernt haben.
            if (line.startsWith("<title>")) {
                String title = line.replace("<title>", "");
                title = title.replace("</title>", "");
                pageTitle.set(title);
                continue;
            }
        }
        
        // Der Titel wird als Output Key übergeben. Der Output Value ist ein leeres Text-Objekt.
        context.write(pageTitle, empty);
    }
}