import org.xml.sax.SAXException;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ParseXML {

    public static void main(String[] args) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            File file = new File("/user/7mhoffma/wikipedia2021_en/enwiki-latest-pages-meta-current.xml");

            PersonHandler personHandler = new PersonHandler();
            parser.parse(file, personHandler);

            File out = new File("personen.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));
            writer.write(personHandler.getPersonList().toString());
            writer.close();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

}