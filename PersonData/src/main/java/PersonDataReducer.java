import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;

public class PersonDataReducer extends Reducer<Text, Text, Text, Text> {

    // Initialisierung von Output value
    private Text result = new Text();

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        ArrayList<String> allInfos = new ArrayList<String>();
        for (Text val : values) {
            String info = val.toString();
            allInfos.add(info);
        }

        String information = String.join(", " , allInfos);
        result.set(information);

        context.write(key, result);
    }
}