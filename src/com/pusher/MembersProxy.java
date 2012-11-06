package com.pusher;

import java.util.HashMap;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import android.util.Log;

@Kroll.proxy
public class MembersProxy extends KrollProxy {

	private ChannelProxy channelProxy = null;
	private Map<String, Map<String,String>> members = null;
	
	public void configure( ChannelProxy channelProxy, Map<String, Map<String,String>> members ){
		this.channelProxy = channelProxy;
		this.members = members;
	}
	
	@Kroll.method
	public void each( KrollFunction function){
		for (Map<String,String> member: members.values()){
			function.call(getKrollObject(), (HashMap<String,String>)member);
		}
	}
	
	@Kroll.method
	public KrollDict get(String userId){
		return new KrollDict(members.get(userId));
	}
	
	@Kroll.method
	@Kroll.getProperty
	public KrollDict getMe(){
		Log.d("MembersProxy", this.channelProxy.getUserId());
		return this.get(this.channelProxy.getUserId());
	}
	
	@Kroll.method
	@Kroll.getProperty
	public int getCount(){
		return members.size();
	}
	
}
