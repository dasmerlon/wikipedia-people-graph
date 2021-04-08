package basecamp.project.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uhh_lt.datenbank.MySQLconnect;

/**
 * Stellt die `/relatedPerson` Route bereit, die aus main.js aufgerufen wird.
 */
@RestController
@RequestMapping("/relatedPersons")
public class RelatedPersonController {

    /**
     * Gibt alle Personen zurück, die zur gegebenen Person related sind.
     *
     * @param person     Eingegebener Name für Personen
     * @param startsWith Ausgewählter Anfangsbuchstabe für Namen einer Person
     * @return finaler JSON-String mit Timeline-Daten zu gefilterten Personen. Dient als Eingabe für die anychart-gantt.min.js Library im main.js.
     */
    @GetMapping
    public String filter(
            @RequestParam(value = "person") String person,
            @RequestParam(value = "startsWith", required = false, defaultValue = "") String startsWith
        ) throws Exception {

        MySQLconnect con = new MySQLconnect();

        return con.getRelatedPersonData(person, startsWith);
    }
}

