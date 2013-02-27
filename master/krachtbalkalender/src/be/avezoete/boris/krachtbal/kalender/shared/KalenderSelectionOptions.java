package be.avezoete.boris.krachtbal.kalender.shared;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

public class KalenderSelectionOptions implements Serializable {

	private static final long serialVersionUID = 5749367627199006352L;
	private final LinkedHashMap<String,Club> clubs;
	private final LinkedHashMap<String,Reeks> reeksen;
	private final LinkedHashMap<String,Datum> datums;
	

	public KalenderSelectionOptions(List<Club> clubs, List<Reeks> reeksen, List<Datum> datums) {
		this.reeksen = toMap(reeksen);
		this.datums = toMap(datums);
		this.clubs = toMap(clubs);
	}
	
	private <Z extends KeyValue> LinkedHashMap<String,Z> toMap(List<Z> list){
		LinkedHashMap<String,Z> map = new LinkedHashMap<>(list.size());
		for (Z z : list) {
			if(!map.containsKey(z.getKey())){
				map.put(z.getKey(), z);
			}
		}
		return map;
	}
	
	public LinkedHashMap<String, Club> getClubs() {
		return clubs;
	}
	public LinkedHashMap<String, Datum> getDatums() {
		return datums;
	}
	public LinkedHashMap<String, Reeks> getReeksen() {
		return reeksen;
	}

	@Override
	public String toString() {
		return "KalenderSelectionOptions [clubs=" + clubs + ", reeksen="
				+ reeksen + ", datums=" + datums + "]";
	}
	
	

}
