  anychart.onDocumentReady(function () {

    // create data
    var data = [


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


    ];

    // set the input date/time format
    anychart.format.inputDateTimeFormat("yyyy-MM-dd");

    // set the output date/time format
    anychart.format.outputDateTimeFormat("d MMMM yyyy");

    // create a data tree
    var treeData = anychart.data.tree(data, "as-tree");

    var mapping = treeData.mapAs({actualStart: "birth_date", actualEnd: "death_date"});

    // create a chart
    chart = anychart.ganttProject();

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
    chart.splitterPosition("10%");

    /* listen to the rowClick event
    and update the chart title */
    chart.listen("rowClick", function (e) {
      var itemName = e.item.get("info");
      chart.title(itemName);
      document.getElementById("TextDisplay").innerHTML = itemName;
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
                    "{%actualStart}{dateTimeFormat:d MMMM yyyy}<br>"  + "Died on " +
                    "{%actualEnd}{dateTimeFormat:d MMMM yyyy}</span>"

       );


    // set the data
    chart.data(mapping); //mapping oder treedata als input, liefert

// set the minimum and maximum values of the scale
    chart.getTimeline().scale().minimum("0000-01-01");
    chart.getTimeline().scale().maximum("2030-01-01");

    // set the container id
    chart.container("container");

        // set zoom levels of the scale
    chart.getTimeline().scale().zoomLevels([
      [
        {unit: "year", count: 40},

      ]
    ]);

    // initiate drawing the chart
    chart.draw();

    // fit elements to the width of the timeline
    chart.fitAll();
});

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