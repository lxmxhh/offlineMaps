package sam.android.nztravelguide.maps;

import android.app.Application;

public class NZTravelGuideApp extends Application {

	public static boolean isDemo = false;
	
	public NZTravelGuideApp() {
		super();
	}

	@Override
	public void onCreate() {
		Preferences.init(this);
	}

}
