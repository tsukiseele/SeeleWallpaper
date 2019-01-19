package com.tsukiseele.seelewallpaper.app;

import android.app.Application;
import android.content.Context;
import com.tsukiseele.seelewallpaper.app.debug.CrashHandler;

public class App extends Application {
	private static Context context;
	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		CrashHandler.getInstance().init(this);
	}
	public static void setContext(Context context) {
		App.context = context;
	}

	public static Context getContext() {
		return context;
	}
}
