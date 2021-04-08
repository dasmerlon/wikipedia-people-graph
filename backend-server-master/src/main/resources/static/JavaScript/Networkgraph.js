//Variablen, die den Inhalt des Netwerk-Graph definieren und durch den User verändert werden können
  var persons = "Albert Einstein";
  secondLayer = false;


//beim initialen Laden Seite wird der Graph mit default-Werten gebaut
anychart.onDocumentReady(buildGraph());

//Erstellt den Graphen in der "container"-div
 function buildGraph() {


        var container = document.getElementById("container");
        container.innerHTML = "" ;


    anychart.data.loadJsonFile('/graph_data' + '?person=' + persons + '&secLayer=' + secondLayer, function (data) {

        // create a chart from the loaded data
        var chart = anychart.graph(data);


        // legt den Titel des Graohen fest
        chart.title("Use mouse wheel to zoom, click to highlight connections");

        // sorgt dafür das Einstellungen an den Knoten vorgenommen werden können
        var nodes = chart.nodes();

        // Größe der Knoten
        nodes.normal().height(6);
        nodes.hovered().height(7);
        nodes.selected().height(7);

        // set the fill of nodes
        nodes.normal().fill("#455a64");  // #ffa000 = Orange
        nodes.hovered().fill("#333333", 3);
        nodes.selected().fill("#dd2c00", 3);

        // Umrandung der Knoten
        nodes.normal().stroke(null);
        nodes.hovered().stroke("#333333", 3);
        nodes.selected().stroke("#dd2c00", 3);

        // Einschalten der labels (Bildunterschrift unter den Knoten)
        chart.nodes().labels().enabled(true);

        // enable the alignment of nodes
        chart.interactivity().magnetize(true);

        // set the iteration step, Setzt Anzahl der kanten die maximal gerendert werden, beeinflusst die Ladezeit stark
        chart.layout().iterationCount(500);

        // Einstellungen für die labels
        // Woher der Text für die Bildunterschrift stammt
        chart.nodes().labels().format("{%id}");
        // Einstellungen für die Schrift
        chart.nodes().labels().fontSize(7);
        chart.nodes().labels().fontWeight(600);

        // configure the visual settings of edges
        chart.edges().normal().stroke("#64B5F6", 0.5);
        chart.edges().hovered().stroke("#64B5F6", 2);
        chart.edges().selected().stroke("#64B5F6", 1.5);

       // configure tooltips
       chart.tooltip().useHtml(true);
       chart.tooltip().format(function() {
         if (this.type == "node") {
           return "<span style='font-weight:bold'>" +
                  this.id +
                  "</span><br><br>Connections: " + this.siblings.length ;
         } else {
           return this.getData("to") + " -> " + this.getData("from");
         }});


        //wähle Person, die eingegeben wurde aus und hebe sie damit hervor
        chart.select([persons]);
        chart.fit();


        // erstellt das Chart
        chart.container("container");
        chart.draw();
    });

    chart.invalidate();

};


//Aktualisiert die Variablen mit den eingegebenen Werten aus dem Personen-Suchfeld und der Checkbox und baut neuen Graphen
function getSubmitFields() {
  persons = document.getElementById("PersonInput").value;
  secondLayer = document.getElementById("layerCheckbox").checked;
  buildGraph();
}