package com.tsukiseele.seelewallpaper.service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import com.tsukiseele.seelewallpaper.app.debug.Logger;
import android.content.SharedPreferences;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.writeDebugLog(BootBroadcastReceiver.class, "SystemBootEvent", "Boot this system , BootBroadcastReceiver onReceive()");
		SharedPreferences sharedPreferences = context.getSharedPreferences(WallpaperService.SETTING_FILENAME, Context.MODE_PRIVATE);
		// 是否启用
		boolean isOpen = sharedPreferences.getBoolean(WallpaperService.TYPE_STATE, false);
		
		if (isOpen) {
			switch (intent.getAction()) {
				case Intent.ACTION_USER_PRESENT :
					// 是否应该启动
					boolean isUnlock = sharedPreferences.getBoolean(WallpaperService.TYPE_SWITCH_UNLOCK, false);
					if (isUnlock) {
						startWallpaperService(context);
					}
					break;
				case Intent.ACTION_USER_PRESENT :
					// 是否开机启动
					boolean isBoot = sharedPreferences.getBoolean(WallpaperService.TYPE_SYSTEM_BOOT, false);
					if (isBoot) {
						Logger.writeDebugLog(BootBroadcastReceiver.class, "SystemBootEvent", "Service success start");
						// 重启动服务
						Intent i = new Intent(context, WallpaperService.class);
						context.startService(i);
					}
					break;
				default :
					startWallpaperService(context);
					break;
			}
		}
		
    }

	private void startWallpaperService(Context context) {
		// 重启动服务
		Intent i = new Intent(context, WallpaperService.class);
		context.startService(i);
	}
}
