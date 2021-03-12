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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Dies ist eine gekürzte Version des XmlInputFormats von Thomas P. Moyer:
 * https://tpmoyer-gallery.appspot.com/hadoopWikipedia
 * Wir benutzen dieses InputFormat, damit der Mapper immer einen kompletten und unveränderten
 * Wikipediaartikel übergeben bekommt. In der Klasse Relationships legen wir den Start- und Endtag fest.
 * <p>
 * Reads records that are delimited by a specific begin/end tag.
 * Correctly handles case where xmlInput.start and xmlInput.end span
 * the boundary between inputSplits
 */
public class XmlInputFormat extends TextInputFormat {
    public static final String START_TAG_KEY = "xmlInput.start";
    public static final String END_TAG_KEY = "xmlInput.end";
    private static final transient Logger logger = Logger.getLogger("Map");

    @Override
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        logger.setLevel(Level.DEBUG);
        return new XmlRecordReader();
    }

    public static class XmlRecordReader extends RecordReader<LongWritable, Text> {
        private final DataOutputBuffer buffer = new DataOutputBuffer();
        private final LongWritable key = new LongWritable();
        private final Text value = new Text();
        private byte[] startTag;
        private byte[] endTag;
        private long start;
        private long end;
        private FSDataInputStream fsInput;

        @Override
        public void initialize(InputSplit is, TaskAttemptContext tac) throws IOException {
            FileSplit fileSplit = (FileSplit) is;
            startTag = tac.getConfiguration().get(START_TAG_KEY).getBytes(StandardCharsets.UTF_8);
            endTag = tac.getConfiguration().get(END_TAG_KEY).getBytes(StandardCharsets.UTF_8);

            start = fileSplit.getStart();
            end = start + fileSplit.getLength();

            Path file = fileSplit.getPath();
            Configuration conf = tac.getConfiguration();
            CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(conf);
            final CompressionCodec codec = compressionCodecs.getCodec(file);
            logger.debug("codec seen as " + codec);

            FileSystem fs = file.getFileSystem(conf);
            fsInput = fs.open(fileSplit.getPath());
            fsInput.seek(start);
            logger.debug("see first location of the start as " + fsInput.getPos());
        }

        @Override
        public boolean nextKeyValue() throws IOException {
            if (fsInput.getPos() < end) {
                if (readUntilMatch(startTag, false)) {
                    key.set(fsInput.getPos() - startTag.length);
                    try {
                        buffer.write(startTag);
                        if (readUntilMatch(endTag, true)) {
                            value.set(buffer.getData(), 0, buffer.getLength());
                            return true;
                        } else if (0 != buffer.getLength()) {
                            value.set(buffer.getData(), 0, buffer.getLength());
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
        public LongWritable getCurrentKey() {
            return key;
        }

        @Override
        public Text getCurrentValue() {
            return value;
        }

        @Override
        public float getProgress() throws IOException {
            return (fsInput.getPos() - start) / (float) (end - start);
        }

        @Override
        public void close() throws IOException {
            fsInput.close();
        }

        private boolean readUntilMatch(byte[] match, boolean withinBlock) throws IOException {
            int i = 0;
            while (true) {
                // This is the current byte we're looking at.
                // Sorry for the name.
                int b = fsInput.read();

                // This is the last byte of the file.
                // Return false, since we couldn't find a match before the file ended.
                if (b == -1) return false;

                // Save the current byte to the buffer
                if (withinBlock) {
                    // This block only writes the current byte, if
                    buffer.write(b);
                }

                // Check byte for byte, if we're matching the given "match" byte String.
                if (b == match[i]) {
                    i++;
                    // The amount of matched characters is the amount of the word we're looking for.
                    if (i == match.length) return true;
                } else {
                    // The next character didn't match. Start looking from the beginning.
                    i = 0;
                }

                // See if we've passed the stop point:
                if (!withinBlock && i == 0 && fsInput.getPos() >= end) return false;
            }
        }
    }
}