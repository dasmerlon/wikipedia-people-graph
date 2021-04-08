package basecamp.project.server.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uhh_lt.datenbank.MySQLconnect;

/**
 * Stellt die filter-Methode bereit, die aus Networkgraph.js aufgerufen wird.
 */

@RestController
@RequestMapping("/graph_data")
public class GraphController {


    /**
     * Erstellt ein Objekt der Klasse MySQLconnect und nutzt dieses um die mit den Parametern gefilterten Beziehungsdaten-Daten von einer SQL-Datenbank abzurufen.
     *
     * @param  person  Eingegebener Name für Person
     * @param  secLayer Eingegebener Boolean-Wert, ob auch die Personen die die Personen kennen, welche die eingegebene Person (param person) kennen angezeigt werden
     * @return finaler JSON-String mit Graph-Daten zu gefilterten Beziehungen. Dient als Eingabe für die anychart-graph.min.js Library in Networkgraph.js.
     */

    @GetMapping
    public String filter(@RequestParam(value = "person", required = false, defaultValue = "") String person,
                        @RequestParam(value = "secLayer", required = false, defaultValue = "") Boolean secLayer) throws Exception {


        MySQLconnect con = new MySQLconnect();
        System.out.println("Connector erstellt");

        String jsonout = con.getRelationships(person);

        System.out.println("WERT SEC LAYER = " + secLayer);
        String jsonProcessed = converter(jsonout, secLayer); //vorher true

        return jsonProcessed;
    }



    /**
     * Bringt den Input-Json/String (param jsonStr) in das richtige Format für eine Verarbeitung in Networkgraph.js.
     * Durch den Aufruf weiterer Methoden aus dieser Klasse werden außerdem ggf. indirekte Beziehungen zur Person (zweite Ebene) im Ausgabe-Datenset ergänzt und formatiert.
     *
     * @param  jsonStr  JSON-String der von der MySQLconnect-Klasse erzeugt wird
     * @param  layer0 Boolean-Wert, der nur dann true ist wenn es sich beim param jsonStr um direkte Beziehungen zur eingegebenen Person handelt (erste Ebene).
     * @return finaler JSON-String mit Graph-Daten zu gefilterten Beziehungen (ggf. mit Beziehungen auf zweiter Ebene).
     */
    public String converter(String jsonStr, Boolean layer0) throws Exception {

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
            String neueId1 = (String) o2.get("PERSON2");
            JSONObject node1 = new JSONObject();

            node1.put("id", neueId1);
            nodesArray.put(node1);
            JSONObject contactObj = new JSONObject();
            contactObj.put("ContactName", neueId1);
            contactPersonArray.put(contactObj);

            String neueId2 = (String) o2.get("PERSON1");
            JSONObject node2 = new JSONObject();
            node2.put("id", neueId2);

            nodesArray.put(node2);

            inputArray.getJSONObject(i);
        }


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

        //build final JSON output

        JSONObject obj = new JSONObject();

        obj.put("nodes", nodesArray);

        obj.put("edges", edgesArray);


        System.out.println("JSON OBJECT " + obj);
        System.out.println("JSON OBJECT TO STRING " + obj.toString());
        System.out.println("contactPersonArray " + contactPersonArray);

        //Wenn weitere Beziehungsebene gezogen werden soll
        if (layer0 == true) {


            JSONObject out = getSecondLayer(contactPersonArray, obj);

            System.out.println("FINAL OUT" + out);
            return out.toString();
        }

        else {
            return obj.toString();
        }

    }


    /**
     * Zieht sich die Beziehungen zu den Beziehungspersonen aus erster Ebene (mit Hilfe der MySQLconnect Klasse).
     * Durch den Aufruf einer weiteren Methoden aus dieser Klasse (MergeLayer0AndLayer1) werden die Beziehungen aus dieser Ebene und der ersten Ebene(aus converter) anschließend zusammengengeführt.
     *
     * @param  nodesLayer1  JSON-Array, welcher die Kontaktpersonen zur gefilterten Person beinhaltet.
     * @param  layer0Ausgabe JSONObject, welches die finale Ausgabe der ersten Ebene (aus converter) beinhaltet (wird in MergeLayer0AndLayer1 benötigt).
     * @return finales JSON-Object mit zusammengeführten Beziehungsdaten aus erster und zweiter Ebene.
     */

    public JSONObject getSecondLayer(JSONArray nodesLayer1, JSONObject layer0Ausgabe) throws Exception //INPUT CONTACT PERSON ARRAY
    {
        MySQLconnect con = new MySQLconnect();
        System.out.println("Connector erstellt");
        JSONArray alleDatenLayer2 = new JSONArray();


        for (int i = 0; i < nodesLayer1.length(); i++) {
            JSONObject o2 = nodesLayer1.getJSONObject(i);
            String nextPerson = (String) o2.get("ContactName");

            //get relations from database
            String jsonout = con.getRelationships(nextPerson);
            String jsonProcessed = converter(jsonout, false);

            JSONObject processedobj = new JSONObject(jsonProcessed);
            alleDatenLayer2.put(processedobj);

        }


        JSONArray allNodes = new JSONArray();
        JSONArray allEdges = new JSONArray();


        //alleDatenLayer2 besteht aus mehreren converter Outputs
        //alleDatenLayer2 structure = [ { nodes:[{},{},{}], edges:[{},{},{}] } ,    { nodes:[{},{},{}], edges:[{},{},{}] } ]
        for (int i = 0; i < alleDatenLayer2.length(); i++) {
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



    }

    /**
     * Fügt die die beiden JSON-Objekte mit den Beziehungen aus Ebene 1 und aus Ebene 2 zu einem JSON-Objekt zusammen
     *
     * @param  NodesEdgesLayer1  JSON-Objekt, welches die Beziehungen auf der zweiten Ebene enthält, Structure = { nodes:[{},{},{}], edges:[{},{},{}] }
     * @param  NodesEdgesLayer0 JSON-Objekt, welches die Beziehungen auf der erster Ebene enthält, Structure = { nodes:[{},{},{}], edges:[{},{},{}] }
     * @return finales JSON-Object mit zusammengeführten Beziehungsdaten aus erster und zweiter Ebene.
     */

    public JSONObject MergeLayer0AndLayer1(JSONObject NodesEdgesLayer1, JSONObject NodesEdgesLayer0) throws JSONException, ParseException {

        JSONArray nodesComplete = new JSONArray();
        JSONArray edgesComplete = new JSONArray();

        JSONObject o2 = (JSONObject) NodesEdgesLayer0;
        JSONArray nodesInLayer0 = (JSONArray) o2.get("nodes");
        JSONArray edgesInLayer0 = (JSONArray) o2.get("edges");

        JSONObject o3 = (JSONObject) NodesEdgesLayer1;
        JSONArray nodesInLayer1 = (JSONArray) o3.get("nodes");
        JSONArray edgesInLayer1 = (JSONArray) o3.get("edges");


        //Kopiere alle nodes aus layer0 in finales nodes-Array
        for (int i = 0; i < nodesInLayer0.length(); i++) {

            String nextPerson = (String) nodesInLayer0.getJSONObject(i).get("id");

            JSONObject lulu = new JSONObject();
            lulu.put("id", nextPerson);
            nodesComplete.put(lulu);

        }

        //Kopiere alle edges aus layer0 in finales edges-Array
        for (int i = 0; i < edgesInLayer0.length(); i++) {

            String fromPerson = (String) edgesInLayer0.getJSONObject(i).get("from");
            String toPerson = (String) edgesInLayer0.getJSONObject(i).get("to");

            JSONObject lala = new JSONObject();
            lala.put("from", fromPerson);
            lala.put("to", toPerson);
            edgesComplete.put(lala);

        }

        //Kopiere alle nodes aus layer1 in finales nodes-Array
        for (int i = 0; i < nodesInLayer1.length(); i++) {

            String nextPerson = (String) nodesInLayer1.getJSONObject(i).get("id");

            JSONObject lulu = new JSONObject();
            lulu.put("id", nextPerson);
            nodesComplete.put(lulu);
        }


        //Kopiere alle edges aus layer0 in finales edges-Array
        for (int i = 0; i < edgesInLayer1.length(); i++) {

            String fromPerson = (String) edgesInLayer1.getJSONObject(i).get("from");
            String toPerson = (String) edgesInLayer1.getJSONObject(i).get("to");

            JSONObject lala = new JSONObject();
            lala.put("from", fromPerson);
            lala.put("to", toPerson);
            edgesComplete.put(lala);


        }

        //Erstelle finales JSONObject mit allen nodes und relations aus erster und zweiter Ebene
        JSONObject out = new JSONObject();
        out.put("nodes", nodesComplete);
        out.put("edges", edgesComplete);

        return (out);
    }

}

