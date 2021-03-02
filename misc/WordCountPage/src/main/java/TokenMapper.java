import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.util.StringTokenizer;

// Mapper <Input Key, Input Value, Output Key, Output Value>
public class TokenMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        if (0 == (context.getCounter(mapCounters.NUMPAGES)).getValue()) {
            /* will use the inputSplit as the high order portion of the output key. */
            FileSplit fileSplit = (FileSplit) context.getInputSplit();
            Configuration cf = context.getConfiguration();
            long blockSize = Integer.parseInt(cf.get("dfs.blocksize"));
            context.getCounter(mapCounters.MAPID).increment(fileSplit.getStart() / blockSize);/* the base of this increment is 0 */
        }

        StringTokenizer itr = new StringTokenizer(value.toString());
        while (itr.hasMoreTokens()) {
            word.set(itr.nextToken());
            context.write(word, one);
        }

        context.getCounter(mapCounters.NUMPAGES).increment(1);
    }

    public enum mapCounters {NUMPAGES, MAPID}
}