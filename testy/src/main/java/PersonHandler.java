import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PersonHandler extends DefaultHandler {

    private static final String TEXT = "text";

    private final List<Person> personList = new ArrayList<>();
    private StringBuilder characters;

    private boolean isCategory;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        characters = new StringBuilder();
        isPerson = false;
    }

    @Override public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(TEXT)) {
            final String contentOfXml = characters.toString().trim();
            Person person = new Person();

            try (StringReader stringReader = new StringReader(contentOfXml);
                 BufferedReader reader = new BufferedReader(stringReader)) {

                String line;
                while (null != (line = reader.readLine())) {
                    line = line.trim();

                    if (line.contains("[[Category:")) {
                        isCategory = true;
                    }
                    if (isCategory) {
                        if (line.startsWith("|NAME")) {
                            person.setName(line.replace("|NAME=", ""));
                        }
                        if (line.startsWith("[[Category:") && line.contains("")) {
                            person.setBirthDate(line.replace("|GEBURTSDATUM=", ""));
                        }
                        if (line.startsWith("|STERBEDATUM")) {
                            person.setDeathDate(line.replace("|STERBEDATUM=", ""));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (!personList.contains(person) && person.getName() != null
                        && person.getBirthDate() != null) {
                    personList.add(person);
                }
            }
        }
    }

    @Override public void characters(char[] ch, int start, int length) throws SAXException {
        characters.append(new String(ch, start, length));
    }

    public List<Person> getPersonList() {
        return personList;
    }
}
