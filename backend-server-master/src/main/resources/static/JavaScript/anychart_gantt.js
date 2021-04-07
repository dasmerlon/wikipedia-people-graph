  var persons = "";
  var firstdate = "";
  var enddate = "";
  var profession = "Politics";
  var startsWith = "";

  var chart;
  //anychart.onDocumentReady(function buildeMal() {

  anychart.onDocumentReady(buildeMal());

  function buildeMal() {

    // create data

/*      var data = [
             {
               id: "1",
               name: "Dschingis Khan",
               birth_date: "1125-02-07",
               death_date: "1213-01-05",
               info: "Dischingis Khan konnte ganz gut mit Bögen und Pferden."
             },
             {
               id: "2",
               name: "Mahad Ma Gandi",
               birth_date: "1920-09-11",
               death_date: "1980-08-07",
               info: "Hat so viel Yoga gemacht, dass es für ne Revolution reichte."
             },
             {

             id: "3",
             name: "Rainer Kallmund",
             birth_date: "1970-10-11",
             info: "Rainer Kallmund ist ein deutscher Fußballfunktionär und Lebemann."


             }
           ]*/


    /*var data = (function() {
                 var json = null;
                 $.ajax({
                   'async': true,
                   'global': false,
                   'url': "JSONs/test.json",
                   'dataType': "json",
                   'success': function(data) {
                     json = data;
                   }
                 });
                 return json;
               })();*/

 /*   var name = "Eugen";
    anychart.data.loadJsonFile('/users/?name=' + name,   function (data) {*/





    var container = document.getElementById("container");
    container.innerHTML = "" ;

    // ruft users, die users Methode im FilterController auf?
    anychart.data.loadJsonFile('/users' + '?person=' + persons + '&birthdate=' + firstdate + '&deathdate=' + enddate + '&job=' + profession + '&startsWith=' + startsWith,   function (data) {


    // set the input date/time format
    anychart.format.inputDateTimeFormat("G-y-MM-dd");

    //anychart.format.inputDateTimeFormat("yyyy");


    // set the output date/time format
    //anychart.format.outputDateTimeFormat("yyyy");
    anychart.format.outputDateTimeFormat("G d MMMM y");


    // create a data tree
    var treeData = anychart.data.tree(data, "as-tree");

    var mapping = treeData.mapAs({name: "TITLE", actualStart: "BIRTH_DATE", actualEnd: "DEATH_DATE"}); //evtl. NAME statt TITLE?
    // var mapping = treeData.mapAs({name: "Name", actualStart: "Birthdate", actualEnd: "Deathdate"}); //evtl. NAME statt TITLE?

    // create a chart
    chart = anychart.ganttProject(data);

    // disable the first data grid column
    chart.dataGrid().column(0).enabled(false);

    //CLICKIBUNTI
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
    and update the chart title */
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


       chart.getTimeline().tooltip().useHtml(true);
       chart.getTimeline().tooltip().format(
               "<span style='font-weight:300;font-size:10pt'>" + "Born on " +
                    "{%actualStart}{dateTimeFormat:d MMMM y G}<br>"  + "Died on " +
                    "{%actualEnd}{dateTimeFormat:d MMMM y G}</span>"

       );


    // set the data
    chart.data(mapping); //mapping oder treedata als input, liefert




// set the minimum and maximum values of the scale
    //chart.getTimeline().scale().minimum("0100-01-01");
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

     //chart.invalidate(); // scheint auch ohne zu funktionieren





 };

 // zoom the timeline in
     function zoomIn() {
       chart.zoomIn(2);
     }

     // zoom the timeline out
     function zoomOut() {
       chart.zoomOut(2);
     }



// zoom the timeline to the given dates
function zoomToDates() {
  chart.zoomTo(Date.UTC(2018, 1, 3), Date.UTC(2018, 1, 6));
}

// zoom the timeline to the given units
function zoomToUnits() {
  var unit = document.getElementById("unitSelect").value;
  var count = document.getElementById("countInput").value;
  var anchor = document.getElementById("anchorSelect").value;
  chart.zoomTo(unit, count, anchor);
}

function charClick() {
    startsWith = event.srcElement.id;
    persons = document.getElementById("PersonInput").value;
    firstdate = document.getElementById("LivedFromInput").value;
    enddate = document.getElementById("LivedUntilInput").value;
    profession = document.getElementById("ProfessionInput").value;
    buildeMal();
}


// zoom the timeline to the given units
function getSubmitFields() {
  persons = document.getElementById("PersonInput").value;
  firstdate = document.getElementById("LivedFromInput").value;
  enddate = document.getElementById("LivedUntilInput").value;
  profession = document.getElementById("ProfessionInput").value;
  startsWith = '';
  buildeMal();
}
