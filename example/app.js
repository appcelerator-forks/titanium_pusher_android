// Initialization

var Pusher = require('com.pusher');
Pusher.setup({
  key: '33e9ab34f98fcc05005f',     // CHANGEME
  reconnectAutomaticaly: true,
  encrypted: true
});


Pusher.log = function(x){
	Ti.API.warn("LOG 1");
	alert(x);
	Ti.API.warn("LOG 2");
};

var myEventHandler = function(x,y,z){
	alert(x);
	alert(y);
	alert(z);
};

//Pusher.bindAll( myEventHandler );
//Pusher.unbind( myEventHandler );
//Pusher.bind( 'pusher:connection_established', myEventHandler);

//Pusher.connection.bindAll( myEventHandler );

//Pusher.unbindAll();

var window = Ti.UI.createWindow({
	backgroundColor:'white',
  title: 'Pusher'
});
window.open()

// Handlers
var handleConnected = function() {
  Pusher.connect();
};

var handleDoStuff = function(){
	var public_channel = Pusher.subscribeChannel("public-channel");
	public_channel.bindAll( function(x,y,z){
		handleEvent(x,y,z);
	})
	
	Pusher.channel_auth_endpoint = "http://192.168.2.111:3000/auth/";
	var private_channel = Pusher.subscribeChannel("private-channel");
	private_channel.bindAll( function(x,y,z){
		handleEvent(x,y,z);
	})
	
	Pusher.channel_auth_endpoint = "http://192.168.2.111:3000/presence_auth/";
	var presence_channel = Pusher.subscribeChannel("presence-channel");
	presence_channel.bindAll( function(x,y,z){
		handleEvent(x,y,z);
	})
	
	//alert(Pusher.channel_auth_endpoint);

	//var connection_state = Pusher.connection.state;
	//alert(connection_state);
	//Pusher.connection.bindAll(myEventHandler);
	//Pusher.connection.unbindAll();
}

var handleDisconnected = function() {
  Pusher.disconnect();
}

var handleEvent = function(x,y,z) {
  //Ti.API.warn("ATAO 2");

  var label = Ti.UI.createLabel({
	text: "event: " + x, 
    top: 3,
    left: 10,
    height: '23',
    font: {fontSize: 20}
  });

  var sublabel = Ti.UI.createLabel({
    text: JSON.stringify(y),
    top: 25,
    left: 10,
    height: '15',
    font: {fontSize:12}
  });

  
  var tableViewRow = Ti.UI.createTableViewRow({});
  tableViewRow.add(label);
  tableViewRow.add(sublabel);
  
  var sublabel2 = Ti.UI.createLabel({
	  text: "channel: " + z,
	  top: 50,
	  left: 10,
	  height: '15',
	  font: {fontSize:12}
  });
  tableViewRow.add(sublabel2);

  //Ti.API.warn("ATAO 2");

  tableview.appendRow(tableViewRow, {animated:true});
};

Pusher.bindAll(handleEvent);
Pusher.connection.bindAll(handleEvent);

var handleAlertEvent = function(e) {
  alert(JSON.stringify(e.data));
}

var menu;
var CONNECT = 1, DISCONNECT = 2, ADD = 3;
var DOSTUFF = 4;
Ti.Android.currentActivity.onCreateOptionsMenu = function(e) {
  menu = e.menu;
  var connect = menu.add({title:'Connect', itemId:CONNECT});
  connect.addEventListener('click', handleConnected);
  
  var do_stuff = menu.add({title:"Do Stuff", itemID:DOSTUFF});
  do_stuff.addEventListener('click', handleDoStuff);
  
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

