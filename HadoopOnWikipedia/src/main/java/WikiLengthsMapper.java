package gallery; /* you'll need to change this to match your package name */

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@SuppressWarnings("unused")
public class WikiLengthsMapper extends Mapper<LongWritable, Text, LongWritable, LongWritable> {
    /* output key is InputSplit catenated with _  catenated with the page number
     * output value is all the lines between <page> and </page> catenated into one line.
     * This value has had all  cr's and lf's and leading spaces purged.
     */
    private static final transient Logger logger = Logger.getLogger("Map");

    public static enum mapCounters{NUMPAGES,MAPID}

    @Override
    public void map(LongWritable key0, Text value0, Context context) throws IOException, InterruptedException {
        /**/logger.setLevel(Level.DEBUG);

        if(0==(context.getCounter(mapCounters.NUMPAGES)).getValue()){
            /* will use the inputSplit as the high order portion of the output key. */
            FileSplit fileSplit = (FileSplit) context.getInputSplit();
            Configuration cf = context.getConfiguration();
            long blockSize=Integer.parseInt(cf.get("dfs.blocksize"));
            context.getCounter(mapCounters.MAPID).increment(fileSplit.getStart()/blockSize);/* the base of this increment is 0 */
            logger.debug("MAPID set as "+context.getCounter(mapCounters.MAPID).getValue()+" from FileSplit.start="+fileSplit.getStart()+" and blockSize="+blockSize);
        }
        //logger.debug("mpm23^K="+key0.get());
        //logger.debug("wlm44^K="+key0.get()+" len="+value0.getLength()+" len2="+value0.toString().length()+" V="+value0.toString());

//		context.write(
//			 new LongWritable(value0.getLength())
//			,new LongWritable(1)
//		);
        context.write(key0,new LongWritable(value0.getLength()));

        /* dump the first 3 key value pairs for each inputSplit into the mapper.log file */
        if(0==context.getCounter(mapCounters.NUMPAGES).getValue()){
            logger.debug("                    infile               ");
            logger.debug("                     byte                 ");
            logger.debug("             length offset  mapper   page");
        }
        if(3>context.getCounter(mapCounters.NUMPAGES).getValue()){
            logger.debug(String.format(
                    "mpm60^ %5d %12d %4d %10d %s"
                    ,value0.getLength()
                    ,key0.get()
                    ,context.getCounter(mapCounters.MAPID).getValue()+((0<key0.get()?0:1))
                    ,(0<key0.get()?context.getCounter(mapCounters.NUMPAGES).getValue():0)
                    ,value0
                    )
            );
        }

        context.getCounter(mapCounters.NUMPAGES).increment(1);
    }
}