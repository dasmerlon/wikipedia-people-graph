  var persons = "Angela Merkel";
  var firstdate = "";
  var enddate = "";
  var profession = "";

anychart.onDocumentReady(buildeMal());

 function buildeMal() {
    //anychart.data.loadJsonFile('http://localhost:8080/JSONs/testdaten.json', function (data) {


        var container = document.getElementById("container");
        container.innerHTML = "" ;


    anychart.data.loadJsonFile('/graph_data' + '?person=' + persons , function (data) {

        // create a chart from the loaded data
        var chart = anychart.graph(data);

        // legt den Titel des Graohen fest
        chart.title("People Graph");

        // sorgt dafür das Einstellungen an den Knoten vorgenommen werden können
        var nodes = chart.nodes();

        // Größe der Knoten
        nodes.normal().height(3);
        nodes.hovered().height(4);
        nodes.selected().height(4);

        // set the fill of nodes
        nodes.normal().fill("#ffa000"); //ORANGE  // #ffa000
        nodes.hovered().fill("white");
        nodes.selected().fill("#ffa000");



        // Umrandung der Knoten
        nodes.normal().stroke(null);
        nodes.hovered().stroke("#333333", 3);
        nodes.selected().stroke("#333333", 3);

        // Einschalten der labels (Bildunterschrift unter den Knoten)
        chart.nodes().labels().enabled(true);

        // enable the alignment of nodes
        chart.interactivity().magnetize(true);

        // set the iteration step Setzt Anzahl der kanten die maximal gerendert werden, beeinflusst die Ladezeit stark
        chart.layout().iterationCount(500);

        // Einstellungen für die labels
        // Woher der Text für die Bildunterschrift stammt
        chart.nodes().labels().format("{%id}");
        // Einstellungen für die Schrift
        chart.nodes().labels().fontSize(2);
        chart.nodes().labels().fontWeight(400);

        // configure the visual settings of edges
        chart.edges().normal().stroke("#33ADFF", 0.1);
        chart.edges().hovered().stroke("#33ADFF", 2);
        chart.edges().selected().stroke("#33ADFF", 2);
/*
        chart.edges().normal().fill("#ffa000", 2, "10 5", "round");
        chart.edges().hovered().fill("#ffa000", 4, "10 5", "round");
        chart.edges().selected().fill("#ffa000", 4);*/

       // configure tooltips
       chart.tooltip().useHtml(true);
       chart.tooltip().format(function() {
         if (this.type == "node") {
           return "<span style='font-weight:bold'>" +
                  this.id +
                  "</span><br><br>Connections: " + this.siblings.length ;
         } else {
           return this.getData("from") + " -> " + this.getData("to");
         }});


        // erstellt das Chart
        chart.container("container");
        chart.draw();
    });

    chart.invalidate();

};

function getSubmitFields() {
  persons = document.getElementById("PersonInput").value;
  firstdate = document.getElementById("LivedFromInput").value;
  enddate = document.getElementById("LivedUntilInput").value;
  profession = document.getElementById("ProfessionInput").value;
  buildeMal();
}