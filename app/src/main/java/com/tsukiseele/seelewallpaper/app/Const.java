package com.tsukiseele.seelewallpaper.app;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import android.os.Environment;
import com.tsukiseele.seelewallpaper.R;

public class Const {
	public final static File ROOT_DIRECTORY = Environment.getExternalStorageDirectory();
	public final static File APP_DATA_DIRECTORY = new File(ROOT_DIRECTORY, "/android/data/" + R.class.getPackage().getName().trim());
	public final static File DEBUG_CRACHLOG_DIRECTORY = new File(APP_DATA_DIRECTORY, "Logger");
	
	public final static String DEBUG_LOG_WRITE_PATH = APP_DATA_DIRECTORY.getAbsolutePath() + "/debug.log";
	public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
	
	public static void init() {
		try {
			Field[] fields = Const.class.getFields();
			for (Field f : fields) {
				Object obj = f.get(null);
				if (obj instanceof File) {
					File file = (File) obj;
					file.mkdirs();
				}
			}
		} catch (IllegalAccessException e) {
			
		} catch (IllegalArgumentException e) {
			
		}
	}
}
