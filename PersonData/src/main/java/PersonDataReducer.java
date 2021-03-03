import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class PersonDataReducer extends Reducer<Text, Text, Text, Text> {

    // Initialisierung von Output value
    private Text result = new Text();

    public void reduce(Text key, Text values, Context context) throws IOException, InterruptedException {
        if (0 == context.getCounter(redCounters.NUMKEYS).getValue()) {
            String taskId = context.getTaskAttemptID().getTaskID().toString();
            context.getCounter(redCounters.REDID).increment(Integer.parseInt(taskId.substring(taskId.length() - 6)));
        }
/*
        ArrayList<String> allInfos = new ArrayList<String>();
        for (Text val : values) {
            String info = val.toString();
            allInfos.add(info);
        }

        String information = String.join("\n" , allInfos);
        result.set(information);

 */
        context.write(key, values);
    }



    public enum redCounters {NUMKEYS, REDID}
}