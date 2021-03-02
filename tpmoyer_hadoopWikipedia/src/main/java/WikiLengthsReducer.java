//package ; /* you'll need to change this to match your package name */

// import WikiLengthsMapper.mapCounters;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

@SuppressWarnings("unused")
public class WikiLengthsReducer
        extends Reducer<LongWritable, LongWritable, NullWritable, LongWritable> {
    private static final transient Logger logger = Logger.getLogger("App");

    @Override
    public void reduce(LongWritable key1, Iterable<LongWritable> value1s, Context context)
            throws IOException, InterruptedException {
        /**/
        logger.setLevel(Level.DEBUG);
        //logger.debug("red22^"+key1.get());

        if (0 == context.getCounter(redCounters.NUMKEYS).getValue()) {
            String taskId = context.getTaskAttemptID().getTaskID().toString();
            logger.debug(String.format("^fr37 setting REDID to %d from %s", Integer.parseInt(taskId.substring(taskId.length() - 6)), taskId));
            context.getCounter(redCounters.REDID).increment(Integer.parseInt(taskId.substring(taskId.length() - 6)));
            sayRedContextStuff(context);
        }
//		int counter=0;
//		for(LongWritable v:value1s){
//			counter++;
//		}
//		context.write(key1,new LongWritable(counter));

        for (LongWritable v : value1s) {
            //logger.debug(String.format("29^ %12d %10d", key1.get(),v.get()));
            context.write(NullWritable.get(), v);
        }
    }

    public void sayRedContextStuff(Context context) throws IOException, InterruptedException {
        logger.debug("cme@ sayRedContextStuff");
        /* Used to see what can be seen from here.
         * Dumps pretty much everything I could see to the map.log file*/
        JobID jid = context.getJobID();
        logger.debug("jobName=" + context.getJobName() + " jidIdentifier=" + jid.getJtIdentifier() + " jid.toString=" + jid.toString());
        logger.debug("jar=" + context.getJar());

        Configuration cf = context.getConfiguration();
        logger.debug("fs.default.name=" + cf.get("fs.default.name"));
        Iterator<Entry<String, String>> cfi = cf.iterator();
        int counter = 0;
        while (cfi.hasNext()) {
            Entry<String, String> cfItem = cfi.next();
            logger.debug(String.format("cfi %4d %-60s %-60s", counter++, cfItem.getKey(), cfItem.getValue()));
        }

        TaskAttemptID taid = context.getTaskAttemptID();
        TaskID tid = taid.getTaskID();
        logger.debug("TaskID=" + tid.toString());
        Path wdir = context.getWorkingDirectory();
        logger.debug("workingDirectory path=" + wdir);
        logger.debug("/*************************************************************************************************************/");
    }

    public enum redCounters {NUMKEYS, REDID}
}
