import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 *
 */
public class Relationships {

    /**
     * Die main Methode richtet die Konfiguration für den Map-Only Job ein, damit dieser in Hadoop
     * ausgeführt werden kann. Wir geben dafür die Konfigurationen, den Jobnamen, den Datentyp von
     * dem Output Key und Output Value, den Datentyp der Ein- und Ausgabe und die Klassennamen des
     * Mappers und des Input-Formats an. Außerdem legen wir hier fest, dass es keinen Reduce Job gibt.
     */
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set("xmlInput.start", "<page>");
        conf.set("xmlInput.end", "</page>");

        Job job = Job.getInstance(conf, "relationships");
        job.setJarByClass(Relationships.class);

        job.addCacheFile(new Path("hdfs:///user/7mhoffma/wikipedia-people-graph/data/PersonNames.txt").toUri());

        job.setMapperClass(RelationshipMapper.class);
        //job.setReducerClass(InformationReducer.class);
        job.setInputFormatClass(XmlInputFormat.class);
        job.setNumReduceTasks(0);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
