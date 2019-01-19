package com.tsukiseele.seelewallpaper.service;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// 重启动服务
		Intent i = new Intent(context, WallpaperService.class);
		context.startService(i);
	}
}
