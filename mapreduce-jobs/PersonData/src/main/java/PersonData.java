import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Die Klasse PersonData erzeugt ein Map-Only Job, der aus Personenartikel von Wikipedia den Artikelnamen
 * und ausgewählte Informationen zu der Person ausgibt. Der Map-Only Job erwartet einen Wikipedia-Dump als
 * XML-Datei, der nur Personenartikel enthält. Dafür kann das Projekt PersonArticleExtractor verwendet werden.
 * <p>
 * Der Output enthält in jeder Zeile die Informationen zu einer Person. Dabei werden alle Daten nacheinander
 * aufgeführt und durch den Delimiter ">>>>" voneinander getrennt. Jede Zeile enthält die gleichen Informationen
 * in derselben Reihenfolge. Wenn eine Person zu einer Kategorie keine Daten enthält, steht an der entsprechenden
 * Stelle "NONE". Eine Zeile ist wie folgt aufgebaut:
 * <p>
 * TITLE>>>>URL>>>>SHORT_DESCRIPTION>>>>IMAGE>>>>NAME>>>>BIRTH_NAME>>>>BIRTH_DATE>>>>BIRTH_PLACE>>>>DEATH_DATE>>>>
 * DEATH_PLACE>>>>DEATH_CAUSE>>>>NATIONALITY>>>>EDUCATION>>>>KNOWN_FOR>>>>OCCUPATION>>>>ORDER>>>>OFFICE>>>>
 * TERM_START>>>>TERM_END>>>>PARTY
 */
public class PersonData {

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
