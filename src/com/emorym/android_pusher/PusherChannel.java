package com.emorym.android_pusher;

/*	Copyright (C) 2011 Emory Myers
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 *  Contributors: Martin Linkhorst
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class PusherChannel implements PusherEventEmitter {
	private static final String LOG_TAG = "PusherChannel";

	private Pusher mPusher;
	private String mName;

	private List<PusherCallback> mGlobalCallbacks = new ArrayList<PusherCallback>();
	private Map<String, List<PusherCallback>> mLocalCallbacks = new HashMap<String, List<PusherCallback>>();
	
	private Map<String, JSONObject> mLocalUsers = new HashMap<String, JSONObject>();

	public PusherChannel(Pusher pusher, String name) {
		mPusher = pusher;
		mName = name;
	}

	public boolean isPrivate() {
		return mName.startsWith("private-");
	}
	
	public boolean isPresence(){
		return mName.startsWith("presence-");
	}

	public void bind(String event, PusherCallback callback) {
		/* if there are no callbacks for that event assigned yet, initialize the list */
		if (!mLocalCallbacks.containsKey(event)) {
			mLocalCallbacks.put(event, new ArrayList<PusherCallback>());
		}

		/* add the callback to the event's callback list */
		mLocalCallbacks.get(event).add(callback);

		//Log.d(LOG_TAG, "bound to event " + event + " on channel " + mName);
	}

	public void bindAll(PusherCallback callback) {
		mGlobalCallbacks.add(callback);

		//Log.d(LOG_TAG, "bound to all events on channel " + mName);
	}

	public void unbind(PusherCallback callback) {
		/* remove all matching callbacks from the global callback list */
		while (mGlobalCallbacks.remove(callback))
			;

		/* remove all matching callbacks from each local callback list */
		for (List<PusherCallback> localCallbacks : mLocalCallbacks.values()) {
			while (localCallbacks.remove(callback))
				;
		}
	}

	public void unbindAll() {
		/* remove all callbacks from the global callback list */
		mGlobalCallbacks.clear();
		/* remove all local callback lists, that is removes all local callbacks */
		mLocalCallbacks.clear();
	}

	public void dispatchEvents(String eventName, String eventData) {
		if ( this.isPresence() ) {
			if ( eventName.equalsIgnoreCase("pusher_internal:subscription_succeeded") ){
				JSONArray users = null;
				try {
					users = new JSONArray(eventData);
				} catch (JSONException e) {
					users = new JSONArray();
					//e.printStackTrace();
				}
				for (int i =0;  i < users.length(); i++) {			
					try {
						JSONObject user = users.getJSONObject(i);
						if (user.has("user_info")){
							this.mLocalUsers.put(user.getString("user_id"), user.getJSONObject("user_info"));
						} else {
							this.mLocalUsers.put(user.getString("user_id"), new JSONObject());
						}
					} catch (JSONException e) {
						//e.printStackTrace();
					}
				}
			} 
			if (eventName.equalsIgnoreCase("pusher_internal:member_added")){
				JSONObject user = null;
				try {
					user = new JSONObject( eventData );
					JSONObject user_info = user.getJSONObject("user_info");
					this.mLocalUsers.put(user.getString("user_id"), user_info);
				} catch (JSONException e) {
						//e.printStackTrace();
				}
				
			}
			if (eventName.equalsIgnoreCase("pusher_internal:member_removed")) {
				JSONObject user = null;
				try {
					user = new JSONObject( eventData );
					this.mLocalUsers.remove(user.getString("user_id"));
				} catch (JSONException e) {
					//e.printStackTrace();
				}
			}
		}
			
		Bundle payload = new Bundle();
		payload.putString("eventName", eventName);
		payload.putString("eventData", eventData);
		payload.putString("channelName", mName);
		
		Message msg = Message.obtain();
		msg.setData(payload);
		
		for (PusherCallback callback : mGlobalCallbacks) {
			callback.sendMessage(msg);
		}

		/* do we have a callback bound to that event? */
		if (mLocalCallbacks.containsKey(eventName)) {
			/* execute each callback */
			for (PusherCallback callback : mLocalCallbacks.get(eventName)) {
				callback.sendMessage(msg);
			}
		}
	}

	public String getName() {
		return mName;
	}
	
	public Map<String,JSONObject> getUsers(){
		//return new HashMap<String, JSONObject>(this.mLocalUsers);
		Map<String, JSONObject> users = new HashMap<String, JSONObject>();
		users.putAll(this.mLocalUsers);
		return users;
	}
	
	public JSONObject getUser(String user_id){
		return this.mLocalUsers.get(user_id);
	}
	
	public void subscribe(){
		
		new Thread( new Runnable(){
			
			public void run() {
				String channelName = PusherChannel.this.getName();			

				JSONObject eventData = new JSONObject();
				try {
					eventData.put("channel", channelName);
		
					if ( PusherChannel.this.isPresence() || PusherChannel.this.isPrivate() ){
						String authString = authenticate(channelName);
						if ( authString == null ) return;
						JSONObject authInfo = new JSONObject(authString);
						@SuppressWarnings("unchecked")
						Iterator<String> iter = authInfo.keys();
						while( iter.hasNext() ){
							String key = iter.next();
							String value = authInfo.getString(key);
							eventData.put(key, value);
						}
					}
					
					mPusher.sendEvent(mPusher.PUSHER_EVENT_SUBSCRIBE, eventData, null);
				
				} catch (Exception e) {
					mPusher.dispatchEvents("pusher:subscription_error", "{ \"message\": \""+ e.toString() +"\" }", null);
				}
			}
			
			public String authenticate(String channelName){
				HttpClient httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost(mPusher.getChannelAuthEndpoint());
			    
			 // Add all extra headers to the request
			    Map<String, String> auth_headers = mPusher.getAuthHeaders();
			    if( !auth_headers.isEmpty() ){
			    	Set<String> keys = auth_headers.keySet();
			    	Iterator<String> iter = keys.iterator();
			    	while( iter.hasNext() ){
			    		String key = iter.next();
			    		String value = auth_headers.get(key);
			    		httppost.setHeader(key, value);
			    	}
			    }
			    
			    // Prepare params 
				List<NameValuePair> namedParams = new ArrayList<NameValuePair>(2);
				namedParams.add(new BasicNameValuePair( "socket_id", mPusher.getSocketId() ));
				namedParams.add(new BasicNameValuePair( "channel_name", channelName));
			    
				// Add all extra params to the request
				Map<String, String> auth_params = mPusher.getAuthParams();
				if (! auth_params.isEmpty()){		
					Set<String> keys = auth_params.keySet();
					Iterator<String> iter = keys.iterator();
					while( iter.hasNext() ){
						String key = iter.next();
						String value = auth_params.get(key);
						namedParams.add(new BasicNameValuePair( key, value));
					}			
				}
				
				try {
					httppost.setEntity(new UrlEncodedFormEntity(namedParams));

					HttpResponse response = httpclient.execute(httppost);

					String line = "";
					StringBuilder total = new StringBuilder();
					// Wrap a BufferedReader around the InputStream
					InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
					BufferedReader rd = new BufferedReader( reader );

					// Read response until the end
					while ((line = rd.readLine()) != null) { 
						total.append(line); 
					}

					// Return full string
					return total.toString();
				} catch (Exception e) {
					mPusher.dispatchEvents("pusher:subscription_error", "{ \"message\": \""+ e.toString() +"\" }", null);
				} 							
				return null;    
			}
			
		}).start();

	}
}
