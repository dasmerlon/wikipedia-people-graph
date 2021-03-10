/*
package basecamp.project.server.controller;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

	@Value("${service.key}")
	private String key;

	@GetMapping
	public String users_alt(@RequestParam(defaultValue = "User") String name)  {


		JSONObject result =new JSONObject();
		result.put("titel", "mathe");
		result.put("dauer", 45);

		JSONObject user1 = new JSONObject();
		user1.put("first", "User");
		user1.put("last", "A");

		JSONObject user2 = new JSONObject();
		user2.put("first", "Person");
		user2.put("last", "B");

		JSONArray userList = new JSONArray();
		userList.add(user1);
		userList.add(user2);

		result.put("users", userList);
		return "[\n" +
				"\n" +
				"\n" +
				"{\n" +
				"  \"id\": \"1\",\n" +
				"  \"name\": \"Dschingis Khan\",\n" +
				"  \"birth_date\": \"1112-02-07\",\n" +
				"  \"death_date\": \"1178-01-05\",\n" +
				"  \"info\": \"Dischingis Khan konnte ganz gut mit Bögen und Pferden.\"\n" +
				"},\n" +
				"{\n" +
				"\"id\": \"2\",\n" +
				"\"name\": \"Mahad Ma Gandi\",\n" +
				"\"birth_date\": \"1920-09-11\",\n" +
				"\"death_date\": \"1980-08-07\",\n" +
				"\"info\": \"Hat so viel Yoga gemacht, dass es für ne Revolution reichte.\"\n" +
				"},\n" +
				"{\n" +
				"\"id\": \"3\",\n" +
				"\"name\": \"Rainer Kallmund\",\n" +
				"\"birth_date\": \"1970-10-11\",\n" +
				"\"info\": \"Rainer Kallmund ist ein deutscher Fußballfunktionär und Lebemann.\"\n" +
				"},\n" +
				"{\n" +
				"\"id\": \"4\",\n" +
				"\"name\": \""+name+" Schmallmund\",\n" +
				"\"birth_date\": \"1971-10-11\",\n" +
				"\"info\": \"Rainer Kallmund ist ein deutscher Fußballfunktionär und Lebemann.\"\n" +
				"},\n" +
				"\n" +
				"  {\n" +
				"    \"id\": \"5\",\n" +
				"    \"name\": \"Kainer Schmallmund\",\n" +
				"    \"birth_date\": \"1971-10-11\",\n" +
				"    \"info\": \"Rainer Kallmund ist ein deutscher Fußballfunktionär und Lebemann.\"\n" +
				"  }\n" +
				"\n" +
				"]";
	}

	@RequestMapping("nrusers_alt")
	String nrUsers() {
		return "2";
	}


}
*/
