import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Die Klasse PersonArticleExtractor erzeugt ein Map-Only Job, der aus Wikipediaartikel die Personenartikel
 * herausfiltert und ausgibt. Der Map-Only Job erwartet einen Wikipedia-Dump als XML-Datei.
 * Der Output enthält ganze Personenartikel ohne Referenzen innerhalb des Artikels und ohne Userkommentare.
 * Zudem sind im Output einige Sonderzeichen, die als HTML-Codierung enthalten waren, durch ihre eigentlichen
 * Zeichen ersetzt.
 */
public class PersonArticleExtractor {

    /**
     * Die main Methode richtet die Konfiguration für den Map-Only Job ein, damit dieser in Hadoop
     * ausgeführt werden kann. Wir geben dafür die Konfigurationen, den Jobnamen, den Datentyp von
     * dem Output Key und Output Value, den Datentyp der Ein- und Ausgabe und die Klassennamen des
     * Mappers und des Input-Formats an. Außerdem legen wir hier fest, dass es keinen Reduce Job gibt.
     */
    public static void main(String[] args) throws Exception {

        // Wir setzen in der Konfiguration fest, dass der Mapper-Input mit <page> beginnt und </page> endet.
        // Somit bekommt der Mapper immer eine ganze Wikipediaseite als Input.
        Configuration conf = new Configuration();
        conf.set("xmlinput.start", "<page>");
        conf.set("xmlinput.end", "</page>");

        Job job = Job.getInstance(conf, "personArticle");
        job.setJarByClass(PersonArticleExtractor.class);

        job.setMapperClass(ArticleMapper.class);
        job.setInputFormatClass(XmlInputFormat.class);
        job.setNumReduceTasks(0);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
