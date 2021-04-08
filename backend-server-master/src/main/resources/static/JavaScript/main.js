//Variablen, die den Inhalt der Timeline definieren und durch den User verändert werden können
var relatedPersonsOnly = false;
var secondLayer = false;
var persons = "Einstein";
var firstdate = "";
var enddate = "";
var profession = "";
var startsWith = "";

var timelineChart;
var graphChart;

// Beim initialen Laden Seite wird die Timeline mit default-Werten gebaut
anychart.onDocumentReady(buildTimeline());

// Erstellt die Timeline in der "timelineContainer"-div
function buildTimeline() {
    var container = document.getElementById("timelineContainer");
    container.innerHTML = "";

    // Wir operieren in zwei Modi.
    // Wenn `realtedPersonsOnly === true`, dann werden die Personen angezeigt, die zur momentan ausgewählten Person related sind.
    // Andernfalls werden alle Personen angezeigt, die auf die momentanen Form-Filterkriteren zutreffen.
    let url;
    if (relatedPersonsOnly) {
        url = '/relatedPersons' + '?person=' + persons + '&startsWith=' + startsWith;
    } else {
        url ='/persons' + '?person=' + persons + '&birthdate=' + firstdate + '&deathdate=' + enddate + '&job=' + profession + '&startsWith=' + startsWith;
    }

    anychart.data.loadJsonFile(url, function (data) {

        // set the input date/time format
        anychart.format.inputDateTimeFormat("G-y-MM-dd");

        // set the output date/time format
        anychart.format.outputDateTimeFormat("G d MMMM y");

        // create a data tree
        var treeData = anychart.data.tree(data, "as-tree");

        //Mapping von SQL-Spalten auf anychart-gantt.min.js variablen
        var mapping = treeData.mapAs({name: "TITLE", actualStart: "BIRTH_DATE", actualEnd: "DEATH_DATE"});

        // create a chart
        timelineChart = anychart.ganttProject(data);
        // disable the first data grid column
        timelineChart.dataGrid().column(0).enabled(false);

        // configure chart design
        timelineChart.background("#64b5f6 0.2");
        timelineChart.rowHoverFill("#ffd54f 0.3");
        timelineChart.rowSelectedFill("#ffd54f 0.3");
        timelineChart.rowStroke("0.5 #64b5f6");
        timelineChart.columnStroke("0.5 #64b5f6");

        // configure task design
        var tasks = timelineChart.getTimeline().tasks();
        tasks.normal().fill("#455a64 1.0");
        tasks.selected().fill("#dd2c00");
        tasks.normal().stroke("#455a64");
        tasks.selected().stroke("#dd2c00");

        // disable labels of tasks
        timelineChart.getTimeline().tasks().labels().enabled(false);
        // configure the height of tasks
        timelineChart.getTimeline().tasks().height(35);

        // configure milestone design
        var milestones = timelineChart.getTimeline().milestones();
        milestones.normal().fill("#455a64 1.0");  // #ffff05 = gelb
        milestones.selected().fill("#dd2c00");
        milestones.normal().stroke("#455a64");
        milestones.selected().stroke("#dd2c00");

        // disable labels of milestones
        timelineChart.getTimeline().milestones().labels().enabled(false);
        // set the position of the splitter
        timelineChart.splitterPosition("20%");
        timelineChart.dataGrid().column(0).width('0%');
        timelineChart.dataGrid().column(1).width('100%');

        // configure the visual settings of the data grid
        var dataGrid = timelineChart.dataGrid();
        dataGrid.rowEvenFill("gray 0.3");
        dataGrid.rowOddFill("gray 0.1");
        dataGrid.rowHoverFill("#ffd54f 0.3");
        dataGrid.rowSelectedFill("#ffd54f 0.3");
        dataGrid.columnStroke("2 #64b5f6");
        dataGrid.headerFill("#64b5f6 0.2");

        // set the row height
        timelineChart.defaultRowHeight(35);
        // set the header height
        timelineChart.headerHeight(40);

        // configure tooltips of the data grid
        timelineChart.dataGrid().tooltip().useHtml(true);
        timelineChart.dataGrid().tooltip().format(
            "<span style='font-weight:300;font-size:10pt'>" + "Born on " +
            "{%actualStart}{dateTimeFormat:d MMMM y G}<br>" + "Died on " +
            "{%actualEnd}{dateTimeFormat:d MMMM y G}</span>"
        );

        // configure tooltips of the timeline
        timelineChart.getTimeline().tooltip().useHtml(true);
        timelineChart.getTimeline().tooltip().format(
            "<span style='font-weight:300;font-size:10pt'>" + "Born on " +
            "{%actualStart}{dateTimeFormat:d MMMM y G}<br>" + "Died on " +
            "{%actualEnd}{dateTimeFormat:d MMMM y G}</span>"
        );

        // set the data
        timelineChart.data(mapping);
        // set the minimum and maximum values of the scale
        timelineChart.getTimeline().scale().maximum("2022-01-01");
        // set the container id
        timelineChart.container("timelineContainer");

        // set zoom levels of the scale
        timelineChart.getTimeline().scale().zoomLevels([
            [
                {unit: "year", count: 50},
                {unit: "year", count: 10},
                {unit: "year", count: 1}

            ]
        ]);

        // initiate drawing the chart
        timelineChart.draw();

        // fit elements to the width of the timeline
        timelineChart.fitAll();

        /* listen to the rowClick event and update the Info Box (TextDisplay) */
        timelineChart.listen("rowClick", function (e) {
            updatePersonalInformationBox(e);

            // Reload the chart with all persons that are related to the selected person
            relatedPersonsOnly = true;
            startsWith = '';
            persons = e.item.get("TITLE");
            buildTimeline();
            secondLayer = document.getElementById("layerCheckbox").checked;
            buildGraph();
        });
    });
};

// zoom the timeline in
function zoomIn() {
    timelineChart.zoomIn(2);
}

// zoom the timeline out
function zoomOut() {
    timelineChart.zoomOut(2);
}

// Aktualisiert die Variablen mit den eingegebenen Werten aus den Filter-Feldern und dem ausgewählten Buchstaben. Baut Timeline neu (Wird ausgeführt, bei Click auf Buchstaben-Navigation)
function charClick() {
    startsWith = event.srcElement.id;
    buildTimeline();
}

// Aktualisiert die Variablen mit den eingegebenen Werten aus den Filter-Feldern und baut Timeline neu (Wird ausgeführt, bei Click auf "Search!")
function getSubmitFields() {
    persons = document.getElementById("PersonInput").value;
    firstdate = document.getElementById("LivedFromInput").value;
    enddate = document.getElementById("LivedUntilInput").value;
    profession = document.getElementById("ProfessionInput").value;
    startsWith = '';
    relatedPersonsOnly = false;
    buildTimeline();
}


// This function is called everytime a row is clicked on.
// The event will be caught and we'll display all information of that person in the info box to the right.
function updatePersonalInformationBox(e) {
    var months = [
        'January',
        'February',
        'March',
        'April',
        'May',
        'June',
        'July',
        'August',
        'September',
        'October',
        'November',
        'December'
    ]

    var innerHTML = '<h4>' + e.item.get("TITLE") + "</h4>";

    if (e.item.get("IMAGE") == "NONE") {
        innerHTML += "<br />";
        innerHTML += '<img class="img" src="https://www.pngitem.com/pimgs/m/99-998739_dale-engen-person-placeholder-hd-png-download.png" width="500" height="500">';
    } else {
        innerHTML += "<br />";
        innerHTML += '<img class="img" src="' + e.item.get("IMAGE") + '"width="500" height="500">';
    }

    if (e.item.get("LINK") != "NONE") {
        innerHTML += "<br />";
        innerHTML += '<a href="' + e.item.get("LINK") + '"><h6>Wiki page</h6></a>';
    }

    if (e.item.get("SHORT_DESCRIPTION") != "NONE") {
        innerHTML += "<br />";
        innerHTML += "<b>Short Description</b><br/>";
        innerHTML += "<span>" + e.item.get("SHORT_DESCRIPTION") + "<span>";
    }

    if (e.item.get("BIRTH_DATE") != "NONE") {
        innerHTML += "<br />";
        innerHTML += "<b>Born</b><br/>";

        var date = e.item.get("BIRTH_DATE").substring(3);
        var era = e.item.get("BIRTH_DATE").split("-", 1);
        var splitDate = date.split("-");

        if (splitDate.length == 3) {
            monthIndex = splitDate[1] - 1;
            month = months[monthIndex];
            var newDate = splitDate[2] + " " + month + " " + splitDate[0] + " " + era;
        } else {
            var newDate = splitDate[0] + " " + era;
        }
        innerHTML += "<span>" + newDate + "<span>";
    }

    if (e.item.get("BIRTH_PLACE") != "NONE") {
        innerHTML += "<br />";
        if (e.item.get("BIRTH_DATE") == "NONE") {
            innerHTML += "<b>Born</b><br/>";
        }
        innerHTML += "<span>" + e.item.get("BIRTH_PLACE") + "<span>";
    }

    if (e.item.get("DEATH_DATE") != "NONE") {
        innerHTML += "<br />";
        innerHTML += "<b>Died</b><br/>";

        var date = e.item.get("DEATH_DATE").substring(3);
        var era = e.item.get("DEATH_DATE").split("-", 1);
        var splitDate = date.split("-");

        if (splitDate.length == 3) {
            monthIndex = splitDate[1] - 1;
            month = months[monthIndex];
            var newDate = splitDate[2] + " " + month + " " + splitDate[0] + " " + era;
        } else {
            var newDate = splitDate[0] + " " + era;
        }
        innerHTML += "<span>" + newDate + "<span>";
    }

    if (e.item.get("DEATH_PLACE") != "NONE") {
        innerHTML += "<br />";
        if (e.item.get("DEATH_DATE") == "NONE") {
            innerHTML += "<b>Died</b><br/>";
        }
        innerHTML += "<span>" + e.item.get("DEATH_PLACE") + "<span>";
    }

    if (e.item.get("OCCUPATION") != "NONE") {
        innerHTML += "<br />";
        innerHTML += "<b>Occupation</b><br/>";
        innerHTML += "<span>" + e.item.get("OCCUPATION") + "<span>";
    }
    document.getElementById("TextDisplay").innerHTML = innerHTML;
}

///// ----------- Graph Logic ----------- /////

// Erstellt den Graphen im "graphContainer"-div
function buildGraph() {
    var container = document.getElementById("graphContainer");
    container.innerHTML = "";

    anychart.data.loadJsonFile('/graph_data' + '?person=' + persons + '&secLayer=' + secondLayer, function (data) {
        // create a chart from the loaded data
        graphChart = anychart.graph(data);

        // legt den Titel des Graohen fest
        graphChart.title("Use mouse wheel to zoom, click to highlight connections");

        // sorgt dafür das Einstellungen an den Knoten vorgenommen werden können
        var nodes = graphChart.nodes();

        // Größe der Knoten
        nodes.normal().height(4);
        nodes.hovered().height(5)
        nodes.selected().height(5);

        // set the fill of nodes
        nodes.normal().fill("#455a64");  // #ffa000 = Orange
        nodes.hovered().fill("#333333", 3);
        nodes.selected().fill("#dd2c00", 3);

        // Umrandung der Knoten
        nodes.normal().stroke(null);
        nodes.hovered().stroke("#333333", 3);
        nodes.selected().stroke("#dd2c00", 3);

        // Einschalten der labels (Bildunterschrift unter den Knoten)
        graphChart.nodes().labels().enabled(true);

        // enable the alignment of nodes
        graphChart.interactivity().magnetize(true);

        // set the iteration step, Setzt Anzahl der kanten die maximal gerendert werden, beeinflusst die Ladezeit stark
        graphChart.layout().iterationCount(500);

        // Einstellungen für die labels
        // Woher der Text für die Bildunterschrift stammt
        graphChart.nodes().labels().format("{%id}");
        // Einstellungen für die Schrift
        graphChart.nodes().labels().fontSize(5);
        graphChart.nodes().labels().fontWeight(600);

        // configure the visual settings of edges
        graphChart.edges().normal().stroke("#64B5F6", 0.5);
        graphChart.edges().hovered().stroke("#64B5F6", 2);
        graphChart.edges().selected().stroke("#64B5F6", 1.5);

        // configure tooltips
        graphChart.tooltip().useHtml(true);
        graphChart.tooltip().format(function () {
            if (this.type == "node") {
                return "<span style='font-weight:bold'>" +
                    this.id +
                    "</span><br><br>Connections: " + this.siblings.length;
            } else {
                return this.getData("to") + " -> " + this.getData("from");
            }
        });

        // Wähle Person, die eingegeben wurde aus und hebe sie damit hervor
        graphChart.select([persons]);
        graphChart.fit();

        // Erstellt das Chart
        graphChart.container("graphContainer");
        graphChart.draw();
    });

    // Invalidate any previous graphChart instance
    if (typeof graphChart === 'object' && graphChart.hasOwnProperty('invalidate')) {
        graphChart.invalidate();
    }
}

// Listen on the layer checkbox and refresh the graph on change
document.getElementById("layerCheckbox").addEventListener("change", function (e) {
    secondLayer = this.checked;
    buildGraph();
});
