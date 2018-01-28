package ch.obermuhlner.salesman.util;

public class ThreadInterruptedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ThreadInterruptedException() {
		super();
	}

	public ThreadInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ThreadInterruptedException(String message) {
		super(message);
	}

	public ThreadInterruptedException(Throwable cause) {
		super(cause);
	}

}
