package gallery; /* you'll need to change this to match your package name */

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

@SuppressWarnings("unused")
public class WikiLengths {
    /* this is intended as a beginners task... read the wikipedia dump file (43GB non zipped) and output the feeder
     * a histogram of number of pages vs number of bytes/page   (each wikipedia article is one <page> </page>)
     */
    private static final transient Logger logger = Logger.getLogger("app");

    public static void runJob(String input, String output) throws IOException {
        logger.debug("cme@ runJob with input="+input+"  output="+output);
        Configuration conf = new Configuration();
        conf.set("xmlinput.start", "<page>");
        conf.set("xmlinput.end", "</page>");
        conf.set(
                "io.serializations",
                "org.apache.hadoop.io.serializer.JavaSerialization,org.apache.hadoop.io.serializer.WritableSerialization"
        );

        /* these two lines enable bzip output from the reducer */
        //conf.setBoolean("mapred.output.compress", true);
        //conf.setClass  ("mapred.output.compression.codec", BZip2Codec.class,CompressionCodec.class);
        SimpleDateFormat ymdhms=new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss"); /* ISO 8601 format */
        Job job = new Job(conf, "wikPageLengths "+ymdhms.format(new Date()));

        FileInputFormat.setInputPaths(job, input);
        job.setJarByClass(WikiLengths.class);

        job.setMapperClass  (WikiLengthsMapper.class);
        /*job.setCombinerClass(WikiLengthsReducer.class); This is how to get a mapper which never completes... specify a reducer with different outputs from it's inputs as a combiner */
        job.setReducerClass (WikiLengthsReducer.class);
        //job.setNumReduceTasks(0);

        job.setInputFormatClass(XmlInputFormat.class);

        job.setMapOutputKeyClass(LongWritable.class);
//		job.setMapOutputValueClass(LongWritable.class); /* not necessary because reducer outputValueClass matches */


        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(LongWritable.class);

        Path outPath = new Path(output);
        FileOutputFormat.setOutputPath(job, outPath);
        FileSystem dfs = FileSystem.get(outPath.toUri(), conf);
        if (dfs.exists(outPath)) {
            dfs.delete(outPath, true);
        }

        try {
            job.waitForCompletion(true);
        } catch (InterruptedException ex) {
            //Logger.getLogger(WikiSee2.class.getName()).log(Level.SEVERE, null, ex);
            logger.fatal("InterruptedException "+ WikiLengths.class.getName()+" "+ex);
        } catch (ClassNotFoundException ex) {
            logger.fatal("ClassNotFoundException "+ WikiLengths.class.getName()+" "+ex);
        }

    }
    public static void main(String[] args) {
        logger.debug("cme@ main");
        try {
            runJob(args[0], args[1]);
        } catch (IOException ex) {
            logger.fatal("IOException "+ WikiLengths.class.getName()+" "+ex);
        }
    }
}
