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
        // Wir iterieren über alle Zeilen der Wikiseite und entfernen zunächst alle Leerzeichen am
        // Anfang und am Ende. Danach werden die Zeilen überprüft, ob sie den Titel oder eine
        // Personeninformation enthalten.
        for (String line : lines) {
            line = line.trim();
            // Der Titel von Personenartikeln enthält den Namen und gegebenenfalls eine eindeutige
            // Beschreibung. Wir prüfen also, ob die Zeile den Titel enthält und übergeben diesen als
            // Output Key, nachdem wir das XML aus der Zeile entfernt haben.
            if (line.startsWith("<title>")) {
                String title = line.replace("<title>", "");
                title = title.replace("</title>", "");
                logger.info("title: " + title);
                name.set(title);
            }
            // Die Zeilen mit den Personeninformationen beginnen mit einem Pipesymbol. Daher prüfen wir,
            // ob die Zeile mit einem Pipesymbol beginnt. Wenn dies der Fall ist, entfernen wir das Symbol
            // und splitten die Zeile beim Gleichzeichen.
            else if (line.startsWith("|") && line.contains("=")) {
                line = line.replace("|", "");
                String[] info = line.split("=", 1);
                if (info.length < 2) {
                    continue;
                }
                String infoKey = info[0].trim();
                String infoValue = info[1].trim();
                infos.set(infoKey + ": " + infoValue);
                //context.write(name, infos);
                infoList.add(infoKey + ": " + infoValue);
            }
        }
        String information = String.join(", ", infoList);
        logger.info("information: " + information);
        infos.set(information);

        // Uncomment this for the amount of lines for the current page
        //infos.set(String.valueOf(lines.length));
        context.write(name, infos);
    }
}