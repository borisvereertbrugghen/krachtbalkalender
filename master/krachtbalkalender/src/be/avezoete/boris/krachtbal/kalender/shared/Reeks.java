package be.avezoete.boris.krachtbal.kalender.shared;

import java.io.Serializable;

public class Reeks implements KeyValue, Serializable {
	private static final long serialVersionUID = 9028748891942139122L;
	private String name;
	private String id;

	public Reeks(String name, String id) {
		this.name = name;
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	@Override
	public String getKey() {
		return id;
	}

	@Override
	public String toString() {
		return "Reeks [name=" + name + ", id=" + id + "]";
	}
}
