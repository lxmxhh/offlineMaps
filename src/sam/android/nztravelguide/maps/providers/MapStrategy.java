package sam.android.nztravelguide.maps.providers;

public abstract class MapStrategy {

	public abstract String getURL(int x, int y, int z, int layout);

	public abstract String getDescription();
}
