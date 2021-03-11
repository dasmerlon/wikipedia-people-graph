package basecamp.project.server.controller;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uhh_lt.datenbank.MySQLconnect;
//import uhh_lt.datenverarbeitung.Verarbeitung; //vermutlich nur daten ummodeln

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

@RestController
@RequestMapping("/users")
public class FilterController {

	@Value("${service.key}")
	private String key;

	@GetMapping	//umbennen in filters z.B. wenn funktionierend

	//TODO defaultValue ggf. anpassen

	public JSONArray users(@RequestParam(value = "person", required=false, defaultValue = "") String person,
						@RequestParam(value = "birthdate", required=false, defaultValue = "") String birthdate,
						@RequestParam(value = "deathdate", required=false, defaultValue = "") String deathdate,
						@RequestParam(value = "job", required=false, defaultValue = "") String job)
	{

		MySQLconnect con = new MySQLconnect();
		System.out.println("Connector erstellt");
		//double[] watsonData = con.getWatson(date);
		JSONArray jsonout = con.getWatson(person, birthdate, deathdate, job);


		return jsonout;
	}

	@RequestMapping("nrusers")
	String nrUsers() {
		return "2";
	}


}
