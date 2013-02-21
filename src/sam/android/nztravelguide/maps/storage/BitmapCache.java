package sam.android.nztravelguide.maps.storage;

import sam.android.nztravelguide.maps.RawTile;

import android.graphics.Bitmap;

/**
 * @author tytung
 */
public class BitmapCache {

	private LRUCache<RawTile, Bitmap> cacheMap;

	public BitmapCache(int size) {
		cacheMap = new LRUCache<RawTile, Bitmap>(size);
	}

	public void clear(){
		cacheMap.clear();
	}
	
	public void put(RawTile tile, Bitmap bitmap) {
		cacheMap.put(tile, bitmap);
	}

	public Bitmap get(RawTile tile) {
		return cacheMap.get(tile);
	}

}
