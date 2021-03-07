package basecamp.project.server.controller;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello/users")
public class UserControllerMap {

	@Value("${service.key}")
	private String key;

	@GetMapping
	public String users() {

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
		return result.toJSONString();
	}

	@RequestMapping("nrusers")
	String nrUsers() {
		return "2";
	}


	@RequestMapping(value = "/view/{userId}")
	public String getUserData(@PathVariable Integer userId) {
		return userId.toString();
	}
}
