import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Die Klasse PersonDataMapper erbt von der Mapper Klasse und definiert den Mapper für den
 * Map-Only Job. Der Input Value, Output Key und Output Value wird als Text-Objekt festgelegt.
 * Der Mapper bekommt als Input einen ganzen Wikipediaartikel einer Person im XML Format, aus
 * dem er bestimmte Personeninformationen als Output Key extrahiert. Der Output Value bleibt leer.
 * <p>
 * Der Output des Mappers ist wie folgt aufgebaut:
 * TITLE>>>>URL>>>>SHORT_DESCRIPTION>>>>IMAGE>>>>NAME>>>>BIRTH_NAME>>>>BIRTH_DATE>>>>BIRTH_PLACE>>>>DEATH_DATE>>>>
 * DEATH_PLACE>>>>DEATH_CAUSE>>>>NATIONALITY>>>>EDUCATION>>>>KNOWN_FOR>>>>OCCUPATION>>>>ORDER>>>>OFFICE>>>>
 * TERM_START>>>>TERM_END>>>>PARTY
 */
public class PersonDataMapper extends Mapper<Object, Text, Text, Text> {

    private final Text infos = new Text();
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

        // Das String-Array personData enthält die Informationen einer Person, die wir aus der
        // Wikipedia-Infobox erhalten wollen.
        String[] personData = {
                "image",
                "name",
                "birth_name",
                "birth_date",
                "birth_place",
                "death_date",
                "death_place",
                "death_cause",
                "nationality",
                "education",
                "occupation",
                "known_for",
                "order",
                "office",
                "term_start",
                "term_end",
                "party"
        };

        // Wir speichern den Input Value als String page ab. Dieser String wird bei Zeilenumbrüchen
        // gesplittet, damit wir über die Zeilen iterieren können.
        String page = value.toString();
        String[] lines = page.split("\\r?\\n");

        // Die ArrayList keyList dient dem Sammeln von Informationstypen, die für die Person gefunden wurden.
        // Die ArrayList infoList dient zum Sammeln aller Informationsinhalte. Zu Beginn wird jeder Inhalt auf
        // "NONE" gesetzt. Wenn später eine Information gefunden wird, wird der entsprechende Eintrag überschrieben.
        // Nicht gefundene Informationen behalten den Inhalt "NONE".
        ArrayList<String> keyList = new ArrayList<>();
        ArrayList<String> infoList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            infoList.add("NONE");
        }

        // Wir iterieren über alle Zeilen des Personenartikels und entfernen zunächst alle Leerzeichen am
        // Anfang und am Ende. Danach werden die Zeilen überprüft, ob sie den Titel, die Kurzbeschreibung
        // oder eine Personeninformation enthalten.
        for (String line : lines) {
            line = line.trim();

            // Es gibt Sandboxseiten, die wir herausfiltern wollen. Diese Seiten haben im Titel den Wikipedia-User
            // angegeben. Wenn es sich also um eine Sandboxseite handelt, bearbeiten wir diese Seite nicht weiter.
            if (line.startsWith("<title>") && line.toLowerCase().contains("user:")) {
                return;
            }

            // Der Titel von Personenartikeln enthält den Namen und gegebenenfalls eine eindeutige
            // Beschreibung. Wir prüfen, ob die Zeile den Titel enthält und fügen diesen,
            // nachdem wir das XML aus der Zeile entfernt haben, zur ArrayList infoList hinzu.
            if (line.startsWith("<title>")) {
                String title = line.replace("<title>", "");
                title = title.replace("</title>", "");
                addToList("title", title, infoList, keyList);

                // Wir ersetzen alle Leerzeichen des Titels durch Underscores und hängen ihn an einen URL-Anfang.
                // Die dadurch entstehende URL ist die URL des Wikipediaartikels. Diese fügen wir anschließend
                // auch zur infoList hinzu.
                String urlEnd = title.replaceAll("\\s", "_");
                addToList("url", "https://en.wikipedia.org/wiki/" + urlEnd.trim(), infoList, keyList);
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
                // Dies kann der Fall sein, wenn zb. nach dem Pipe-Symbol nichts steht.
                if (shortDesc.length < 2) {
                    continue;
                }

                // Bevor wir die Kurzbeschreibung zur infoList hinzufügen, prüfen wir, ob die Beschreibung noch
                // weitere geschweifte Klammern enthält und entfernen diese.
                String description = curvedBracketTest(shortDesc[1]);
                if (description == null || description.isEmpty()) {
                    continue;
                }
                addToList("short_description", description.trim(), infoList, keyList);
                continue;
            }

            // Die Art der Personeninformation steht zwischen einem Pipe-Symbol und einem Gleichheitszeichen.
            // Zwischen diesem InfoKey und dem Pipe-Symbol und dem InfoKey und dem Gleichheitszeichen
            // kann manchmal ein Leerzeichen sein. Wir müssen diese Fälle überprüfen, um ungewollte
            // Informationen auszuschließen, wie zb. "image1".
            for (String infoKey : personData) {
                Boolean hasSpaces = line.startsWith("| " + infoKey + " ");
                Boolean hasNoSpaces = line.startsWith("|" + infoKey + "=");
                Boolean hasStartSpace = line.startsWith("| " + infoKey + "=");
                Boolean hasEndSpace = line.startsWith("|" + infoKey + " ");

                // Wenn eine Zeile mit dem infoKey gefunden wurde, finden wird die Position des ersten
                // Gleichheitszeichens und speichern den Substring nach dem Gleich als InfoValue.
                // Der InfoValue wird mithilfe der Methode parseInfoValue bearbeitet. Danach wird der infoValue
                // noch auf weitere Klammern geprüft und anschließend zu der infoList hinzugefügt.
                if ((hasSpaces || hasNoSpaces || hasStartSpace || hasEndSpace) && line.contains("=")) {
                    int position = line.indexOf("=");
                    String infoValue = line.substring(position + 1).trim();
                    infoValue = parseInfoValue(infoKey, infoValue);
                    infoValue = curvedBracketTest(infoValue);
                    if (infoValue == null || infoValue.isEmpty()) {
                        continue;
                    }
                    addToList(infoKey.trim(), infoValue.trim(), infoList, keyList);
                }
            }
        }
        // Die Elemente der ArrayList werden mit einem eindeutigen Delimiter voneinander abgegrenzt und zu
        // einem String zusammengefasst, welcher als Output Key übergeben wird. Der Mapper gibt anschließend
        // den Output Key infos und den leeren Output Value als Text-Objekt aus.
        infos.set(String.join(">>>>", infoList));
        context.write(infos, none);
    }


    /**
     * Diese Methode ersetzt den entsprechenden Eintrag der ArrayList list mit dem infoValue, wenn diese
     * Information bisher noch nicht gefunden wurde. Der richtige Index der ArrayList wird durch den infoKey
     * bestimmt. Mit der keyList wird überprüft, ob der infoKey bereits schon gefunden wurde und dementsprechend
     * schon ein Eintrag in der ArrayList list steht, der nicht überschrieben werden soll. Wenn der Wikipediaartikel
     * also mehrere Einträge derselben Information enthält, übernehmen wir nur den ersten Eintrag und ignorieren
     * alle weiteren.
     *
     * @param infoKey   der infoKey, der die Art der Information beschreibt
     * @param infoValue der infoValue, der die eigentliche Information enthält
     * @param list      die ArrayList zu der der infoValue hinzugefügt werden soll
     * @param keyList   die ArrayList, die alle bisherigen infoKeys sammelt
     */
    private void addToList(String infoKey, String infoValue, ArrayList<String> list, ArrayList<String> keyList) {

        // Mit der HashMap legen wir die Reihenfolge der Informationen in der ArrayList fest.
        // Sie enthält als Key-Value-Paar den infoKey und den ArrayList-Index.
        Map<String, Integer> infoMapping = new HashMap<>();
        infoMapping.put("title", 0);
        infoMapping.put("url", 1);
        infoMapping.put("short_description", 2);
        infoMapping.put("image", 3);
        infoMapping.put("name", 4);
        infoMapping.put("birth_name", 5);
        infoMapping.put("birth_date", 6);
        infoMapping.put("birth_place", 7);
        infoMapping.put("death_date", 8);
        infoMapping.put("death_place", 9);
        infoMapping.put("death_cause", 10);
        infoMapping.put("nationality", 11);
        infoMapping.put("education", 12);
        infoMapping.put("known_for", 13);
        infoMapping.put("occupation", 14);
        infoMapping.put("order", 15);
        infoMapping.put("office", 16);
        infoMapping.put("term_start", 17);
        infoMapping.put("term_end", 18);
        infoMapping.put("party", 19);

        // Wenn der infoKey nicht in der ArrayList infoKey enthalten ist, also zum ersten mal auftaucht,
        // überschreiben wir den bisherigen Eintrag (NONE) an dem entsprechenden Index mit dem infoValue.
        // Anschließend fügen wir den infoKey zur keyList hinzu.
        if (!keyList.contains(infoKey)) {
            int index = infoMapping.get(infoKey);
            list.set(index, infoValue);
            keyList.add(infoKey);
        }
    }

    /**
     * Nach dem parsen des infoValues werden manchmal nicht alle geschweiften Klammern und ihr Inhalt entfernt.
     * Die geschweifte Klammern enthalten für uns irrelevante Informationen, weshalb wir diese entfernen wollen.
     * Diese Methode prüft, ob der infoValue noch geschweifte Klammern enthält. Wenn dies der Fall ist,
     * wird der Inhalt der Klammern inklusive der geschweiften Klammern selbst entfernt.
     *
     * @param infoValue der infoValue, der die Personeninformation enthält
     * @return den infoValue ohne geschweifte Klammern
     */
    private String curvedBracketTest(String infoValue) {
        if (infoValue == null || infoValue.isEmpty()) {
            return null;
        }

        // Solange der infoValue mindestens eine geöffnete und geschlossene Klammer enthält, werden diese und
        // der Text dazwischen gelöscht.
        while (infoValue.contains("{") && infoValue.contains("}")) {
            infoValue = infoValue.replaceAll("\\{(.*?)\\}", "");
        }

        // Beispielsweise beim splitten passiert es manchmal, dass der infoValue nur noch eine Hälfte
        // der Klammern enthält. Wenn der infoValue nur eine geöffnete Klammer enthält, splitten wir den infoValue
        // bei dieser Klammer und geben nur den ersten String des String-Arrays zurück, da dies der Text links
        // von der Klammer, also der Text außerhalb der Klammer ist. Analog dazu geben wir den zweiten String
        // des String-Arrays zurück, wenn der infoValue nur eine geschlossene Klammer enthält.
        if (infoValue.contains("{") && !infoValue.contains("}")) {
            String[] textChunks = infoValue.split(Pattern.quote("{"));
            if (textChunks.length == 0) {
                return null;
            }
            return textChunks[0];
        } else if (infoValue.contains("}") && !infoValue.contains("{")) {
            String[] textChunks = infoValue.split(Pattern.quote("}"));
            if (textChunks.length == 0) {
                return null;
            } else if (textChunks.length < 2) {
                // Wenn der infoValue nicht gesplittet werden konnte, geben wir den ersten String zurück.
                return textChunks[0];
            }
            return textChunks[1];
        }

        return infoValue;
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

        // Es existieren gewisse Elemente in den Wikipediaartikeln, bei denen die eigentlichen Informationen
        // erst in den nächsten Zeilen stehen. Dazu gehören zb. plainlist und flatlist.
        // Wir erstellen mit listNames eine Liste von solchen Begriffen.
        String[] listNames = {
                "plainlist",
                "flatlist",
                "ublist",
                "flat list",
                "plain list",
                "flagicon"
        };

        // Wenn ein infoValue, ein Begriff aus dem String-Array enthält, enthält er keine Informationen.
        // Daher setzen wir den infoValue auf null.
        for (String list : listNames) {
            if (infoValue.toLowerCase().contains(list)) {
                return null;
            }
        }

        // Wenn XML Code in dem infoValue enthalten ist, wird dieser entfernt.
        infoValue = infoValue.replaceAll("\\<(.*?)\\>", " ");

        // Eckige Klammern beschreiben in Wikipedia eine Verlinkung. Manchmal ist innerhalb der Verlinkung ein
        // Pipe-Symbol. Der Inhalt vor dem Pipe-Symbol ist der Wikipediatitel auf den verlinkt wird, der Inhalt
        // nach dem Pipe-Symbol ist der Text der angezeigt wird. Für den infoValue interessieren wir uns nur für
        // den den Inhalt nach dem Pipe-Symbol, weshalb wir den Inhalt zwischen geöffneten Klammern und einem
        // Pipe-Symbol entfernen. Anschließend entfernen wir die restlichen Klammern.
        infoValue = infoValue.replaceAll("\\[\\[[^,\\]]+\\|", "")
                .replace("[[", "").replace("]]", "");

        // Wir ersetzen, bzw. entfernen einige Zeichen, die im infoValue enthalten sein können.
        infoValue = infoValue.replace("{{circa}}", "ca.")
                .replace("{{thinsp}}", " ")
                .replaceAll("\\{\\{sup(.*?)\\}\\}", "");

        // Je nach infoKey, führen wir verschiedene Operationen auf dem infoValue aus.
        switch (infoKey) {
            case "image":
                // Der infoValue enthält den Namen der Bilddatei. Wir ersetzen alle Leerzeichen durch
                // Underscores und hängen ihn an einen URL-Anfang. Die dadurch entstehende URL ist
                // die URL des Bildes.
                infoValue = infoValue.replaceAll("\\s", "_");
                return "https://commons.wikimedia.org/wiki/Special:FilePath/" + infoValue;

            case "birth_date":
            case "death_date":
            case "term_start":
            case "term_end":
                // Der infoValue des Geburts- bzw. Sterbetags enthält das Datum als Substring in Form
                // von Year|Month|Day. Wir extrahieren dieses Datum und geben es formatiert zurück.
                // Daten die nicht dieses Format besitzen werden direkt formatiert.
                if (infoValue.contains("date") && infoValue.contains("|")) {
                    infoValue = extractSubstring(infoValue, "(\\d{1,4}\\|\\d{1,2}\\|\\d{1,2})");
                }
                return dateFormatter(infoValue);

            case "name":
            case "birth_name":
            case "nationality":
                // Der infoValue kann folgendes Format haben: {{lang|fr|INFORMATION|optional}}
                // Wir entfernen daher alles von den geöffneten Klammern bis zum zweiten Pipe-Symbol
                // und löschen die geschlossenen Klammern. Wir splitten das Ergebnis beim Pipe-Symbol und
                // geben das erste Element, was unsere Information ist, zurück.
                infoValue = infoValue.replaceAll("\\{\\{(.*?)\\|(.*?)\\|", " ");
                infoValue = infoValue.replace("}}", "");
                String[] names = infoValue.split(Pattern.quote("|"));
                if (names.length == 0) {
                    return null;
                }
                return names[0];

            case "office":
            case "order":
            case "party":
            case "death_cause":
            case "birth_place":
            case "death_place":
            case "known_for":
            case "occupation":
            case "education":
                // Der infoValue kann folgendes Format haben: {{something|INFO|INFO|INFO|etc.}}
                // Wir entfernen daher alles von den geöffneten Klammern bis zum ersten Pipe-Symbol und löschen
                // die geschweiften Klammern. Die Pipe-Symbole ersetzen wir durch Kommas.
                // Am Ende geben wir den infoValue eine Azurück, der nun eine Aufzählung enthält.
                infoValue = infoValue.replaceAll("\\{\\{(.*?)\\|", "");
                infoValue = infoValue.replace("}}", "")
                        .replace("{{", "")
                        .replace("|", ", ");
                return infoValue;
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

        // Wir legen unser gewünschtes Datenformat als formatter fest.
        SimpleDateFormat formatter = new SimpleDateFormat("G-y-MM-dd");
        // Die patternList enthält die Datenformate, die das gegebene Datum haben kann.
        String[] patternList = {
                "d MMM yyyy G",
                "d MMM G yyyy",
                "G d MMM yyyy",
                "d MMM yyyy",
                "MMM d, yyyy",
                "MMM d yyyy",
                "d.MM.yyyy",
                "yyyy|MM|d"
        };

        // Wir versuchen für jedes Datenformat das gegebene Datum zu parsen. Wenn das Datum geparst
        // werden konnte, wird es formatiert und zurückgegeben.
        for (String pattern : patternList) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern);
            try {
                Date parsedDate = parser.parse(date);
                return formatter.format(parsedDate);
            } catch (ParseException ignored) {
            }
        }

        // Wenn das Datum mit keinem der Datenformate geparst werden konnte, versuchen wir nur das Jahr
        // zu parsen, da manche Personen nur ein ungefähres Geburtsjahr haben.
        return yearFormatter(date);
    }


    /**
     * Diese Methode formatiert Daten, die nur ein Jahr enthalten, in ein einheitliches Format.
     *
     * @param date das Datum, was formatiert werden soll
     * @return das formatierte Datum
     */
    private String yearFormatter(String date) {
        // Wir extrahieren aus dem gegebenen Datum das Jahr und gegebenenfalls das Zeitalter (BC/AD).
        String year = extractSubstring(date, "(AD|BC)\\s\\d{1,4}|\\d{1,4}\\s(AD|BC)|\\d{1,4}");
        if (year == null || year.isEmpty()) {
            return null;
        }

        // Wir legen unser gewünschtes Datenformat als formatter fest.
        SimpleDateFormat formatter = new SimpleDateFormat("G-y");
        // Die patternList enthält die Datenformate, die das gegebene Datum haben kann.
        String[] patternList = {
                "yyyy G",
                "G yyyy",
                "yyyy"
        };

        // Wir versuchen für jedes Datenformat das gegebene Datum zu parsen. Wenn das Datum geparst
        // werden konnte, wird es formatiert und zurückgegeben.
        for (String pattern : patternList) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern);
            try {
                Date parsedDate = parser.parse(year);
                return formatter.format(parsedDate);
            } catch (ParseException ignored) {
            }
        }

        // Wenn das Datum mit keinem der Datenformate geparst werden konnte, geben wir null zurück.
        return null;
    }
}