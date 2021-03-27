  var persons = "";
  var firstdate = "";
  var enddate = "";
  var profession = "Politics";

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
    anychart.data.loadJsonFile('http://localhost:8080/users/?name=' + name,   function (data) {*/





    var container = document.getElementById("container");
    container.innerHTML = "" ;

    // ruft users, die users Methode im FilterController auf?
    //anychart.data.loadJsonFile('http://localhost:8080/users/' + '?person=' + persons + '?birthdate=' + firstdate + '?deathdate=' + enddate + '?job=' + profession,   function (data) {
    anychart.data.loadJsonFile('http://localhost:8080/users' + '?person=' + persons + '&birthdate=' + firstdate + '&deathdate=' + enddate + '&job=' + profession,   function (data) {


    // set the input date/time format
    anychart.format.inputDateTimeFormat("G-yyyy-MM-dd");

    //anychart.format.inputDateTimeFormat("yyyy");


    // set the output date/time format
    //anychart.format.outputDateTimeFormat("yyyy");
    anychart.format.outputDateTimeFormat("G d MMMM yyyy");


    // create a data tree
    var treeData = anychart.data.tree(data, "as-tree");

    var mapping = treeData.mapAs({name: "TITLE", actualStart: "BIRTH_DATE", actualEnd: "DEATH_DATE"}); //evtl. NAME statt TITLE?
    // var mapping = treeData.mapAs({name: "Name", actualStart: "Birthdate", actualEnd: "Deathdate"}); //evtl. NAME statt TITLE?

    // create a chart
    chart = anychart.ganttProject(data);

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
        milestones.normal().fill("#ffff05 1.0");
        milestones.selected().fill("#dd2c00");
        milestones.normal().stroke("#000000");
        milestones.selected().stroke("#000000");

     // disable labels of milestones
        chart.getTimeline().milestones().labels().enabled(false);

    // set the position of the splitter
    chart.splitterPosition("17%");

    /* listen to the rowClick event
    and update the chart title */
    chart.listen("rowClick", function (e) {
      var itemName = e.item.get("TITLE") + "<br />Profession: " + e.item.get("OCCUPATION") + " <br /> Short Description: " + e.item.get("SHORT_DESCRIPTION");
      var imgLink = e.item.get("IMAGE");



      document.getElementById("TextDisplay").innerHTML = itemName;

      document.getElementById("PictureDisplay").src= imgLink;




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
                    "{%actualStart}{dateTimeFormat:d MMMM yyyy}<br>"  + "Died on " +
                    "{%actualEnd}{dateTimeFormat:d MMMM yyyy}</span>"

      );


       chart.getTimeline().tooltip().useHtml(true);
       chart.getTimeline().tooltip().format(
               "<span style='font-weight:300;font-size:10pt'>" + "Born on " +
                    "{%actualStart}{dateTimeFormat:G d MMMM yyyy}<br>"  + "Died on " +
                    "{%actualEnd}{dateTimeFormat:G d MMMM yyyy}</span>"

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

// zoom the timeline to the given units
function getSubmitFields() {
  persons = document.getElementById("PersonInput").value;
  firstdate = document.getElementById("LivedFromInput").value;
  enddate = document.getElementById("LivedUntilInput").value;
  profession = document.getElementById("ProfessionInput").value;
  buildeMal();

}

