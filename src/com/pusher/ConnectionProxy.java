package com.pusher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.json.JSONException;
import org.json.JSONObject;

import com.emorym.android_pusher.PusherCallback;
import com.emorym.android_pusher.PusherConnection;

@Kroll.proxy(creatableInModule = PusherModule.class)
public class ConnectionProxy extends KrollProxy 
{
	
	PusherModule mPusherM;
	PusherConnection mConnection;
	
	private List<KrollFunction> mGlobalCallbacks = new ArrayList<KrollFunction>();
	private Map<String, List<KrollFunction>> mLocalCallbacks = new HashMap<String, List<KrollFunction>>();

	public ConnectionProxy() {
		super();
	}
	
	public void configure(PusherModule _pusherModule, PusherConnection connection) {
		this.mPusherM = _pusherModule;
		this.mConnection = connection;
		
		this.mConnection.bindAll(new PusherCallback() {
			
			@Override
			public void onEvent(String eventName, JSONObject eventData, String channelName) {

				// We need to convert eventData to HashMap
				HashMap<String,String> eventHashData = new HashMap<String,String>();
				@SuppressWarnings("unchecked")
				Iterator<String> iter = eventData.keys();
				while( iter.hasNext() ){
					String key = iter.next();
					try {
						eventHashData.put(key, eventData.getString(key));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				Object[] params = { eventName, eventHashData, channelName };
				
				for (KrollFunction callback : mGlobalCallbacks) {
					callback.call(getKrollObject(), params); 
				}
				
				/* do we have a callback bound to that event? */
				if (mLocalCallbacks.containsKey(eventName)) {
					/* execute each callback */
					for (KrollFunction callback : mLocalCallbacks.get(eventName)) {
						callback.call(getKrollObject(), params); 					
					}
				}
				
			}
			
		});
			
	}
	
	@Kroll.getProperty @Kroll.method
	public String getState(){
		return this.mConnection.state();
	}
	
	// Bind methods
	@Kroll.method
	public long bindAllNative(KrollFunction func) {
		mGlobalCallbacks.add(func);
		return Helpers.uniqueId(func);
	}
	
	@Kroll.method
	public void unbindAllNative() {
		/* remove all callbacks from the global callback list */
		mGlobalCallbacks.clear();
		/* remove all local callback lists, that is removes all local callbacks */
		mLocalCallbacks.clear();
	}

	
}
