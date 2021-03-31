package basecamp.project.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uhh_lt.datenbank.MySQLconnect;

import java.sql.SQLException;
//import uhh_lt.datenverarbeitung.Verarbeitung; //vermutlich nur daten ummodeln


@RestController
@RequestMapping("/graph_data")
public class GraphController {

    @Value("${service.key}")
    private String key;

    @GetMapping    //umbennen in filters z.B. wenn funktionierend

    //TODO defaultValue ggf. anpassen

    public String users(@RequestParam(value = "person", required = false, defaultValue = "") String person,
                        @RequestParam(value = "secLayer", required = false, defaultValue = "") Boolean secLayer) throws Exception {


        MySQLconnect con = new MySQLconnect();
        System.out.println("Connector erstellt");
        //double[] watsonData = con.getWatson(date);


        String jsonout = con.getRelationships(person);

        System.out.println("WERT SEC LAYER = " + secLayer);
        String jsonProcessed = converter(jsonout, secLayer); //vorher true

        //Geburts und Todesdaten umformatieren


        //String formattedjsonout = converter(jsonout);


        //return jsonProcessed;

        return jsonProcessed;
    }

    @RequestMapping("nrusers")
    String nrUsers() {
        return "2";
    }


    public String converter(String jsonStr, Boolean layer0) throws Exception { //wenn layer0 == true, dann hole nächste Ebene

        System.out.println("CONVERTER INPUT " + jsonStr);


        JSONParser parser = new JSONParser();

        //JSONArray inputArray = (JSONArray) parser.parse(jsonStr);
        JSONArray inputArray = new JSONArray(jsonStr);
        //new JSONArray(jsonStr);

        //System.out.println("NACH PARSEN " + inputArray);

        JSONArray nodesArray = new JSONArray();
        JSONArray edgesArray = new JSONArray();
        JSONArray contactPersonArray = new JSONArray(); // wird noch gebraucht?

        //build nodes

        for (int i = 0; i < inputArray.length(); i++) {

            JSONObject o2 = inputArray.getJSONObject(i);

            //String neueId1 = ((JSONObject) o).getString("PERSON2");
            String neueId1 = (String) o2.get("PERSON2");
            //System.out.println("PERSON 2 ziehen" + (String) o2.get("PERSON2") + neueId1 );
            JSONObject node1 = new JSONObject();

            node1.put("id", neueId1);

            //System.out.println("node1 Objekt" + node1);

            nodesArray.put(node1);


            JSONObject contactObj = new JSONObject();
            contactObj.put("ContactName", neueId1);

            contactPersonArray.put(contactObj);


            //contactPersonArray.add(neueId1);

            String neueId2 = (String) o2.get("PERSON1");
            JSONObject node2 = new JSONObject();
            node2.put("id", neueId2);

            nodesArray.put(node2);


            inputArray.getJSONObject(i);
        }


        //System.out.println("NODE ARRAY " + nodesArray);


        //build edges


        for (int i = 0; i < inputArray.length(); i++) {
            JSONObject o2 = inputArray.getJSONObject(i);

            //String neueId1 = ((JSONObject) o).getString("PERSON2");
            String neueId1 = (String) o2.get("PERSON2");
            String neueId2 = (String) o2.get("PERSON1");
            JSONObject edge = new JSONObject();
            edge.put("from", neueId1);
            edge.put("to", neueId2);

            edgesArray.put(edge);


        }


        JSONObject obj = new JSONObject();

        obj.put("nodes", nodesArray);

        obj.put("edges", edgesArray);


        System.out.println("JSON OBJECT " + obj);
        System.out.println("JSON OBJECT TO STRING " + obj.toString());
        System.out.println("contactPersonArray " + contactPersonArray);


        if (layer0 == true) {

            JSONObject out = getSecondLayer(contactPersonArray, obj);

            System.out.println("FINAL OUT" + out);
            return out.toString();
        } else {
            return obj.toString();
        }
        //return obj.toString() ;

    }


	/*public JSONObject getContactPersons(JSONObject jsonStr) throws JSONException, ParseException { //INPUT JSON DIREKT AUS DB
		System.out.println("CONVERTER INPUT " + jsonStr);
		JSONObject layer0Output = jsonStr;

		//String newjsonStr = jsonStr.toString();


		String neujsonStr = jsonStr.toString();
		neujsonStr = neujsonStr.substring( 1, neujsonStr.length() - 1 ) ;

		JSONArray inputArray = new JSONArray(neujsonStr) ;

		//JSONArray inputArray = parser.parse(newjsonStr);
		//new JSONArray(jsonStr);

		System.out.println("NACH PARSEN " + inputArray);

		JSONArray contactPersonArray = new JSONArray();

		//build nodes

		for(Object o: inputArray){

			JSONObject o2 = (JSONObject)o;

			//String neueId1 = ((JSONObject) o).getString("PERSON2");
			String neueId1 = (String) o2.get("PERSON2");
			System.out.println("PERSON 2 ziehen" + (String) o2.get("PERSON2") + neueId1 );

			JSONObject contactObj = new JSONObject();
			contactObj.put("ContactName", neueId1);

			contactPersonArray.add(contactObj);

		}


		JSONObject out = getSecondLayer(contactPersonArray, layer0Output);
		return (out);

	}*/


    public JSONObject getSecondLayer(JSONArray nodesLayer1, JSONObject layer0Ausgabe) throws Exception //INPUT CONTACT PERSON ARRAY
    {
        MySQLconnect con = new MySQLconnect();
        System.out.println("Connector erstellt");
        JSONArray alleDatenLayer2 = new JSONArray();


        for (int i = 0; i < nodesLayer1.length(); i++) {
            JSONObject o2 = nodesLayer1.getJSONObject(i);
            String nextPerson = (String) o2.get("ContactName");

            String jsonout = con.getRelationships(nextPerson); //hole Beziehungen aus DB
            String jsonProcessed = converter(jsonout, false); //get Objekt mit nodes Array und egdes Array
            JSONObject processedobj = new JSONObject(jsonProcessed);
            alleDatenLayer2.put(processedobj);

        }


        JSONArray allNodes = new JSONArray();
        JSONArray allEdges = new JSONArray();


        for (int i = 0; i < alleDatenLayer2.length(); i++) {      //alleDatenLayer2 IST ZUSAMMENGEFUEGT AUS AUS CONVERTER OUTPUTS
			/*JSONObject o2 = parser.parse(o);
			JSONArray nodesInO = (JSONArray) o2.get("nodes");
			JSONArray edgesInO = (JSONArray) o2.get("edges");*/

            JSONObject o3 = alleDatenLayer2.getJSONObject(i);

            JSONArray nodesInO = (JSONArray) o3.get("nodes");
            JSONArray edgesInO = (JSONArray) o3.get("edges");


            for (int j = 0; j < nodesInO.length(); j++) {
                String nextPerson = (String) nodesInO.getJSONObject(j).get("id");

                JSONObject lulu = new JSONObject();
                lulu.put("id", nextPerson);
                allNodes.put(lulu);
            }


            for (int z = 0; z < edgesInO.length(); z++) {

                String fromPerson = (String) edgesInO.getJSONObject(z).get("from");
                String toPerson = (String) edgesInO.getJSONObject(z).get("to");

                JSONObject lala = new JSONObject();
                lala.put("from", fromPerson);
                lala.put("to", toPerson);
                allEdges.put(lala);

            }

        }


        JSONObject moddedjson = new JSONObject();
        moddedjson.put("nodes", allNodes);
        moddedjson.put("edges", allEdges);

        JSONObject out = MergeLayer0AndLayer1(moddedjson, layer0Ausgabe);

        return (out);

        //     alleDatenLayer2 = [ { nodes:[{},{},{}], edges:[{},{},{}] } ,    { nodes:[{},{},{}], edges:[{},{},{}] } ]

        //            = { allnodes:[{},{},{}], alledges:[{},{},{}] }


    }

    public JSONObject MergeLayer0AndLayer1(JSONObject NodesEdgesLayer1, JSONObject NodesEdgesLayer0) throws JSONException, ParseException {
        // INPUT NodesEdgesLayer1 = { nodes:[{},{},{}], edges:[{},{},{}] }
        // INPUT NodesEdgesLayer0 = { nodes:[{},{},{}], edges:[{},{},{}] }

        JSONArray nodesComplete = new JSONArray();
        JSONArray edgesComplete = new JSONArray();

        JSONObject o2 = (JSONObject) NodesEdgesLayer0;
        JSONArray nodesInLayer0 = (JSONArray) o2.get("nodes");
        JSONArray edgesInLayer0 = (JSONArray) o2.get("edges");

        JSONObject o3 = (JSONObject) NodesEdgesLayer1;
        JSONArray nodesInLayer1 = (JSONArray) o3.get("nodes");
        JSONArray edgesInLayer1 = (JSONArray) o3.get("edges");


        for (int i = 0; i < nodesInLayer0.length(); i++) {

            String nextPerson = (String) nodesInLayer0.getJSONObject(i).get("id");

            JSONObject lulu = new JSONObject();
            lulu.put("id", nextPerson);
            nodesComplete.put(lulu);

        }

        for (int i = 0; i < edgesInLayer0.length(); i++) {

            String fromPerson = (String) edgesInLayer0.getJSONObject(i).get("from");
            String toPerson = (String) edgesInLayer0.getJSONObject(i).get("to");

            JSONObject lala = new JSONObject();
            lala.put("from", fromPerson);
            lala.put("to", toPerson);
            edgesComplete.put(lala);

        }


        for (int i = 0; i < nodesInLayer1.length(); i++) {

            String nextPerson = (String) nodesInLayer1.getJSONObject(i).get("id");

            JSONObject lulu = new JSONObject();
            lulu.put("id", nextPerson);
            nodesComplete.put(lulu);
        }


        for (int i = 0; i < edgesInLayer1.length(); i++) {

            String fromPerson = (String) edgesInLayer1.getJSONObject(i).get("from");
            String toPerson = (String) edgesInLayer1.getJSONObject(i).get("to");

            JSONObject lala = new JSONObject();
            lala.put("from", fromPerson);
            lala.put("to", toPerson);
            edgesComplete.put(lala);


        }

        JSONObject out = new JSONObject();
        out.put("nodes", nodesComplete);
        out.put("edges", edgesComplete);

        return (out);
    }

    // users --> getWatson --> Converter --> ContactPerson --> getSecondLayer --> MergeLayer0AndLayer1 --> return to users
}

