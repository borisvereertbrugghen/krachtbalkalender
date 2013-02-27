package be.avezoete.boris.krachtbal.kalender.shared;

import java.io.Serializable;

public class Club implements KeyValue, Serializable {
	private static final long serialVersionUID = -3738810615765894626L;
	private String name;
	private String id;
	private String pre;
	
	public Club(String name, String pre, String id) {
		this.name = name;
		this.id = id;
		this.pre = pre;
	}
	
	public String getName() {
		return pre+" "+name;
	}
	
	public String getPre() {
		return pre;
	}
	
	@Override
	public String getKey() {
		return id;
	}

	@Override
	public String toString() {
		return "Club [name=" + name + ", id=" + id + "]";
	}
}
