package app.demons.blindassist;

import android.app.Application;
import android.content.Context;

/**
 * @author Adhiraj Singh Chauhan
 */
public class MyApplication extends Application {

	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

	public static Context getMyApplicationContext() {
		return context;
	}
}
