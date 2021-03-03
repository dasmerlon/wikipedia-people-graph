import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class PersonDataMapper extends Mapper<Object, Text, Text, Text> {

    private static final transient Logger logger = Logger.getLogger("Map");

    // Wir initialisieren den Output Key name und Output Value infos als leere Textobjekte.
    private Text name = new Text();
    private Text infos = new Text();

    /**
     *
     * @param key Erstmal irrelevant
     * @param value Das XML der Page als Hadoops Text Class
     * @param context Kontexte im Kontext Hadoops
     * @throws IOException
     * @throws InterruptedException
     */
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        logger.setLevel(Level.DEBUG);
        logger.debug("penis");

        if (0 == (context.getCounter(mapCounters.NUMPAGES)).getValue()) {
            /* will use the inputSplit as the high order portion of the output key. */
            FileSplit fileSplit = (FileSplit) context.getInputSplit();
            Configuration cf = context.getConfiguration();
            long blockSize = Integer.parseInt(cf.get("dfs.blocksize"));
            context.getCounter(mapCounters.MAPID).increment(fileSplit.getStart() / blockSize);/* the base of this increment is 0 */
        }

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
                logger.debug("title: " + title);
                name.set(title);
            }
            // Die Zeilen mit den Personeninformationen beginnen mit einem Pipesymbol. Daher prüfen wir,
            // ob die Zeile mit einem Pipesymbol beginnt. Wenn dies der Fall ist, entfernen wir das Symbol
            // und splitten die Zeile beim Gleichzeichen.
            else if (line.startsWith("|") && line.contains("=")) {
                line = line.replace("|", "");
                String[] info = line.split("=", 1);
                String infoKey = info[0].trim();
                String infoValue = info[1].trim();
                infoList.add(infoKey + ": " + infoValue);
            }
        }
        String information = String.join(", ", infoList);
        logger.debug("information: " + information);
        infos.set(information);



/*
        int start = value.find("<title>", 0);
        int end = value.find("</title>", start);
        int advance = "<title>".length();

        try {
            String title = Text.decode(value.getBytes(), start+advance, end-start-advance);
            name.set(title);
        } catch (IOException e) {
            System.out.println("IOException was " + e.getMessage());
            return;
        }

 */

        context.write(name, infos);


        context.getCounter(mapCounters.NUMPAGES).increment(1);
    }

    public enum mapCounters {NUMPAGES, MAPID}
}