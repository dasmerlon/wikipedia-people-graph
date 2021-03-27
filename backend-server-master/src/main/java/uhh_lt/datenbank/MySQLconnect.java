package uhh_lt.datenbank;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
//import org.json.JSONObject;
import org.json.JSONException;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * Diese Klasse stellt eine Verbindung zu MySQL her und kann Daten in die Datenbank speichern, bestehende Zeilen aktualisieren und Daten auslesen.
 */
public class MySQLconnect{
	private static Connection con = null;
	private static String dbHost = "127.0.0.1";			// Hostname
	private static String dbPort = "3306";					// Port -- Standard: 3306
	private static String dbName = "peoplegraph_prakt21";	// Datenbankname
	private static String dbUser = "peoplegraph_prakt21";	// Datenbankuser
	private static String dbPass = "W22$wqtF";				// Datenbankpasswort
	private static String dbTable = "PersonData";	// Tabelle //PersonData PersonsStringDate

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

			System.out.println("Versuche mit " + "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false"+"," + dbUser +"," + dbPass + " zu verbinden");

			// Verbindung zur JDBC-Datenbank herstellen.
			con = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false", dbUser, dbPass);
			//con = DriverManager.getConnection("jdbc:mysql://134.100.14.49:3306/" + dbName + "?user=" + dbUser + "&password=" + dbPass + "&useUnicode=true&characterEncoding=UTF-8");
			System.out.println("Verbindung zur JDBC-Datenbank hergestellt");
		} catch (ClassNotFoundException e) {
			System.out.println("Treiber nicht gefunden");
		} catch (SQLException e) {
			System.out.println("Verbindung nicht moglich");
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}

	/**
	 * Schließt die Verbindung zur Datenbank
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
	public String getWatson(String person, String birthdate, String deathdate, String job) {

		Statement st = null;
		try {
			st = con.createStatement();
			System.out.println("Statement erstellt");
		} catch (SQLException e) {
			System.out.println("Statement konnte nicht erstellt werden");
		}


		if(person.equals(""))
		{
			person = "!=" + "'" + person + "'";
		}

		else
		{
			person = " LIKE" + "'%" + person + "%'";
		}

		if(birthdate.equals(""))
		{
			birthdate = "!=" + "'" + birthdate + "'";
		}

		else
		{
			birthdate = "=" + "'" + birthdate + "'"; //Geburts und Todesdatum sind in der DB leider als String gespeichert, daher können wir keine "Zeitbereiche" eingrenzen...
		}											//TODO Genaues Datum funktioniert aber auch nicht, in welchem Format liegt das Datum in der DB vor? Ansonsten die Buttons lieber löschen
		if(deathdate.equals(""))
		{
			deathdate = "!=" + "'" + deathdate + "'";
		}

		else
		{
			deathdate = "=" + "'" + deathdate + "'";
		}
		if(job.equals(""))
		{
			job = "!=" + "'" + job + "'";
		}

		else
		{
			job = " LIKE" + "'%" + job + "%'";
		}




		//String X = "'Julius Caesar'";
		//String sql = ("SELECT * FROM " + dbTable + " WHERE TITLE=" + X + ";");
		String sql = ("SELECT * FROM " + dbTable + " WHERE OCCUPATION" +  job + " AND BIRTH_DATE" + birthdate + " AND DEATH_DATE" + deathdate + " AND TITLE" + person + " LIMIT 10000"+" ;");
		// "SELECT * FROM " + dbTable + " WHERE TITLE = 'Abraham Lincoln';"

		//String sql = ("SELECT * FROM " + dbTable + " WHERE BIRTH_DATE" + birthdate +" AND DEATH_DATE" + deathdate + " AND TITLE" + person + " AND OCCUPATION" + job +" ;");

		//"SELECT * FROM " + dbTable + " WHERE TITLE = 'Abraham Lincoln';"
		//String sql = ("SELECT * FROM " + dbTable + "WHERE TITLE = "Julius Caesar" LIMIT 10"+" ;"); //"SELECT * FROM " + dbTable + " WHERE TITLE = 'Abraham Lincoln';"
		//ResultSet rs = null;
		ResultSet rs = null;

		try {
			rs = st.executeQuery(sql);
			JSONArray jsonReceived = uhh_lt.datenbank.ResultSetConverter.convert(rs);
			System.out.println(jsonReceived);
			String j = jsonReceived.toString();

			//JSONArray newJArray = new JSONArray(j);
			System.out.println(j);

			return j;


		}
		catch (SQLException e) {
			System.out.println("Anfrage konnte nicht ausgeführt werden");
		}
		catch (JSONException e) {
			System.out.println("Anfrage konnte nicht ausgeführt werden");
		}

		return null;

	}

}


