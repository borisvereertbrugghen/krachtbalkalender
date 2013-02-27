package be.avezoete.boris.krachtbal.kalender.shared;

import java.io.Serializable;
import java.util.Date;

public class KalenderItem implements Serializable{
	private static final long serialVersionUID = 608450698369606704L;
	private String nummer;
	private Date date;
	private Datum datum;
	private String home;
	private String out;
	private int scoreHi;
	private int scoreOi;
	private String comment;
	private Reeks reeks;

	public KalenderItem(String nummer,Reeks reeks, Date date,Datum datum, String home,
			String out, int scoreHi, int scoreOi) {
				this.nummer = nummer;
				this.reeks = reeks;
				this.date = date;
				this.datum = datum;
				this.home = home;
				this.out = out;
				this.scoreHi = scoreHi;
				this.scoreOi = scoreOi;
	}

	public KalenderItem(String nummer,Reeks reeks, Date date,Datum datum, String home,
			String out, String comment) {
				this.nummer = nummer;
				this.reeks = reeks;
				this.date = date;
				this.datum = datum;
				this.home = home;
				this.out = out;
				this.comment = comment;
	}
	
	public Reeks getReeks() {
		return reeks;
	}
	
	public String getHome() {
		return home;
	}
	
	public String getOut() {
		return out;
	}
	
	public Datum getDatum() {
		return datum;
	}

	@Override
	public String toString() {
		return "KalenderItem [reeks=" + reeks + ", nummer=" + nummer
				+ ", date=" + date + ", home=" + home + ", out=" + out + "]";
	}

	
	
	
}
