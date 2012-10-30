// Initialization

var Pusher = require('com.pusher');
Pusher.setup({
  key: '33e9ab34f98fcc05005f',     // CHANGEME
  reconnectAutomaticaly: true,
  encrypted: true
});

Pusher.setLog( function(x){
	Ti.API.warn("LOG 1");
	alert(x);
	Ti.API.warn("LOG 2");
});

var myEventHandler = function(x){
	alert("EVENT!");
};

Pusher.bindAll( myEventHandler );

Pusher.unbind( myEventHandler );

var window = Ti.UI.createWindow({
	backgroundColor:'white',
  title: 'Pusher'
});
window.open()

// Handlers
var handleConnected = function() {
  Pusher.connect();
};

var handleDisconnected = function() {
  Pusher.disconnect();
}

var handleEvent = function(e) {
  Ti.API.warn("ATAO 2");

  var label = Ti.UI.createLabel({
    text: "channel:" + e.channel + " event: " + e.name,
    top: 3,
    left: 10,
    height: '23',
    font: {fontSize: 20}
  });

  var sublabel = Ti.UI.createLabel({
    text: JSON.stringify(e.data),
    top: 25,
    left: 10,
    height: '15',
    font: {fontSize:12}
  });

  var tableViewRow = Ti.UI.createTableViewRow({});
  tableViewRow.add(label);
  tableViewRow.add(sublabel);

  Ti.API.warn("ATAO 2");

  tableview.appendRow(tableViewRow, {animated:true});
};

var handleAlertEvent = function(e) {
  alert(JSON.stringify(e.data));
}

var menu;
var CONNECT = 1, DISCONNECT = 2, ADD = 3;
Ti.Android.currentActivity.onCreateOptionsMenu = function(e) {
  menu = e.menu;
  var connect = menu.add({title:'Connect', itemId:CONNECT});
  connect.addEventListener('click', handleConnected);
  
  var disconnect = menu.add({title:'Disconnect', itemId:DISCONNECT});
  disconnect.addEventListener('click', handleDisconnected);

  var add = menu.add({title:'Add', itemId:ADD});
  add.addEventListener('click', function() {
    var new_window = Ti.UI.createWindow({
      url: 'channel.js',
      backgroundColor: 'white',
      title: 'Send event to channel',
      fullscreen: true
    });
    new_window.pusher = Pusher;
    new_window.open({animated:true});
  });
}

var tableview = Ti.UI.createTableView({
  data: [],
  headerTitle: 'Send events to the test channel'
});
window.add(tableview);

