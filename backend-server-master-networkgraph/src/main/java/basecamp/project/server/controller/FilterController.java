package basecamp.project.server.controller;


import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uhh_lt.datenbank.MySQLconnect;
//import uhh_lt.datenverarbeitung.Verarbeitung; //vermutlich nur daten ummodeln


@RestController
@RequestMapping("/users")
public class FilterController {

	@Value("${service.key}")
	private String key;

	@GetMapping	//umbennen in filters z.B. wenn funktionierend

	//TODO defaultValue ggf. anpassen

	public String users(@RequestParam(value = "person", required=false, defaultValue = "") String person) throws JSONException {

		MySQLconnect con = new MySQLconnect();
		System.out.println("Connector erstellt");
		//double[] watsonData = con.getWatson(date);


		String jsonout = con.getWatson(person);





	//Geburts und Todesdaten umformatieren


		//String formattedjsonout = converter(jsonout);

		return jsonout;
	}

	@RequestMapping("nrusers")
	String nrUsers() {
		return "2";
	}



	// Example of jsonStr
/* String jsonStr = "{\n"
        + "\"dt1\":\"12-12-2020\",\n"
        + "\"street\":\"test\",\n"
        + "\"city\":\"test2\",\n"
        + "\"country\":\"test3\",\n"
        + "\"dt2\":\"12-11-2020\"\n"
        + "}";
*/
	public String converter(String jsonStr) throws JSONException {

		// jsonStr is our original JSON with the date in the dash format (e.g. "12-12-2020")

		JSONObject jo = new JSONObject();

		// populate the array
		jo.put("unsereAlteJson",jsonStr);
		// Here we convert into our json object
		//JSONObject jsono = new JSONObject(jsonStr);

		//jsono.forEach((item) => item.BIRTH_DATE = item.optString("BIRTH_DATE").substring(3,dateStr.length()+1 ));

	/*	// Get the orginal date value
		for (int i = 0; i< jo.getJSONArray("unsereAlteJson").length(); i++ ) {

			String dateStrB = (String) jo.getJSONArray("unsereAlteJson")[i].get("BIRTH_DATE");
			String dateStrD = (String) jo.get("DEATH_DATE");

			// Convert the date into new format and update the JSON key 'dt1' with the new value
			jo.put("BIRTH_DATE", dateConverter(dateStrB));
			jo.put("DEATH_DATE", dateConverter(dateStrD));


		}*/


/*
		for (int i=0; i < jsono.length(); i++){
			JSONObject itemArr = (JSONObject)jsono.get(i);
			if(itemArr.get("IDSERV").getAsString().equals("2")){
				itemArr.put("STATUSUPDATE", 1);
			}else if(itemArr.get("IDSERV").getAsString().equals("3")){
				itemArr.put("STATUSUPDATE", 2);
			}
		}*/

		return jo.toString();
	}

	private String dateConverter(String dateStr) {
		if (dateStr.equals("NONE"))
		{
			return "";
		}
		else
		{
			String neuesDatum = dateStr.substring(3,dateStr.length()+1 );
			return neuesDatum;
		}

	}



}

