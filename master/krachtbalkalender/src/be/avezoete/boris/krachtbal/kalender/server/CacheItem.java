package be.avezoete.boris.krachtbal.kalender.server;

import java.io.Serializable;

public class CacheItem<Z> implements Serializable{
	private static final long serialVersionUID = 6955858326127029905L;
	private long lastUpdateTime;
	private long timeout;
	private Z object;
	public CacheItem(long timeout) {
		this.timeout = timeout;
	}
	
	public void setObject(Z object) {
		this.object = object;
		if(object!=null){
			lastUpdateTime=System.currentTimeMillis();
		}else{
			lastUpdateTime=0L;
		}
	}
	
	public Z getObject() {
		if(object==null){
			return null;
		}
		if(System.currentTimeMillis()>lastUpdateTime+timeout){
			object=null;
			lastUpdateTime=0L;
			return null;
		}
		return object;
	}
}
