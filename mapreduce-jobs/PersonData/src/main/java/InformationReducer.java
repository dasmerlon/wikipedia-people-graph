import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * "TITLE>>>>URL>>>>SHORT_DESCRIPTION>>>>IMAGE>>>>NAME>>>>BIRTH_NAME>>>>BIRTH_DATE>>>>BIRTH_PLACE>>>>
 * DEATH_DATE>>>>DEATH_PLACE>>>>DEATH_CAUSE>>>>NATIONALITY>>>>EDUCATION>>>>KNOWN_FOR>>>>OCCUPATION>>>>
 * ORDER>>>>OFFICE>>>>TERM_START>>>>TERM_END>>>>PARTY"
 */
public class InformationReducer extends Reducer<Text, Text, Text, Text> {

    private final Text result = new Text();
    private final Text none = new Text();

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        ArrayList<String> infoList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            infoList.add("NONE");
        }

        infoList.set(0, key.toString());

        Map<String, Integer> infoMapping = new HashMap<>();
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

        for (Text value : values) {
            String infoValue = value.toString();
            String[] infos = infoValue.split(Pattern.quote("**#**"));
            String infoKey = infos[0];

            for (Map.Entry<String, Integer> entry : infoMapping.entrySet()) {
                String hashKey = entry.getKey();
                int index = entry.getValue();
                if (infoKey.equals(hashKey) && infoList.get(index).equals("NONE")) {
                    //addToList(entry.getValue(), entry.getKey(), infos[1], infoList);
                    infoList.set(index, infos[1]);
                }
            }
        }


        // Die Elemente der ArrayList werden mit einem eindeutigen Delimiter voneinander abgegrenzt und zu
        // einem String zusammengefasst, welcher als Output Value übergeben wird. Der Mapper gibt anschließend
        // den Output Key name und den Output Value infos als Text-Objekt aus.
        result.set(String.join(">>>>", infoList));
        context.write(result, none);
    }

    private void addToList(int index, String infoKey, String infoValue, ArrayList<String> list) {

        ArrayList<String> keyList = new ArrayList<>();
        for (String element : list) {
            String[] subelement = element.split(Pattern.quote("**#**"));
            keyList.add(subelement[0]);
        }
        if (!keyList.contains(infoKey)) {
            list.set(index, infoValue);
        }
    }
}