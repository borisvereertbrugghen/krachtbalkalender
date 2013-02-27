package be.avezoete.boris.krachtbal.kalender.shared;

import java.util.HashSet;
import java.util.Set;

public class KalenderSelection {
	private final Set<String> clubs= new HashSet<>();
	private final Set<String> reeksen= new HashSet<>();
	private final Set<String> datums= new HashSet<>();

	public Set<String> getClubs() {
		return clubs;
	}
	public Set<String> getReeksen() {
		return reeksen;
	}
	public Set<String> getDatums() {
		return datums;
	}
	
	@Override
	public String toString() {
		return "KalenderSelection [clubs=" + clubs + ", reeksen=" + reeksen
				+ ", datums=" + datums + "]";
	}
	
	
}
