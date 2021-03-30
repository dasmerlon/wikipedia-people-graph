package basecamp.project.server.controller;

//import org.json.JSONArray;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uhh_lt.datenbank.MySQLconnect;

import java.sql.SQLException;
// import uhh_lt.datenverarbeitung.Verarbeitung;
// vermutlich nur daten ummodeln


@RestController
@RequestMapping("/graph_data")
public class GraphController {

    @Value("${service.key}")
    private String key;

    @GetMapping
    public String users(@RequestParam(value = "person", required = false, defaultValue = "") String person) throws Exception {
        MySQLconnect con = new MySQLconnect();
        System.out.println("Connector erstellt");

        String jsonout = con.getRelationships(person);
        String jsonProcessed = converter(jsonout);
        return jsonProcessed;
    }

    public String converter(String jsonStr) throws JSONException, ParseException {

        System.out.println("CONVERTER INPUT " + jsonStr);

        JSONParser parser = new JSONParser();
        JSONArray inputArray = (JSONArray) parser.parse(jsonStr);

        System.out.println("NACH PARSEN " + inputArray);

        JSONArray nodesArray = new JSONArray();
        JSONArray edgesArray = new JSONArray();
        JSONArray contactPersonArray = new JSONArray();

        //build nodes
        for (Object o : inputArray) {

            JSONObject o2 = (JSONObject) o;

            //String neueId1 = ((JSONObject) o).getString("PERSON2");
            String neueId1 = (String) o2.get("PERSON2");
            System.out.println("PERSON 2 ziehen" + (String) o2.get("PERSON2") + neueId1);
            JSONObject node1 = new JSONObject();

            node1.put("id", neueId1);

            System.out.println("node1 Objekt" + node1);

            nodesArray.add(node1);
            contactPersonArray.add(neueId1);

            String neueId2 = (String) o2.get("PERSON1");
            JSONObject node2 = new JSONObject();

            node2.put("id", neueId2);
            nodesArray.add(node2);
        }

        System.out.println("NODE ARRAY " + nodesArray);

        //build edges
        for (Object o : inputArray) {

            JSONObject o2 = (JSONObject) o;

            //String neueId1 = ((JSONObject) o).getString("PERSON2");
            String neueId1 = (String) o2.get("PERSON2");
            String neueId2 = (String) o2.get("PERSON1");
            JSONObject edge = new JSONObject();
            edge.put("from", neueId1);
            edge.put("to", neueId2);

            edgesArray.add(edge);
        }

        JSONObject obj = new JSONObject();

        obj.put("nodes", nodesArray);
        obj.put("edges", edgesArray);

        System.out.println("JSON OBJECT " + obj);
        System.out.println("JSON OBJECT TO STRING " + obj.toString());

        //JSONObject out = getContactPersons(obj);
        return obj.toString();
    }

    /**
     * INPUT JSON DIREKT AUS DB
     */
    public JSONObject getContactPersons(JSONObject jsonStr) throws Exception {
        System.out.println("CONVERTER INPUT " + jsonStr);
        JSONObject layer0Output = jsonStr;

        String newjsonStr = jsonStr.toString();

        JSONParser parser = new JSONParser();
        JSONArray inputArray = (JSONArray) parser.parse(newjsonStr);

        System.out.println("NACH PARSEN " + inputArray);

        JSONArray contactPersonArray = new JSONArray();

        //build nodes
        for (Object o : inputArray) {

            JSONObject o2 = (JSONObject) o;

            //String neueId1 = ((JSONObject) o).getString("PERSON2");
            String neueId1 = (String) o2.get("PERSON2");
            System.out.println("PERSON 2 ziehen" + (String) o2.get("PERSON2") + neueId1);

            JSONObject contactObj = new JSONObject();
            contactObj.put("ContactName", neueId1);

            contactPersonArray.add(contactObj);
        }

        JSONObject out = getSecondLayer(contactPersonArray, layer0Output);
        return (out);
    }


    public JSONObject getSecondLayer(JSONArray nodesLayer1, JSONObject layer0Ausgabe) throws Exception //INPUT CONTACT PERSON ARRAY
    {
        MySQLconnect con = new MySQLconnect();
        System.out.println("Connector erstellt");

        JSONArray alleDatenLayer2 = new JSONArray();

        for (Object o : nodesLayer1) {
            JSONObject o2 = (JSONObject) o;
            String nextPerson = (String) o2.get("ContactName");

            String jsonout = con.getRelationships(nextPerson); //hole Beziehungen aus DB
            String jsonProcessed = converter(jsonout); //get Objekt mit nodes Array und egdes Array
            alleDatenLayer2.add(jsonProcessed);
        }

        JSONArray allNodes = new JSONArray();
        JSONArray allEdges = new JSONArray();

        //alleDatenLayer2 IST ZUSAMMENGEFUEGT AUS AUS CONVERTER OUTPUTS
        for (Object o : alleDatenLayer2) {
            JSONObject o2 = (JSONObject) o;
            JSONArray nodesInO = (JSONArray) o2.get("nodes");
            JSONArray edgesInO = (JSONArray) o2.get("edges");

            for (Object p : nodesInO) {
                String nextPerson = (String) o2.get("id");

                JSONObject lulu = new JSONObject();
                lulu.put("id", nextPerson);
                allNodes.add(lulu);
            }

            for (Object p : edgesInO) {
                String fromPerson = (String) o2.get("from");
                String toPerson = (String) o2.get("to");

                JSONObject lala = new JSONObject();
                lala.put("from", fromPerson);
                lala.put("to", toPerson);
                allEdges.add(lala);
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

        for (Object p : nodesInLayer0) {
            String nextPerson = (String) o2.get("id");

            JSONObject lulu = new JSONObject();
            lulu.put("id", nextPerson);
            nodesComplete.add(lulu);
        }

        for (Object p : edgesInLayer0) {
            String fromPerson = (String) o2.get("from");
            String toPerson = (String) o2.get("to");

            JSONObject lala = new JSONObject();
            lala.put("from", fromPerson);
            lala.put("to", toPerson);
            edgesComplete.add(lala);
        }

        for (Object p : nodesInLayer1) {
            String nextPerson = (String) o2.get("id");

            JSONObject lulu = new JSONObject();
            lulu.put("id", nextPerson);
            nodesComplete.add(lulu);
        }

        for (Object p : edgesInLayer1) {
            String fromPerson = (String) o2.get("from");
            String toPerson = (String) o2.get("to");

            JSONObject lala = new JSONObject();
            lala.put("from", fromPerson);
            lala.put("to", toPerson);
            edgesComplete.add(lala);
        }

        JSONObject out = new JSONObject();
        out.put("nodes", nodesComplete);
        out.put("edges", edgesComplete);

        return (out);
    }

    // users --> getWatson --> Converter --> ContactPerson --> getSecondLayer --> MergeLayer0AndLayer1 --> return to users
}

