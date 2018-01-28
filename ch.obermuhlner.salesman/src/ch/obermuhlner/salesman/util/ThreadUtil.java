package ch.obermuhlner.salesman.util;

public class ThreadUtil {

	public static void checkThreadInterrupted() {
		if (Thread.interrupted()) {
			throw new ThreadInterruptedException();
		}
	}
	
	public static void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			throw new ThreadInterruptedException();
		}
	}

}
