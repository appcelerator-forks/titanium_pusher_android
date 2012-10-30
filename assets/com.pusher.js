
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
	var uniqueId = this.bindNative(callback);
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