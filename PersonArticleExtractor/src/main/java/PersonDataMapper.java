import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class PersonDataMapper extends Mapper<Object, Text, Text, Text> {

    private static final Logger logger = Logger.getLogger(PersonDataMapper.class);

    // Wir initialisieren den Output Key name und Output Value infos als leere Textobjekte.
    private Text name = new Text();
    private Text infos = new Text();

    /**
     * @param key     Erstmal irrelevant
     * @param value   Das XML der Page als Hadoops Text Class
     * @param context Kontexte im Kontext Hadoops
     * @throws IOException
     * @throws InterruptedException
     */
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        logger.setLevel(Level.WARN);
        logger.info("starte hier");


        ArrayList<String> infoList = new ArrayList<>();
        // Wir speichern den Input Value als String page ab. Dieser String wird bei Zeilenumbrüchen
        // gesplittet, damit wir später über die Zeilen iterieren können.
        String page = value.toString();
        String[] lines = page.split("\\r?\\n");
        String categoryRegex = "(.*)\\d{1,4}\\sbirths]]";
        String referenceRegex = "(\\&lt\\;ref[\\s\\S]*?\\&lt\\;\\/ref\\&gt\\;)|(\\{\\{sfn[\\s\\S]*?\\}\\})";
        String commentRegex = "(\\&lt\\;\\!--[\\s\\S]*?--\\&gt\\;)";
        String filterRegex = referenceRegex+ "|" + commentRegex;


        // Wir iterieren über alle Zeilen der Wikiseite und entfernen zunächst alle Leerzeichen am
        // Anfang und am Ende. Danach werden die Zeilen überprüft, ob sie den Titel oder eine
        // Personeninformation enthalten.
        for (String line : lines) {
            line = line.trim();
            // Der Titel von Personenartikeln enthält den Namen und gegebenenfalls eine eindeutige
            // Beschreibung. Wir prüfen also, ob die Zeile den Titel enthält und übergeben diesen als
            // Output Key, nachdem wir das XML aus der Zeile entfernt haben.

            //if (line.startsWith("[[Category:") && line.endsWith("births]]")) {

            if (line.startsWith("[[Category:") && line.matches(categoryRegex)) {
                page = page.replaceAll(filterRegex, "").trim();
                infos.set(page);
                context.write(name ,infos);
            }

        }
        return;

        // Uncomment this for the amount of lines for the current page
        //infos.set(String.valueOf(lines.length));
        //context.write(name, infos);
    }
}