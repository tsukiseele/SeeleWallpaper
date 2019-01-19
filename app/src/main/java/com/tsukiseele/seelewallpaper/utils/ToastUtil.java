package com.tsukiseele.seelewallpaper.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	private static Toast toast = null;
	
	public final static int LENGTH_SHORT = Toast.LENGTH_SHORT;
	public final static int LENGTH_LONG = Toast.LENGTH_LONG;
	// 单例模式
	private ToastUtil() {}
	
	public static Toast makeText(Context context, String message, int time) {
		if(toast == null) {
			toast = Toast.makeText(context, message, time);
		} else {
			toast.setText(message);
			toast.setDuration(time);
		}
		return toast;
	}
}
