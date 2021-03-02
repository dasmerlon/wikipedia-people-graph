import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        if (0 == context.getCounter(redCounters.NUMKEYS).getValue()) {
            String taskId = context.getTaskAttemptID().getTaskID().toString();
            context.getCounter(redCounters.REDID).increment(Integer.parseInt(taskId.substring(taskId.length() - 6)));
            sayRedContextStuff(context);
        }

        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        result.set(sum);
        context.write(key, result);
    }

    public void sayRedContextStuff(Context context) throws IOException, InterruptedException {
        /* Used to see what can be seen from here.
         * Dumps pretty much everything I could see to the map.log file*/
        JobID jid = context.getJobID();

        Configuration cf = context.getConfiguration();

        Iterator<Map.Entry<String, String>> cfi = cf.iterator();
        int counter = 0;
        while (cfi.hasNext()) {
            Map.Entry<String, String> cfItem = cfi.next();
        }

        TaskAttemptID taid = context.getTaskAttemptID();
        TaskID tid = taid.getTaskID();
        Path wdir = context.getWorkingDirectory();
    }

    public enum redCounters {NUMKEYS, REDID}
}