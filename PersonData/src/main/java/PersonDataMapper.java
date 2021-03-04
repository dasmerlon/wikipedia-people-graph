import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class PersonDataMapper extends Mapper<Object, Text, Text, Text> {

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

        //+ description? lies weiter: https://en.wikipedia.org/wiki/name_name
        ArrayList<String> persondata = new ArrayList<>(
                Arrays.asList("name",
                        "birth_date",
                        "birth_place",
                        "death_date",
                        "death_place",
                        "death_cause",
                        "nationality",
                        "spouse",
                        "children",
                        "education",
                        "occupation",
                        "order",
                        "office",
                        "term_start",
                        "term_end",
                        "party"
                        ));

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
                name.set(title + "<<<ENDTITLE<<<");
                continue;
            }

            //Short description
            if (line.toLowerCase().contains("{{short description|")) {
                line = line.replace("}}","");
                String[] shortDesc = line.split(Pattern.quote("|"));
                if (shortDesc.length < 2) {
                    continue;
                }
                //if (shortDesc[1].isEmpty()) {
                //    shortDesc[1] = shortDesc[1] + "None";
                //}
                String shortDescription = shortDesc[1].trim();
                infoList.add("Short Description: " + shortDescription);
                continue;
            }

            // Die Zeilen mit den Personeninformationen beginnen mit einem Pipesymbol. Daher prüfen wir,
            // ob die Zeile mit einem Pipesymbol beginnt. Wenn dies der Fall ist, entfernen wir das Symbol
            // und splitten die Zeile beim Gleichzeichen.
            for (String infoKey : persondata) {
                if (line.contains("| " + infoKey) && line.contains("=")) {
                    int position = line.indexOf("=");
                    String infoValue = line.substring(position+1).trim();
                    infoValue = parseInfoValue(infoKey, infoValue);
                    if (infoValue == null) {
                        continue;
                    }
                    infoList.add(infoKey + ": " + infoValue);
                }
            }

        }
        String information = String.join(">>>NEXT>>>", infoList);
        infos.set(information);
        context.write(name, infos);
    }


    private String parseInfoValue(String infoKey, String infoValue) {
        if (infoValue.isEmpty()) {
            return null;
        }
        if (infoKey.equals("birth_date")) {
            return infoValue;
        } else if (infoKey.equals("death_date")) {
            return infoValue;
        }
        return infoValue;
    }


}