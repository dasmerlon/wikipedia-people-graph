anychart.onDocumentReady(function () {
    anychart.data.loadJsonFile('http://localhost:8080/JSONs/testdaten.json', function (data) {


        // create a chart from the loaded data
        var chart = anychart.graph(data);

        // legt den Titel des Graohen fest
        chart.title("People Graph");

        // sorgt dafür das Einstellungen an den Knoten vorgenommen werden können
        var nodes = chart.nodes();

        // Größe der Knoten
        nodes.normal().height(30);
        nodes.hovered().height(45);
        nodes.selected().height(45);

        // Umrandung der Knoten
        nodes.normal().stroke(null);
        nodes.hovered().stroke("#333333", 3);
        nodes.selected().stroke("#333333", 3);

        // Einschalten der labels (Bildunterschrift unter den Knoten)
        chart.nodes().labels().enabled(true);

        // Einstellungen für die labels
        // Woher der Text für die Bildunterschrift stammt
        chart.nodes().labels().format("{%id}");
        // Einstellungen für die Schrift
        chart.nodes().labels().fontSize(12);
        chart.nodes().labels().fontWeight(600);

        // erstellt das Chart
        chart.container("container");
        chart.draw();
    });

});
