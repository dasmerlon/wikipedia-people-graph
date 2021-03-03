import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private final IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        if (0 == context.getCounter(redCounters.NUMKEYS).getValue()) {
            String taskId = context.getTaskAttemptID().getTaskID().toString();
            context.getCounter(redCounters.REDID).increment(Integer.parseInt(taskId.substring(taskId.length() - 6)));
        }

        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        result.set(sum);
        context.write(key, result);
    }


    public enum redCounters {NUMKEYS, REDID}
}