package sam.android.nztravelguide.maps.storage;

import sam.android.nztravelguide.NZTravelGuide;
import sam.android.nztravelguide.maps.RawTile;

import android.graphics.Bitmap;

/**
 * Кеш скалированнных и обычных тайлов
 * 
 * @author hudvin
 * 
 */
public class BitmapCacheWrapper {

	/*
	 * размер кеша (для каждого обоих типов)
	 */
	public final static int CACHE_SIZE = (int) (50*NZTravelGuide.density);
	private BitmapCache cache = new BitmapCache(CACHE_SIZE);

	private BitmapCache scaledCache = new BitmapCache(CACHE_SIZE);

	private static BitmapCacheWrapper instance;
	
	public static BitmapCacheWrapper getInstance(){
		if(instance == null){
			instance = new BitmapCacheWrapper();
		}
		return instance;
	}
	
	private BitmapCacheWrapper(){
		
	}
	
	/**
	 * Поиск в кеше скалированых тайлов
	 * 
	 * @param tile
	 * @return
	 */
	public Bitmap getScaledTile(RawTile tile) {
		return scaledCache.get(tile);
	}

	/**
	 * Помещает битмап в кеш скалированых тайлов
	 */
	public void putToScaledCache(RawTile tile, Bitmap bitmap) {
		scaledCache.put(tile, bitmap);
	}

	/**
	 * Поиск в кеше тайлов
	 * 
	 * @param tile
	 * @return
	 */
	public Bitmap getTile(RawTile tile) {
		return cache.get(tile);
	}

	/**
	 * Помещает битмап в кеш тайлов
	 */
	public void putToCache(RawTile tile, Bitmap bitmap) {
		cache.put(tile, bitmap);
	}
	
	public void clear() {
		scaledCache.clear();
		cache.clear();
	}

}
