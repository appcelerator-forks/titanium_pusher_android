package com.pusher;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.runtime.v8.V8Function;

public class Helpers {

	public static long uniqueId(KrollFunction func) {
		if (V8Function.class.isInstance(func) ) {
			return ((V8Function) func).getPointer();
		//} else if (RhinoFunction.class.isInstance(func) ) {
		//	return (RhinoFunction) func).getFunction();
		} else {
			return func.hashCode();
		}
	}
	
}
