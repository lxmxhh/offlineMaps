package sam.android.nztravelguide.maps.ui;

import android.graphics.Point;
import android.view.MotionEvent;

/**
 * Designed to handle a double click
 * 
 * @author hudvin
 * 
 */
public class DoubleClickDetector {

	/**
	 * The minimum time interval between two separate touches, 
	 * in which they are perceived as double-tap
	 */
	private static int CLICK_INTERVAL = 600;

	/**
	 * The maximum distance between touches, 
	 * in which they are perceived as a double-tap
	 */
	private static int CLICK_PRECISE = 12;

	/**
	 * Stores the previous event
	 */
	private Point previousPoint;

	/**
	 * Keeps the previous events
	 */
	private long eventTime;
	
	public static void setInterval(int interval){
		CLICK_INTERVAL = interval;
	}
	
	
	public static void setPrecise(int precise){
		CLICK_PRECISE = precise;
	}
	
	public boolean process(MotionEvent currentEvent) {
		if (previousPoint != null
				&& (System.currentTimeMillis() - eventTime) < DoubleClickDetector.CLICK_INTERVAL
				&& isNear((int) currentEvent.getX(), (int) currentEvent.getY())) {
			eventTime = System.currentTimeMillis();
			previousPoint = null;
			return true;
		}
		previousPoint = new Point();
		previousPoint.x = (int) currentEvent.getX();
		previousPoint.y = (int) currentEvent.getY();
		eventTime = System.currentTimeMillis();
		return false;
	}

	/**
	 * Checks whether the first point near the second
	 * 
	 * @param event
	 * @return
	 */
	private boolean isNear(int x, int y) {
		boolean checkX = Math.abs(previousPoint.x - x) <= DoubleClickDetector.CLICK_PRECISE;
		boolean checkY = Math.abs(previousPoint.y - y) <= DoubleClickDetector.CLICK_PRECISE;
		return checkX == checkY && checkX == true;
	}

}
