package tyt.android.bigplanettracks.maps.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;

/**
 * Utilities for working with bitmaps
 * 
 * @author hudvin
 * 
 */
public class BitmapUtils {

	/**
	 * Цвет фона
	 */
	public static int BACKGROUND_COLOR = Color.argb(255, 128, 128, 128);

	/**
	 * Цвет линий
	 */
	private static int LINE_COLOR = Color.argb(255, 154,154,154);


	/**
	 * Paints the background of the card (in the cage)
	 * 
	 * @param cellSize
	 * @param height
	 * @param widht
	 * @return
	 */
	public static Bitmap drawBackground(int cellSize, int height, int widht) {
		// creating the bitmap to fit the screen
		Bitmap bitmap = Bitmap.createBitmap(widht, height, Config.ARGB_8888);
		Canvas cv = new Canvas(bitmap);
		// drawing background
		Paint background = new Paint();
		background.setColor(BACKGROUND_COLOR);
		cv.drawRect(0, 0, widht, height, background);
		background.setAntiAlias(true);
		// sets the color of the lines
		background.setColor(LINE_COLOR);
		// longitudinal lines
		for (int i = 0; i < widht / cellSize; i++) {
			cv.drawLine(cellSize * i, 0, cellSize * i, height, background);
		}
		// transverse lines
		for (int i = 0; i < height / cellSize; i++) {
			cv.drawLine(0, cellSize * i, widht, cellSize * i, background);
		}
		return bitmap;
	}
	
	public static Bitmap drawEmptyBackground(int size){
		Bitmap bitmap = Bitmap.createBitmap(size, size, Config.RGB_565);
		Canvas cv = new Canvas(bitmap);
		// drawing background
		Paint background = new Paint();
		background.setColor(BACKGROUND_COLOR);
		cv.drawRect(0, 0, size, size, background);
		return bitmap;
	}

}
