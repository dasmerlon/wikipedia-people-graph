package uhh_lt.datenbank;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * Diese Klasse stellt eine Verbindung zu einer MySQL-Datenbank her und kann die Daten auslesen.
 * Die Zugangsdaten für die MySQL-Datenbank werden in credentials.txt definiert
 */
public class MySQLconnect {
    private static Connection con = null;

    /**
     * Stellt eine Verbindung zur Datenbank her.
     */
    public MySQLconnect() throws Exception {
        // Read the credentials from the `resource/credentials.txt` file.
        // That way we don't have to hardcode stuff in our code base.
        InputStream credentials = this.getClass().getClassLoader()
                .getResourceAsStream("credentials.txt");
        if (credentials == null) {
            throw new Exception("Please edit and place a `credentials.txt` into the main/resources folder at build time");
        }

        InputStreamReader streamReader = new InputStreamReader(credentials, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);

        String dbUser = null;
        String dbPass = null;
        String dbHost = null;
        String dbPort = null;
        String dbName = null;
        try {
            String line = reader.readLine();
            while (line != null) {
                String[] fields = line.split("=");
                if (fields[0].compareTo("mysql.user") == 0) {
                    dbUser = fields[1];
                }
                if (fields[0].compareTo("mysql.password") == 0) {
                    dbPass = fields[1];
                }
                if (fields[0].compareTo("mysql.host") == 0) {
                    dbHost = fields[1];
                }
                if (fields[0].compareTo("mysql.port") == 0) {
                    dbPort = fields[1];
                }
                if (fields[0].compareTo("mysql.database") == 0) {
                    dbName = fields[1];
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Abfrage hat nicht funktioniert");
        }

        if (dbHost == null || dbName == null || dbUser == null || dbPass == null || dbPort == null) {
            throw new Exception("Your credentials file has to have a host, port, dbname, user and password");
        }


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");    // Datenbanktreiber für JDBC Schnittstellen laden.

            String URI = String.format("jdbc:mysql://%s:%s/%s?useSSL=false", dbHost, dbPort, dbName);

            System.out.printf("Try to connect to %s with user %s and pass %s%n", URI, dbUser, dbPass);

            // Get Database connection
            con = DriverManager.getConnection(URI, dbUser, dbPass);
            System.out.println("Database connection established");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find DB driver");
        } catch (SQLException e) {
            System.out.println("Database connection failed");
            System.out.printf("SQLException: %s", e.getMessage());
            System.out.printf("SQLState: %s", e.getSQLState());
            System.out.printf("VendorError: %s", e.getErrorCode());
        }
    }

    /**
     * Close the current database connection
     */
    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            System.out.println("Verbindung konnte nicht geschlossen werden");
        }
    }

    /**
     * Liest die Personendaten für die Timeline mit den entsprechenden Filtern aus der MySQL-Datenbank aus. Konvertiert das zurückgegebene ResultSet in ein JSONArray mit Hilfe der ResultSetConverter-Klasse.
     * @param  person Name für zu filternde Personen
     * @param  birthdate Geburtsdatum für zu filternde Personen
     * @param  deathdate Todesdatum für zu filternde Personen
     * @param  job Berufsbezeichnung für zu filternde Personen
     * @param  startsWith Anfangsbuchstabe für zu filternde Personen
     * @return JSONString, welcher die Personendaten aus der SQL-Datenbank beinhaltet
     */
    public String getPersonData(String person, String birthdate, String deathdate, String job, String startsWith) throws SQLException {

        Statement st = con.createStatement();

        if (person.equals("")) {
            person = "!=" + "'" + person + "'";
        } else {
            person = " LIKE" + "'%" + person + "%'";
        }

        // Geburts und Todesdatum sind in unserer DB leider als String gespeichert, daher können wir keine "Zeitbereiche" eingrenzen...
        if (birthdate.equals("")) {
            birthdate = "!=" + "'" + birthdate + "'";
        } else {

            birthdate = "=" + "'AD-" + birthdate + "'";
        }

        if (deathdate.equals("")) {
            deathdate = "!=" + "'" + deathdate + "'";
        } else {

            deathdate = "=" + "'AD-" + deathdate + "'";
        }

        if (job.equals("")) {
            job = "!=" + "'" + job + "'";
        } else {
            job = " LIKE" + "'%" + job + "%'";
        }

        if (startsWith.equals("")) {
            startsWith = "!=" + "'" + startsWith + "'";
        } else {
            startsWith = " LIKE" + "'" + startsWith + "%'";
        }

        String sql = ("SELECT * FROM PersonData WHERE (OCCUPATION" + job + " OR OFFICE" + job + ") AND BIRTH_DATE" + birthdate + " AND DEATH_DATE" + deathdate + " AND (TITLE" + person + " AND TITLE" + startsWith + ") LIMIT 200" + " ;");

        ResultSet rs = null;
        try {
            rs = st.executeQuery(sql);
            JSONArray jsonReceived = ResultSetConverter.convert(rs);
            System.out.println(jsonReceived);
            String j = jsonReceived.toString();

            //System.out.println(j);
            return j;

        } catch (SQLException | JSONException e) {
            System.out.printf("Anfrage konnte nicht ausgeführt werden: %s", e.getMessage());
        }

        return null;
    }

    /**
     * Gibt alle Personen, die zur einer gegebenen Person related sind.
     *
     * @param  person Name für zu filternde Personen
     * @param  startsWith   Anfangsbuchstabe für genauere Filter
     * @return JSONString, welcher die Personendaten aus der SQL-Datenbank beinhaltet
     */
    public String getRelatedPersonData(String person, String startsWith) throws SQLException {
        Statement st = con.createStatement();
        String sql = String.format("SELECT * FROM PersonData WHERE (PersonData.title in (SELECT PERSON2 FROM Relationships WHERE Relationships.PERSON1 = '%s') OR PersonData.title = '%s')", person, person);

        // Falls ein startsWith mitgeliefert wird, filtern wir darauf zusätzlich.
        if (!startsWith.isEmpty()) {
            String startsWithQuery = String.format(" AND PersonData.title LIKE '%s%%'", startsWith);
            sql += startsWithQuery;
        }

        ResultSet rs;
        try {
            rs = st.executeQuery(sql);
            JSONArray jsonReceived = ResultSetConverter.convert(rs);
            System.out.println(jsonReceived);
            String j = jsonReceived.toString();

            //System.out.println(j);
            return j;

        } catch (SQLException | JSONException e) {
            System.out.printf("Anfrage konnte nicht ausgeführt werden: %s", e.getMessage());
        }

        return null;
    }

    /**
     * Liest die Beziehungsdaten für den Netzwerkgraph mit den entsprechenden Filtern aus der MySQL-Datenbank aus. Konvertiert das zurückgegebene ResultSet in ein JSONArray mit Hilfe der ResultSetConverter-Klasse.
     * @param  person Name für zu filternde Person
     * @return JSONString, welcher die Beziehungsdaten aus der SQL-Datenbank beinhaltet
     */

    public String getRelationships(String person) throws SQLException {
        Statement st = con.createStatement();

        if (person.equals("")) {
            person = "!=" + "'" + person + "'";
        } else {
            person = "=" + "'" + person + "'";
        }

        String sql = ("SELECT * FROM Relationships WHERE PERSON1" + person + " ;");

        ResultSet rs;
        try {
            rs = st.executeQuery(sql);
            JSONArray jsonReceived = ResultSetConverter.convert(rs);
            System.out.println(jsonReceived);
            String j = jsonReceived.toString();

            return j;

        } catch (SQLException | JSONException e) {
            System.out.println("Anfrage konnte nicht ausgeführt werden");
        }

        return null;
    }
}