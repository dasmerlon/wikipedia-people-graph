  //Variablen, die den Inhalt der Timeline definieren und durch den User verändert werden können
  var persons = "";
  var firstdate = "";
  var enddate = "";
  var profession = "Politics";
  var startsWith = "";

  var chart;

  //beim initialen Laden Seite wird die Timeline mit default-Werten gebaut
  anychart.onDocumentReady(buildTimeline());

  //Erstellt die Timeline in der "container"-div
  function buildTimeline() {

    var container = document.getElementById("container");
    container.innerHTML = "" ;

    anychart.data.loadJsonFile('/filter' + '?person=' + persons + '&birthdate=' + firstdate + '&deathdate=' + enddate + '&job=' + profession + '&startsWith=' + startsWith,   function (data) {


    // set the input date/time format
    anychart.format.inputDateTimeFormat("G-y-MM-dd");

    // set the output date/time format
    anychart.format.outputDateTimeFormat("G d MMMM y");

    // create a data tree
    var treeData = anychart.data.tree(data, "as-tree");

    //Mapping von SQL-Spalten auf anychart-gantt.min.js variablen
    var mapping = treeData.mapAs({name: "TITLE", actualStart: "BIRTH_DATE", actualEnd: "DEATH_DATE"});

    // create a chart
    chart = anychart.ganttProject(data);

    // disable the first data grid column
    chart.dataGrid().column(0).enabled(false);

    // configure chart design
    chart.background("#64b5f6 0.2");
    chart.rowHoverFill("#ffd54f 0.3");
    chart.rowSelectedFill("#ffd54f 0.3");
    chart.rowStroke("0.5 #64b5f6");
    chart.columnStroke("0.5 #64b5f6");

    // configure task design
    var tasks = chart.getTimeline().tasks();
    tasks.normal().fill("#455a64 1.0");
    tasks.selected().fill("#dd2c00");
    tasks.normal().stroke("#455a64");
    tasks.selected().stroke("#dd2c00");

    // disable labels of tasks
    chart.getTimeline().tasks().labels().enabled(false);

    // configure the height of tasks
    chart.getTimeline().tasks().height(35);

    // configure milestone design
    var milestones = chart.getTimeline().milestones();
        milestones.normal().fill("#455a64 1.0");  // #ffff05 = gelb
        milestones.selected().fill("#dd2c00");
        milestones.normal().stroke("#455a64");
        milestones.selected().stroke("#dd2c00");

     // disable labels of milestones
        chart.getTimeline().milestones().labels().enabled(false);

    // set the position of the splitter
    chart.splitterPosition("20%");
    chart.dataGrid().column(0).width('0%');
    chart.dataGrid().column(1).width('100%');



    /* listen to the rowClick event
    and update the Info Box (TextDisplay) */
    chart.listen("rowClick", function (e) {

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
            var era = e.item.get("BIRTH_DATE").split("-",1);
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
            var era = e.item.get("DEATH_DATE").split("-",1);
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
    });


    // configure the visual settings of the data grid
    var dataGrid = chart.dataGrid();
    dataGrid.rowEvenFill("gray 0.3");
    dataGrid.rowOddFill("gray 0.1");
    dataGrid.rowHoverFill("#ffd54f 0.3");
    dataGrid.rowSelectedFill("#ffd54f 0.3");
    dataGrid.columnStroke("2 #64b5f6");
    dataGrid.headerFill("#64b5f6 0.2");

    // set the row height
    chart.defaultRowHeight(35);

    // set the header height
    chart.headerHeight(40);


    // configure tooltips of the data grid
    chart.dataGrid().tooltip().useHtml(true);
    chart.dataGrid().tooltip().format(
     "<span style='font-weight:300;font-size:10pt'>" + "Born on " +
                    "{%actualStart}{dateTimeFormat:d MMMM y G}<br>"  + "Died on " +
                    "{%actualEnd}{dateTimeFormat:d MMMM y G}</span>"

      );


    // configure tooltips of the timeline
    chart.getTimeline().tooltip().useHtml(true);
    chart.getTimeline().tooltip().format(
           "<span style='font-weight:300;font-size:10pt'>" + "Born on " +
                "{%actualStart}{dateTimeFormat:d MMMM y G}<br>"  + "Died on " +
                "{%actualEnd}{dateTimeFormat:d MMMM y G}</span>"

      );


    // set the data
    chart.data(mapping);


    // set the minimum and maximum values of the scale
    chart.getTimeline().scale().maximum("2022-01-01");

    // set the container id
    chart.container("container");

    // set zoom levels of the scale
    chart.getTimeline().scale().zoomLevels([
      [
        {unit: "year", count: 50},
        {unit: "year", count: 10},
        {unit: "year", count: 1}

      ]
    ]);

    // initiate drawing the chart
    chart.draw();

    // fit elements to the width of the timeline
    chart.fitAll();
        });
 };

// zoom the timeline in
 function zoomIn() {
   chart.zoomIn(2);
 }

 // zoom the timeline out
 function zoomOut() {
   chart.zoomOut(2);
 }

//Aktualisiert die Variablen mit den eingegebenen Werten aus den Filter-Feldern und dem ausgewählten Buchstaben. Baut Timeline neu (Wird ausgeführt, bei Click auf Buchstaben-Navigation)
function charClick() {
    startsWith = event.srcElement.id;
    persons = document.getElementById("PersonInput").value;
    firstdate = document.getElementById("LivedFromInput").value;
    enddate = document.getElementById("LivedUntilInput").value;
    profession = document.getElementById("ProfessionInput").value;
    buildTimeline();
}

//Aktualisiert die Variablen mit den eingegebenen Werten aus den Filter-Feldern und baut Timeline neu (Wird ausgeführt, bei Click auf "Search!")
function getSubmitFields() {
  persons = document.getElementById("PersonInput").value;
  firstdate = document.getElementById("LivedFromInput").value;
  enddate = document.getElementById("LivedUntilInput").value;
  profession = document.getElementById("ProfessionInput").value;
  startsWith = '';
  buildTimeline();
}
