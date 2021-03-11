import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.fs.FileSystem;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class RelationshipMapper extends Mapper<Object, Text, Text, Text> {

    private final Text relationship = new Text();
    private final Text none = new Text();
    private final HashSet<String> names = new HashSet<>();
    private boolean firstRun = true;


    /**
     *
     * @param key     der Offset des Seitenanfangs innerhalb der Datei
     * @param value   der Inhalt der Seite als Text-Objekt
     * @param context der Kontext, der die Konfigurationen des Jobs enthält
     * @throws IOException          if Input or Output operations have failed or were interrupted
     * @throws InterruptedException if thread were interrupted, either before or during the activity
     */
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        //if (names.isEmpty()) {
        if (firstRun) {
            URI[] uris = context.getCacheFiles();
            FileSystem fs = FileSystem.get(context.getConfiguration());
            Path path = new Path(uris[0].toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(path)));
            String name = reader.readLine();
            while (name != null) {
                names.add(name.trim());
                name = reader.readLine();
            }
            firstRun = false;
        }

        // Wir speichern den Input Value als String page ab. Dieser String wird bei Zeilenumbrüchen
        // gesplittet, damit wir über die Zeilen iterieren können. Eine ArrayList dient zum Sammeln aller
        // Informationen.
        String page = value.toString();
        String[] lines = page.split("\\r?\\n");

        String title = null;

        ArrayList<String> relationships = new ArrayList<>();

        /*
        InputStream input = RelationshipMapper.class.getResourceAsStream("/PersonNames.txt");
        if (input == null)
            return;
        String names = IOUtils.toString(input, StandardCharsets.UTF_8);
         */


        // Wir iterieren über alle Zeilen des Personenartikels und entfernen zunächst alle Leerzeichen am
        // Anfang und am Ende. Danach werden die Zeilen überprüft, ob sie den Titel, die Kurzbeschreibung
        // oder eine Personeninformation enthalten.
        for (String line : lines) {
            line = line.replaceAll("\\{\\{(.*?)\\}\\}"," ");
            line = line.trim();

            // Der Titel von Personenartikeln enthält den Namen und gegebenenfalls eine eindeutige
            // Beschreibung. Wir prüfen, ob die Zeile den Titel enthält und übergeben diesen,
            // nachdem wir das XML aus der Zeile entfernt haben, zusammen mit einem eindeutigen
            // Ending Delimiter als Output Key.
            if (line.startsWith("<title>")) {
                title = line.replace("<title>", "");
                title = title.replace("</title>", "");
                continue;
            }

            boolean hasBrackets = line.startsWith("{{") || line.startsWith("}}");
            boolean hasSymbols = line.startsWith("|") || line.startsWith("*") || line.startsWith("=");
            //TODO: poly, contains(alt=

            if (line.startsWith("<") || hasBrackets || hasSymbols || !line.contains("[[") || title == null) {
                continue;
            }

            String[] sentences = line.split("\\.\\s+(?=[A-Z])");
            for (String sentence : sentences) {
                Matcher linkMatcher = Pattern.compile("(?<=\\[\\[)(.*?)(?=\\]\\])").matcher(sentence);
                while (linkMatcher.find()) {
                    String link = sentence.substring(linkMatcher.start(), linkMatcher.end());
                    String person = link.split("\\|")[0];
                    if (names.contains(person) && !relationships.contains(person)) {
                        relationships.add(person);
                        relationship.set(title + ">>>>" + person + ">>>>" + sentence);
                        context.write(relationship, none);
                    }
                }
            }
        }
    }
}