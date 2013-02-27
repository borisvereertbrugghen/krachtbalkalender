package be.avezoete.boris.krachtbal.kalender.shared;

import java.io.Serializable;
import java.util.Date;

public class Datum implements KeyValue,Serializable{
	private static final long serialVersionUID = -5290124969719456388L;
	private String name;
	private Date start;
	private Date end;
	private String id;

	public Datum(String name, String id,Date start, Date end) {
		this.name = name;
		this.id = id;
		this.start = start;
		this.end = end;
	}
	
	public String getName() {
		return name;
	}
	@Override
	public String getKey() {
		return id;
	}
	
	public Date getStart() {
		return start;
	}
	public Date getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "Datum [id=" + id + ", name=" + name + ", start=" + start
				+ ", end=" + end + "]";
	}
}

