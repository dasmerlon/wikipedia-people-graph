package uhh_lt.datenbank;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.*;

/**
 * Diese Klasse stellt eine Verbindung zu MySQL her und kann Daten in die Datenbank speichern, bestehende Zeilen aktualisieren und Daten auslesen.
 */
public class MySQLconnect {
    private static Connection con = null;
    private static String dbHost = "127.0.0.1";            // Hostname
    private static String dbPort = "3306";                    // Port -- Standard: 3306
    private static String dbName = "peoplegraph";    // Datenbankname
    private static String dbUser = "root";    // Datenbankuser
    private static String dbPass = "rofllol";                // Datenbankpasswort
    private static String dbTable = "PersonData";    // Tabelle //PersonData PersonsStringDate

    /**
     * Stellt eine Verbindung zur Datenbank her.
     */
    public MySQLconnect() {
        // get credentials
		/*InputStream is = MySQLconnect.class.getResourceAsStream("/credentials.txt");
		InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
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
				if (fields[0].compareTo("mysql.database") == 0) {
					dbName = fields[1];
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("Abfrage hat nicht funktioniert");
		}*/


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");    // Datenbanktreiber für JDBC Schnittstellen laden.

            String URI = String.format("jdbc:mysql://%s:%s/%s?useSSL=false", dbHost, dbPort, dbName);

            System.out.printf("Try to connect to %s with user %s and pass %s%n", URI, dbUser, dbHost);

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
     * Liest Watson Daten aus der Datenbank aus und errechnet Durchschnittswerte für den Sentiment Score und die Emotions.
     * * @return ein double Array mit den Watson Werten in der Reihenfolge Sentiment, Sadness, Joy, Fear, Disgust, Anger
     */
    public String getPersonData(String person, String birthdate, String deathdate, String job) throws SQLException {

        Statement st = null;
        st = con.createStatement();

        if (person.equals("")) {
            person = "!=" + "'" + person + "'";
        } else {
            person = " LIKE" + "'%" + person + "%'";
        }

        // Geburts und Todesdatum sind in der DB leider als String gespeichert, daher können wir keine "Zeitbereiche" eingrenzen...
        // TODO Genaues Datum funktioniert aber auch nicht, in welchem Format liegt das Datum in der DB vor? Ansonsten die Buttons lieber löschen
        if (birthdate.equals("")) {
            birthdate = "!=" + "'" + birthdate + "'";
        } else {
            birthdate = "=" + "'" + birthdate + "'";
        }

        if (deathdate.equals("")) {
            deathdate = "!=" + "'" + deathdate + "'";
        } else {
            deathdate = "=" + "'" + deathdate + "'";
        }

        if (job.equals("")) {
            job = "!=" + "'" + job + "'";
        } else {
            job = " LIKE" + "'%" + job + "%'";
        }

        String sql = ("SELECT * FROM " + dbTable + " WHERE OCCUPATION" + job + " AND BIRTH_DATE" + birthdate + " AND DEATH_DATE" + deathdate + " AND TITLE" + person + " LIMIT 200" + " ;");

        ResultSet rs = null;
        try {
            rs = st.executeQuery(sql);
            JSONArray jsonReceived = uhh_lt.datenbank.ResultSetConverter.convert(rs);
            System.out.println(jsonReceived);
            String j = jsonReceived.toString();

            //System.out.println(j);
            return j;


        } catch (SQLException | JSONException e) {
            System.out.println("Anfrage konnte nicht ausgeführt werden");
        }

        return null;
    }


    public String getRelationships(String person) throws SQLException {
        Statement st = null;
        st = con.createStatement();

        String sql = "SELECT * FROM Relationships";

        if (!person.equals("")) {
            sql += String.format(" WHERE PERSON1 = %s;", person);
        } else {
            sql += ";";
        }


        ResultSet rs = null;
        //TODO eventuell als JSONARRAY anpassen
        try {
            rs = st.executeQuery(sql);
            JSONArray jsonReceived = uhh_lt.datenbank.ResultSetConverter.convert(rs);
            System.out.println(jsonReceived);
            String j = jsonReceived.toString();

            //JSONArray newJArray = new JSONArray(j);
            //System.out.println(j);

            return j;
        } catch (SQLException | JSONException e) {
            System.out.println("Anfrage konnte nicht ausgeführt werden");
        }

        return null;
    }
}


