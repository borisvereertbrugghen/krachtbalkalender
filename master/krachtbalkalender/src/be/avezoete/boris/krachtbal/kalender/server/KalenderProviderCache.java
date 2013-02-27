package be.avezoete.boris.krachtbal.kalender.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.avezoete.boris.krachtbal.kalender.shared.KalenderItem;
import be.avezoete.boris.krachtbal.kalender.shared.KalenderSelectionOptions;

public class KalenderProviderCache implements Serializable {
	private static final long serialVersionUID = 8753668099004884777L;
	private CacheItem<KalenderSelectionOptions> selectionOptions= new CacheItem<>(1800000);
	private Map<String, CacheItem<List<KalenderItem>>> reeksItems = new HashMap<>();
	public KalenderSelectionOptions getSelectionOptions() {
		return selectionOptions.getObject();
	}
	public void setSelectionOptions(KalenderSelectionOptions selectionOptions) {
		this.selectionOptions.setObject(selectionOptions);
	}
	public List<KalenderItem> getKalenderItems(String reeksId) {
		CacheItem<List<KalenderItem>> cacheItem = reeksItems.get(reeksId);
		if(cacheItem==null){
			return null;
		}else{
			return cacheItem.getObject();
		}
	}
	public void setKalender(String reeksId,List<KalenderItem> kalenderItems) {
		CacheItem<List<KalenderItem>> cacheItem = reeksItems.get(kalenderItems);
		if(cacheItem==null){
			cacheItem = new CacheItem<List<KalenderItem>>(1800000);
			reeksItems.put(reeksId, cacheItem);
		}
		cacheItem.setObject(kalenderItems);
	}
	public void clear() {
		selectionOptions.setObject(null);
		reeksItems.clear();
	}
	
}
