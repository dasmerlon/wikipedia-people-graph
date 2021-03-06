package basecamp.project.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uhh_lt.datenbank.MySQLconnect;


/**
 * Stellt die `/person` Route bereit, die aus main.js aufgerufen wird.
 */
@RestController
@RequestMapping("/persons")
public class PersonController {

    /**
     * Erstellt ein Objekt der Klasse MySQLconnect und nutzt dieses, um die mit den Parametern gefilterten
     * Personen-Daten von einer SQL-Datenbank abzurufen.
     *
     * @param person     Eingegebener Name für Personen
     * @param birthdate  Eingegebenes Geburtsdatum für Personen
     * @param deathdate  Eingegebenes Todesdatum für Personen
     * @param job        Eingegebene Berufsbezeichnung für Personen
     * @param startsWith Ausgewählter Anfangsbuchstabe für Namen einer Person
     * @return finaler JSON-String mit Personen-Daten. Dient als Eingabe für die anychart-gantt.min.js Library im main.js.
     */
    @GetMapping
    public String filter(@RequestParam(value = "person", required = false, defaultValue = "") String person,
                         @RequestParam(value = "birthdate", required = false, defaultValue = "") String birthdate,
                         @RequestParam(value = "deathdate", required = false, defaultValue = "") String deathdate,
                         @RequestParam(value = "job", required = false, defaultValue = "") String job,
                         @RequestParam(value = "startsWith", required = false, defaultValue = "") String startsWith) throws Exception {

        MySQLconnect con = new MySQLconnect();
        return con.getPersonData(person, birthdate, deathdate, job, startsWith);
    }
}