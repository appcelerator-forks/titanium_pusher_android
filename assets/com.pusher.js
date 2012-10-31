
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

exports.subscribeChannel = function(channelName){
	var channel_proxy = this.subscribeChannelNative(channelName);
	var channel = new Channel();
	channel.setup(channel_proxy);
	return channel;
}

exports.xpto = function(){

}


// Connection class
function Connection(){
	var _connection_proxy;
	var _callbacks;
	
	this.setup = function(connection_proxy){
		this._callbacks = [];
		this._connection_proxy = connection_proxy;		
	}
	
	// UNBIND ALL
	this.unbindAll = function(){
		this._connection_proxy.unbindAllNative();
		this._callbacks = [];
	}
	
	
	// BIND ALL
	this.bindAll = function(callback){
		if ('function' !== typeof callback) {
			throw new Error('bindAll only takes instances of Function');
		}
		var uniqueId = this._connection_proxy.bindAllNative(callback);
		if (!this._callbacks ) this._callbacks = [];
		this._callbacks.push( { 'callback' : callback, 'id' : uniqueId });
	}
	
	// BIND
	this.bind = function(event,callback){
		if ('function' !== typeof callback) {
			throw new Error('bind only takes instances of Function');
		}
		var uniqueId = this._connection_proxy.bindNative(event, callback);
		if (!this._callbacks ) this._callbacks = [];
		this._callbacks.push( { 'callback' : callback, 'id' : uniqueId });
	}
	
	// UNBIND
	this.unbind = function(callback){
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

		this._connection_proxy.unbindNative(this._callbacks[position].id);
		this._callbacks.splice(position,1);
	}
	
};

Object.defineProperty(Connection.prototype, "state", {
	get: function(){
		return this._connection_proxy.getState()
	},
	set: undefined
});

exports.connection = new Connection();



// Channel class
function Channel(){
	var _callbacks;
	var _channel_proxy;
	
	this.setup = function(channel_proxy){
		this._channel_proxy = channel_proxy;
		return this;
	}

	// UNBIND ALL
	this.unbindAll = function(){
		this._channel_proxy.unbindAllNative();
		this._callbacks = [];
	}
	
	// BIND ALL
	this.bindAll = function(callback){
		if ('function' !== typeof callback) {
			throw new Error('bindAll only takes instances of Function');
		}
		var uniqueId = this._channel_proxy.bindAllNative(callback);
		if (!this._callbacks ) this._callbacks = [];
		this._callbacks.push( { 'callback' : callback, 'id' : uniqueId });
	}
	
	// BIND
	this.bind = function(event,callback){
		if ('function' !== typeof callback) {
			throw new Error('bind only takes instances of Function');
		}
		var uniqueId = this._channel_proxy.bindNative(event, callback);
		if (!this._callbacks ) this._callbacks = [];
		this._callbacks.push( { 'callback' : callback, 'id' : uniqueId });
	}
	
	// UNBIND
	this.unbind = function(callback){
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

		this._channel_proxy.unbindNative(this._callbacks[position].id);
		this._callbacks.splice(position,1);
	}
	
};

Object.defineProperty(Channel.prototype, "name", {
	get: function(){
		return this._channel_proxy.getName()
	},
	set: undefined
});

Object.defineProperty(Channel.prototype, "members", {
	get: function(){
		return this._channel_proxy.getUsers()
	},
	set: undefined
});

