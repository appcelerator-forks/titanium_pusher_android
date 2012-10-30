package com.pusher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONException;
import org.json.JSONObject;

import com.emorym.android_pusher.PusherCallback;
import com.emorym.android_pusher.PusherChannel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@Kroll.proxy(creatableInModule = PusherModule.class)
public class ChannelProxy extends KrollProxy
{
	
	PusherModule mPusherM;
	PusherChannel mChannel;
	
	private List<KrollFunction> mGlobalCallbacks = new ArrayList<KrollFunction>();
	private Map<String, List<KrollFunction>> mLocalCallbacks = new HashMap<String, List<KrollFunction>>();
	
	public ChannelProxy() {
		super();
	}
	
	public void configure(PusherModule _pusherModule, PusherChannel channel) {
		this.mPusherM = _pusherModule;
		this.mChannel = channel;
		
		this.mChannel.bindAll(new PusherCallback() {
			
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
				
				for (KrollFunction callback : mGlobalCallbacks) {
					callback.call(getKrollObject(), eventHashData); 
				}
				
				/* do we have a callback bound to that event? */
				if (mLocalCallbacks.containsKey(eventName)) {
					/* execute each callback */
					for (KrollFunction callback : mLocalCallbacks.get(eventName)) {
						callback.call(getKrollObject(), eventHashData); 					
					}
				}
				
			}
			
		});
		
		
	}
	
	@Kroll.method(runOnUiThread=true)
	public void unsubscribe() {
		// TODO
	}
	
	@Kroll.method
	public void sendEvent(String eventName, Object data) throws org.json.JSONException {
		JSONObject jsonData = new JSONObject(TiConvert.toString(data));
		this.mPusherM.sendEvent(eventName, this.mChannel.getName(), jsonData);
	}

	// Bind methods
	@Kroll.method
	public void bindAll(KrollFunction func){
		mGlobalCallbacks.add(func);
	}
	
	@Kroll.method
	public void bind(String event, KrollFunction func){
		/* if there are no callbacks for that event assigned yet, initialize the list */
		if (!mLocalCallbacks.containsKey(event)) {
			mLocalCallbacks.put(event, new ArrayList<KrollFunction>());
		}

		/* add the callback to the event's callback list */
		mLocalCallbacks.get(event).add(func);
	}
	
	@Kroll.method
	public void unbindAll(){
		/* remove all callbacks from the global callback list */
		mGlobalCallbacks.clear();
		/* remove all local callback lists, that is removes all local callbacks */
		mLocalCallbacks.clear();
	}
	
	@Kroll.method
	public void unbind(KrollFunction func){
		// TODO
	}
	
	@Kroll.getProperty @Kroll.method
	public String getName(){
		return this.mChannel.getName();
	}
	
	@Kroll.getProperty @Kroll.method
	public Map<String,JSONObject> getUsers(){
		return this.mChannel.getUsers();
	}
	
	@Kroll.method
	public JSONObject getUser(String user_id){
		return this.mChannel.getUser(user_id);
	}
	
 }
