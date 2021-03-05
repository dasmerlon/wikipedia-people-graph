import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
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
        String[] persondata = {
                "image ",
                "name ",
                "birth_date ",
                "birth_place ",
                "death_date ",
                "death_place ",
                "death_cause ",
                "nationality ",
                "spouse ",
                "children ",
                "education ",
                "occupation ",
                "order ",
                "office ",
                "term_start ",
                "term_end ",
                "party "
        };

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
                line = line.replace("}}", "");
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
        String information = String.join(">>>NEXT>>>", infoList);
        infos.set(information);
        context.write(name, infos);
    }


    private String parseInfoValue(String infoKey, String infoValue) {
        if (infoValue == null || infoValue.isEmpty()) {
            return null;
        } else if (infoKey.equals("image ")) {
            infoValue = infoValue.replaceAll("\\s", "_");
            return "https://commons.wikimedia.org/wiki/Special:FilePath/" + infoValue;
        } else if (infoKey.equals("birth_date ") || infoKey.equals("death_date ")) {
            infoValue = extractSubstring(infoValue, "(\\d{1,4}\\|\\d{1,2}\\|\\d{1,2})");
            return dateFormatter(infoValue);
            /*
            String[] unit = infoValue.split("\\|");
            int year = Integer.parseInt(unit[0]);
            int month = Integer.parseInt(unit[1]);
            int day = Integer.parseInt(unit[2]);
            LocalDate date = LocalDate.of(year, month, day);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return date.format(formatter);
             */
        } else if (infoKey.equals("term_start ") || infoKey.equals("term_end ")) {
            return dateFormatter(infoValue);
        } else if (infoKey.equals("birth_place ")) {

            return infoValue;
        }
        return infoValue;
    }

    private String extractSubstring(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return text.substring(matcher.start(), matcher.end());
    }

    private String dateFormatter(String date) {
        if (date == null || date.isEmpty()) {
            return null;
        }
        // unser Output Dateformat
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

        for (String pattern : patternList) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern);
            try {
                Date parsedDate = parser.parse(date);
                return formatter.format(parsedDate);
            } catch (ParseException ignored) {}
        }
        return null;
    }


}