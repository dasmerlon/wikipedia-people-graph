import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Die Klasse Relationships erzeugt ein Map-Only Job, der aus Personenartikel von Wikipedia den Artikelnamen
 * und alle im Artikel erwähnten Personen zusammen mit dem Satz, in dem sie erwähnt werden, ausgibt.
 * Dabei werden nur Personen beachtet, die auch selbst einen Wikipediaartikel haben.
 * Der Map-Only Job erwartet einen Wikipedia-Dump als XML-Datei, der nur Personenartikel enthält. Dafür kann
 * das Projekt PersonArticleExtractor verwendet werden.
 * <p>
 * Der Output enthält in jeder Zeile den Wikipediatitel der ersten Person, einen weiteren Wikipediatitel einer
 * im Artikel erwähnten Person und den Satz, in dem die zweite Person erwähnt wurde.
 * Diese Daten werden durch den Delimiter >>>> voneinander getrennt.
 * Eine Zeile ist wie folgt aufgebaut:
 * <p>
 * PERSON1>>>>PERSON2>>>>SENTENCE
 */
public class Relationships {

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
        conf.set("xmlInput.start", "<page>");
        conf.set("xmlInput.end", "</page>");

        Job job = Job.getInstance(conf, "relationships");
        job.setJarByClass(Relationships.class);

        // Der Mapper verwendet die Textdatei mit den Personennamen mithilfe von Hadoop DistributedCache.
        // Wir geben hier den Pfad der Datei an, die zwischengespeichert werden soll.
        job.addCacheFile(new Path("hdfs:///user/7mhoffma/wikipedia-people-graph/data/PersonNames.txt").toUri());

        job.setMapperClass(RelationshipMapper.class);
        job.setInputFormatClass(XmlInputFormat.class);
        job.setNumReduceTasks(0);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
