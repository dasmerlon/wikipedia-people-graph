import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * PersonData erzeugt ein Map-Only Job, der aus Personenartikel von Wikipedia
 * den Artikelnamen und ausgewählte Informationen zu der Person ausgibt.
 * Die PersonData Klasse richtet die Konfiguration für den Map-Only Job ein,
 * damit dieser in Hadoop ausgeführt werden kann.
 */
public class PersonData {

    /**
     * In dieser Methode geben wir die Konfigurationen, den Jobnamen, den Datentyp von
     * dem Output Key und Output Value, den Datentyp der Ein- und Ausgabe und die Klassennamen des
     * Mappers und des Input-Formats an.
     */
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set("xmlInput.start", "<page>");
        conf.set("xmlInput.end", "</page>");

        Job job = Job.getInstance(conf, "personData");
        job.setJarByClass(PersonData.class);

        job.setMapperClass(PersonDataMapper.class);
        job.setInputFormatClass(XmlInputFormat.class);
        job.setNumReduceTasks(0);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
