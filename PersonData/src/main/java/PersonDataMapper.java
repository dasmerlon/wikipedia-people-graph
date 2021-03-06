import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//+ description? lies weiter: https://en.wikipedia.org/wiki/name_name
//birthname=name: delete birhtname

// Mapper <Input Key, Input Value, Output Key, Output Value>

/**
 * Die Klasse PersonDataMapper
 */
public class PersonDataMapper extends Mapper<Object, Text, Text, Text> {

    private final Text name = new Text();
    private final Text infos = new Text();

    /**
     * @param key     der Offset des Seitenanfangs innerhalb der Datei
     * @param value   der Inhalt der Seite als Text-Objekt
     * @param context der Kontext, der die Konfigurationen des Jobs enthält
     * @throws IOException if Input or Output operations have failed or were interrupted
     * @throws InterruptedException if thread were interrupted, either before or during the activity
     */
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

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

        ArrayList<String> infoList = new ArrayList<>();
        // Wir speichern den Input Value als String page ab. Dieser String wird bei Zeilenumbrüchen
        // gesplittet, damit wir über die Zeilen iterieren können.
        String page = value.toString();
        String[] lines = page.split("\\r?\\n");

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
        // den Output Key name und der Output Value infos aus.
        String information = String.join(">>>NEXT>>>", infoList);
        infos.set(information);
        context.write(name, infos);
    }


    /**
     * @param infoKey
     * @param infoValue
     * @return
     */
    private String parseInfoValue(String infoKey, String infoValue) {
        if (infoValue == null || infoValue.isEmpty()) {
            return null;
        }
        //
        switch (infoKey) {
            case "image ":
                //
                infoValue = infoValue.replaceAll("\\s", "_");
                return "https://commons.wikimedia.org/wiki/Special:FilePath/" + infoValue;
            case "birth_date ":
            case "death_date ":
                //
                infoValue = extractSubstring(infoValue, "(\\d{1,4}\\|\\d{1,2}\\|\\d{1,2})");
                return dateFormatter(infoValue);
            case "term_start ":
            case "term_end ":
                return dateFormatter(infoValue);
            case "birth_place ":
                return infoValue;
        }
        return infoValue;
    }

    /**
     * @param text
     * @param regex
     * @return
     */
    private String extractSubstring(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return text.substring(matcher.start(), matcher.end());
    }

    /**
     * @param date
     * @return
     */
    private String dateFormatter(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        // unser Output DateFormat
        SimpleDateFormat formatter = new SimpleDateFormat("y-MM-dd");
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
        //
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