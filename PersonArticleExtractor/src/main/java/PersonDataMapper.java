import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class PersonDataMapper extends Mapper<Object, Text, Text, Text> {


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

        String page = value.toString();
        String[] lines = page.split("\\r?\\n");
        String categoryRegex = "(.*)\\d{1,4}(\\sBC\\s|\\s)births]]";
        String referenceRegex = "(\\&lt\\;ref[\\s\\S]*?\\&lt\\;\\/ref\\&gt\\;)|(\\{\\{(sfn|efn)[\\s\\S]*?\\}\\})";
        String commentRegex = "(\\&lt\\;\\!--[\\s\\S]*?--\\&gt\\;)";

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("[[Category:") && line.matches(categoryRegex)) {
                page = page.replaceAll(referenceRegex+ "|" + commentRegex, "").trim();
                page = htmlDecoder(page);
                infos.set(page);
                context.write(name ,infos);
            }
        }

        // Uncomment this for the amount of lines for the current page
        //infos.set(String.valueOf(lines.length));
        //context.write(name, infos);
    }


    private String htmlDecoder(String page) {
        page = page.replace("&amp", "&");

        Map<String, Character> htmlMapping = new HashMap<>();
        htmlMapping.put("&lt;", '<');
        htmlMapping.put("&gt;", '>');
        htmlMapping.put("&quot;", '"');
        htmlMapping.put("&mdash;", '—');
        htmlMapping.put("&ensp;", ' ');
        htmlMapping.put("&emsp;", ' ');
        htmlMapping.put("&thinsp;", ' ');
        htmlMapping.put("&nbsp;", ' '); //" "
        htmlMapping.put("&ndash;", '-'); //-
        htmlMapping.put("&amp;", '&');

        for (Map.Entry<String, Character> entry : htmlMapping.entrySet()) {
            String hashKey = entry.getKey();
            Character hashValue = entry.getValue();
            String lol = Character.toString(hashValue);
            page = page.replace(hashKey, lol);
        }

        return page;
    }
}