package be.avezoete.boris.krachtbal.kalender.shared;

import java.io.Serializable;
import java.util.List;

public class Kalender implements Serializable {
	private static final long serialVersionUID = -6692816392170319493L;
	private List<KalenderItem> items;

	public Kalender(List<KalenderItem> items) {
		this.items = items;
	}
	public List<KalenderItem> getItems() {
		return items;
	}
}
