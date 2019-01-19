package com.tsukiseele.seelewallpaper.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import com.tsukiseele.seelewallpaper.app.Const;
import com.tsukiseele.seelewallpaper.app.debug.Logger;
import com.tsukiseele.seelewallpaper.utils.ToastUtil;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class WallpaperService extends Service {
	public static final String SETTING_FILENAME = "Setting.xml";
	// 壁纸路径
	public static final String TYPE_WALLPAPER_PATH = "wallpaper_path";
	// 更新间隔
	public static final String TYPE_INTERVAL_TIME = "interval_time";
	// 壁纸类型
	public static final String TYPE_WALLPAPER_TYPE = "wallpaper_type";
	public static final String TYPE_STRICT_MODE = "strict_mode";
	public static final String TYPE_COMPAT_MODE = "compat_mode";
	public static final String TYPE_SLEEP_MODE = "sleep_mode";
	public static final String TYPE_UPDATE_MODE = "update_mode";
	public static final String TYPE_CUSTOM_WH_MODE = "custom_wh_mode";
	public static final String TYPE_WINDOW_WIDTH = "window_width";
	public static final String TYPE_WINDOW_HEIGHT = "window_height";
	public static final String TYPE_SYSTEM_BOOT = "system_boot";
	public static final String TYPE_ALPHA = "alpha";
	public static final String TYPE_GRAY_BITMAP = "gray_bitmap";
	
	// 状态 是否开启服务
	public static final String TYPE_STATE = "state";
	public static final String TYPE_SWITCH_TIMER = "switch_timer";
	public static final String TYPE_SWITCH_UNLOCK = "switch_unlock";
	
	private static final String VALUE_ORDER_UPDATE_INDEX = "order_update_index";
	
	public static final int MODE_SCROLL = 0;
	public static final int MODE_SIMPLE = 1;
	public static final int MODE_SCROLL_FIXED = 2;
	
	public static final int UPDATE_MODE_ORDER = 100;
	public static final int UPDATE_MODE_RANDOM = 101;
	// 当前实例
	private static WallpaperService wallpaperService;
	// 计时器
	private AlarmManager alarmManager;
	private PendingIntent pi;
	// 壁纸信息
	private String wallpaperPath;
	private int intervalTimeS;
	private int wallpaperType = MODE_SIMPLE;
	private int wallpaperAlpha = 80;
	private boolean grayBitmap = false;
	// 精确模式
	private boolean strictMode = true;
	// 兼容模式
	private boolean compatMode = false;
	// 省电模式
	private boolean sleepMode = true;
	// 时间切换
	private boolean switchTimer = true;
	// 解锁切换
	private boolean switchUnlock = false;
	
	private int windowWidth = -1;
	private int windowHeight = -1;
	private int updateMode = UPDATE_MODE_ORDER;
	// 保存上次更新的时间
	private static long lastUpdateTime = 0;
	
	public static WallpaperService getInstance() {
		return wallpaperService;
	}
	
	private void startUpdateThread(final File[] wallpaperPaths) {
		// 壁纸更新线程
		new Thread(new Runnable() {
				@Override
				public void run() {
					int index = 0;
					int spareIndex = 0;
					switch (updateMode) {
						// 顺序
						case UPDATE_MODE_ORDER :
							SharedPreferences preferences = WallpaperService.this.getSharedPreferences(SETTING_FILENAME, MODE_PRIVATE);
							SharedPreferences.Editor editor = preferences.edit();
							index = preferences.getInt(VALUE_ORDER_UPDATE_INDEX, -1);
							index = index < 0 ? 0 : index >= wallpaperPaths.length ? 0 : index;
							// 生成备用路径
							spareIndex = index + 1;
							spareIndex = spareIndex < 0 ? 0 : spareIndex >= wallpaperPaths.length ? 0 : spareIndex;
							
							editor.putInt(VALUE_ORDER_UPDATE_INDEX, index + 1);
							editor.commit();
							// 按字母排序
							Arrays.sort(wallpaperPaths, new Comparator<File>() {
									@Override
									public int compare(File p1, File p2) {
										return p1.getName().compareToIgnoreCase(p2.getName());
									}
							});
							break;
						// 随机
						case UPDATE_MODE_RANDOM :
							index = (int) (Math.random() * wallpaperPaths.length); 
							// 生成备用路径
							spareIndex = (int) (Math.random() * wallpaperPaths.length);
							break;
					}
					String wallpaperPath = wallpaperPaths[index].getAbsolutePath();
					String sparePath = wallpaperPaths[spareIndex].getAbsolutePath();
					new WallpaperUpdateTask(
						WallpaperService.this, windowWidth, windowHeight, wallpaperType, wallpaperAlpha, grayBitmap
					).execute(wallpaperPath, sparePath);
				}
		}).start();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 载入数据
		SharedPreferences sharedPreferences = getSharedPreferences(SETTING_FILENAME, Context.MODE_PRIVATE);
		intervalTimeS = sharedPreferences.getInt(TYPE_INTERVAL_TIME, 600);
		wallpaperPath = sharedPreferences.getString(TYPE_WALLPAPER_PATH, "");
		wallpaperType = sharedPreferences.getInt(TYPE_WALLPAPER_TYPE, MODE_SIMPLE);
		strictMode = sharedPreferences.getBoolean(TYPE_STRICT_MODE, true);
		compatMode = sharedPreferences.getBoolean(TYPE_COMPAT_MODE, false);
		sleepMode = sharedPreferences.getBoolean(TYPE_SLEEP_MODE, true);
		windowWidth = sharedPreferences.getInt(TYPE_WINDOW_WIDTH, -1);
		windowHeight = sharedPreferences.getInt(TYPE_WINDOW_HEIGHT, -1);
		updateMode = sharedPreferences.getInt(TYPE_UPDATE_MODE, UPDATE_MODE_ORDER);
		switchTimer = sharedPreferences.getBoolean(TYPE_SWITCH_TIMER, true);
		switchUnlock = sharedPreferences.getBoolean(TYPE_SWITCH_UNLOCK, false);
		wallpaperAlpha = sharedPreferences.getInt(TYPE_ALPHA, 80);
		grayBitmap = sharedPreferences.getBoolean(TYPE_GRAY_BITMAP, false);
		
		File[] wallpaperPaths = getWallpaperDirectorys(wallpaperPath);
		// 路径无效则返回
		if (wallpaperPaths == null || wallpaperPaths.length <= 0) {
			ToastUtil.makeText(this, "找不到壁纸啦，请检查路径", ToastUtil.LENGTH_SHORT).show();
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}
		// 防止更新过快
		if (System.currentTimeMillis() - lastUpdateTime >= 5000) {
			lastUpdateTime = System.currentTimeMillis();
			// 启动壁纸更新线程
			startUpdateThread(wallpaperPaths);
			
			if (switchTimer) {
				// 启动循环计时任务
				startTimerTask(intervalTimeS);
			}
		}
		System.gc();
		
		return START_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		this.wallpaperService = this;
	}
	
	private void startTimerTask(int sleepTimeS) {
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		long sleepTime = sleepTimeS * 1000;
		long triggerAtTime = System.currentTimeMillis() + sleepTime; 
		Intent i = new Intent(this, AlarmReceiver.class);
		pi = PendingIntent.getBroadcast(this, 0, i, 0);
		
		// 是否为省电模式
		if (sleepMode) {
			// 是否为精确模式
			if (strictMode) 
				alarmManager.setExact(AlarmManager.RTC, triggerAtTime, pi);
			else
				alarmManager.set(AlarmManager.RTC, triggerAtTime, pi);
		} else {
			if (strictMode) 
				alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
			else
				alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
		}
		Logger.i(WallpaperService.class, "开始下次计时: " + Const.SIMPLE_DATE_FORMAT.format(new Date()));
	}
	public void cancel() {
		if (alarmManager != null && pi != null)
			alarmManager.cancel(pi);
		if (wallpaperService != null)
			wallpaperService.stopSelf();
		alarmManager = null;
		pi = null;
	}
	public void start(Context context) {
		if (alarmManager == null) {
			Intent i = new Intent(context, WallpaperService.class);
			context.startService(i);
		}
	}
	public boolean isCancel() {
		return alarmManager == null;
	}
	public File[] getWallpaperDirectorys(String wallpaperDir) {
		File[] wallpaperPaths = null;
		File wallpaperPath = new File(wallpaperDir);
		
		if (wallpaperPath != null && wallpaperPath.exists() && wallpaperPath.isDirectory()) {
			wallpaperPaths = wallpaperPath.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						String[] suffixs = new String[] {
							".jpg", ".jpeg", ".png", ".bmp", ".webp"
						};
						for (String suffix : suffixs)
							if (name.endsWith(suffix))
								return true;
						return false;
					}
			});
		}
		return wallpaperPaths;
	}
}
