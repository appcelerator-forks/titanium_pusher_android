
exports.bindAll = function(callback){
	if ('function' !== typeof callback) {
		throw new Error('bindAll only takes instances of Function');
	}
	var uniqueId = this.bindAllNative(callback);
	if (!this._callbacks ) this._callbacks = [];
	this._callbacks.push( { 'callback' : callback, 'id' : uniqueId });
}

exports.bind = function(event, callback){
	if ('function' !== typeof callback) {
		throw new Error('bind only takes instances of Function');
	}
	var uniqueId = this.bindNative(event, callback);
	if (!this._callbacks ) this._callbacks = [];
	this._callbacks.push( { 'callback' : callback, 'id' : uniqueId });
}

exports.unbind = function(callback){
	if (!this._callbacks || this._callbacks.length < 1)
	var position = -1;
	
	for (var i = 0, length = this._callbacks.length; i < length; i++) {
		if (this._callbacks[i].callback === callback)
		{
			position = i;
			break;
		}
	}
	
	if (position < 0) { return; }
	
	this.unbindNative(this._callbacks[position].id);
	this._callbacks.splice(position,1);
}

exports.unbindAll = function(){
	this.unbindAllNative();
	this._callbacks = [];
}


exports.connect = function(){
	this.doconnect();
}

exports.setup = function(args){ 
	this.dosetup(args);
	exports.connection.setup( this.getConnection() );
}


// Connection class
function Connection(){
	var _connection_proxy;
	var _callbacks;
	
	this.setup = function(connection_proxy){
		this._callbacks = [];
		this._connection_proxy = connection_proxy;		
	}
	
	this.unbindAll = function(){
		this._connection_proxy.unbindAllNative();
		this._callbacks = [];
	}
	
	this.bindAll = function(callback){
		if ('function' !== typeof callback) {
			throw new Error('bindAll only takes instances of Function');
		}
		var uniqueId = this._connection_proxy.bindAllNative(callback);
		if (!this._callbacks ) this._callbacks = [];
		this._callbacks.push( { 'callback' : callback, 'id' : uniqueId });
	}
	
	this.bind = function(event,callback){
		if ('function' !== typeof callback) {
			throw new Error('bind only takes instances of Function');
		}
		var uniqueId = this._connection_proxy.bindNative(event, callback);
		if (!this._callbacks ) this._callbacks = [];
		this._callbacks.push( { 'callback' : callback, 'id' : uniqueId });
	}
	
	this.unbind = function(callback){
		
	}
	
};

//Object.defineProperty(Connection.prototype, "setup", {
//	value: function(connection_proxy){
//		this._connection_proxy = connection_proxy;		
//		return this
//	},
//	enumerable: false	
//});

Object.defineProperty(Connection.prototype, "state", {
	get: function(){
		return this._connection_proxy.getState()
	},
	set: undefined
});

exports.connection = new Connection();
