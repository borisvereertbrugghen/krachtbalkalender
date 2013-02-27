package be.avezoete.boris.krachtbal.kalender.shared;

/**
 * Exception that indicates there is some issue fetching data from krachbal site
 * @author boris
 *
 */
public class KalenderKBException extends Exception {
	private static final long serialVersionUID = -3241139241427258231L;

	public KalenderKBException() {
		super();
	}

	public KalenderKBException(String message, Throwable cause) {
		super(message, cause);
	}

	public KalenderKBException(String message) {
		super(message);
	}

	public KalenderKBException(Throwable cause) {
		super(cause);
	}
	
}
