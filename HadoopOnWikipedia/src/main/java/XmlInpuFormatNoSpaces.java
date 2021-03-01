package gallery; /* you'll need to change this to match your package name */

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Reads records that are delimited by a specifc begin/end tag.
 * Correctly handles case where xmlinput.start and xmlinput.end span
 * the boundary between inputSplits
 */
public class XmlInputFormatNoSpaces extends TextInputFormat {
    private static final transient Logger logger = Logger.getLogger("Map");

    public static final String START_TAG_KEY = "xmlinput.start";
    public static final String END_TAG_KEY   = "xmlinput.end";

    @Override
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit is,TaskAttemptContext tac) {
        /**/logger.setLevel(Level.DEBUG);
        return new XmlRecordReader();
    }

    public static class XmlRecordReader extends RecordReader<LongWritable, Text> {
        private byte[] startTag;
        private byte[] endTag;
        private long start;
        private long end;
        private FSDataInputStream fsin;
        private DataOutputBuffer buffer = new DataOutputBuffer();
        private LongWritable key = new LongWritable();
        private Text value = new Text();
        private boolean denyLeadingSpaces;
        private CompressionCodecFactory compressionCodecs = null;

        @Override
        public void initialize(InputSplit is, TaskAttemptContext tac) throws IOException, InterruptedException {
            FileSplit fileSplit = (FileSplit) is;
            startTag = tac.getConfiguration().get(START_TAG_KEY).getBytes("utf-8");
            endTag   = tac.getConfiguration().get(END_TAG_KEY  ).getBytes("utf-8");

            start = fileSplit.getStart();
            end = start + fileSplit.getLength();

//	        this.fileSplit = (FileSplit) split;
//	        this.conf = context.getConfiguration();
//
//	        final Path file = fileSplit.getPath();
//	        compressionCodecs = new CompressionCodecFactory(conf);
//
//	        final CompressionCodec codec = compressionCodecs.getCodec(file);
//	        System.out.println(codec);
//	        FileSystem fs = file.getFileSystem(conf);

            Path file = fileSplit.getPath();
            Configuration conf=tac.getConfiguration();
            compressionCodecs = new CompressionCodecFactory(conf);
            final CompressionCodec codec = compressionCodecs.getCodec(file);
            logger.debug("codec seen as "+codec);

            FileSystem fs = file.getFileSystem(conf);
            fsin = fs.open(fileSplit.getPath());
            fsin.seek(start);
            logger.debug("see first location of the start as "+fsin.getPos());
        }
        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            //logger.debug("XRR83^  "+fsin.getPos());
            if (fsin.getPos() < end) {
                if (readUntilMatch(startTag, false)) {
                    //logger.debug("XRR86^  "+fsin.getPos());
                    key.set(fsin.getPos()-startTag.length);
                    try {
                        buffer.write(startTag);
                        denyLeadingSpaces=true;
                        if (readUntilMatch(endTag, true)) {
                            value.set(buffer.getData(), 0, buffer.getLength());
                            //logger.debug("XRR93^key="+key.get()+" value="+value.toString());
                            return true;
                        } else if(0!=buffer.getLength()){
                            logger.error("false= readUntilMatch but buffer not 0length.  This will show only for xmlinput.start with no xmlinput.end tags");
                            value.set(buffer.getData(), 0, buffer.getLength());
                            //logger.debug("XRR98^K="+key.get()+" V="+value.toString());
                            return true;
                        }
                    } finally {
                        buffer.reset();
                    }
                }
            } else {
                logger.debug("at end position");
            }
            return false;
        }

        @Override
        public LongWritable getCurrentKey() throws IOException,
                InterruptedException {
            return key;
        }

        @Override
        public Text getCurrentValue() throws IOException, InterruptedException {
            return value;
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
            return (fsin.getPos() - start) / (float) (end - start);
        }

        @Override
        public void close() throws IOException {
            fsin.close();
        }

        private boolean readUntilMatch(byte[] match, boolean withinBlock) throws IOException {
            /* this is where the magic of the hadoop FileSystem class comes in... if this does not
             * see    match    within the current inputSplit, it will continue to fsin.read() bytes
             * into the next inputsplit.
             */
            //logger.debug("cme@ readUntillMatch starting at "+fsin.getPos());
            int i = 0;
            while (true) {
                int b = fsin.read();
                /* Used to look at a 64MB input slice off the decompressed wikipedia
                 *  dump (latest version of pages only ( the 9GB one)).  The logger section below
                 *  shows the two readUntilMatch behaviors.  First behavior is seen at the end of
                 *  mapper-the-first.  It has been called with match==xmlinput.end   It continues
                 *  reading past the file-split in log file                  !!! yourjobIdBelowHere         !!!
                 *  http://localhost.localdomain:50060/tasklog?attemptid=attempt_201310220813_0030_m_000000_0&all=true
                 *  The second behavior is when readUntilMatch is called with match==the xmlinput.start
                 *  at the beginning of the map-00001 (second input split).
                 *  http://localhost.localdomain:50060/tasklog?attemptid=attempt_201310220813_0030_m_000001_0&all=true
                 *  By reading up to the xmlinput.start it ignores the partial page "overRead" by mapper-just-prior
                 */
//				if(  (fsin.getPos()>=67108864) /* the beginning of split the second */
//					&&(fsin.getPos()<=67158079) /* for the version of wikipedia I grabbed, bendodiazapine started at byte 67042129 and ended at 67157331 (spanning the 0th and 1st input splits)*/
//				 ){
//					/**/logger.debug(String.format("%10d byte=%3d %c",fsin.getPos(),b,(char)b)); /* mapper log gets 1 line per inputfile byte */
//				}

                if (b == -1)return false; /* end of file */
                denyLeadingSpaces=((denyLeadingSpaces&&(32==b))||(10==b)||(13==b))?true:false;

                // save to buffer:
                if (withinBlock){
                    if(  (b!=10)
                            &&(b!=13)
                            &&(b!=32)
                            &&(b!= 9)
                    ){
                        buffer.write(b);
                    }
                }
                // check if we're matching:
                if (b == match[i]) {
                    i++;
                    if (i >= match.length)return true;
                } else
                    i = 0;

                // see if we've passed the stop point:
                if (!withinBlock && i == 0 && fsin.getPos() >= end)return false;
            }
        }
    }
}
