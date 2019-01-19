package com.tsukiseele.seelewallpaper.app.debug;

import android.content.Context;
import android.util.Log;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import com.tsukiseele.seelewallpaper.utils.ToastUtil;
import static com.tsukiseele.seelewallpaper.app.Const.DEBUG_LOG_WRITE_PATH;

public class Logger {
	private static final String TAG = Logger.class.getPackage().toString();
	public static enum LogLevel {
		VERBOSE,
		DEBUG,
		INFO,
		WARN,
		ERROR,
		CLOSE,
	}
	private static LogLevel logLevel = LogLevel.VERBOSE;
	public static LogLevel getLogLevel() {
		return logLevel;
	}
	public static void setLogLevel(LogLevel mode) {
		logLevel = mode;
	}
	public static void v(String tag, String message) {
		if(logLevel.ordinal() < 1) {
			Log.v(tag, message);
		}
	}
	public static void v(Class<?> type, String message) {
		if(logLevel.ordinal() < 1) {
			Log.i(type.getName(), message);
		}
	}
	public static void d(String tag, String message) {
		if(logLevel.ordinal() < 2) {
			Log.d(tag, message);
		}
	}
	public static void d(Class<?> type, String message) {
		if(logLevel.ordinal() < 2) {
			Log.i(type.getName(), message);
		}
	}
	public static void i(String tag, String message) {
		if(logLevel.ordinal() < 3) {
			Log.i(tag, message);
		}
	}
	public static void i(Class<?> type, String message) {
		if(logLevel.ordinal() < 3) {
			Log.i(type.getName(), message);
		}
	}
	public static void w(String tag, String message) {
		if(logLevel.ordinal() < 4) {
			Log.w(tag, message);
		}
	}
	public static void w(Class<?> type, String message) {
		if(logLevel.ordinal() < 4) {
			Log.w(type.getName(), message);
		}
	}
	public static void w(Exception e) {
		if(logLevel.ordinal() < 4) {
			Log.e(e.getMessage(), e.toString());
		}
	}
	public static void e(String tag, String message) {
		if(logLevel.ordinal() < 5) {
			Log.e(tag, message);
		}
	}
	public static void e(Class<?> type, String message) {
		if(logLevel.ordinal() < 5) {
			Log.e(type.getName(), message);
		}
	}
	public static void e(Exception e) {
		if(logLevel.ordinal() < 5) {
			Log.e(e.getMessage(), e.toString());
		}
	}
	public static void showException(Context context, Exception e) {
		ToastUtil.makeText(context, context.getClass().getName() + ":\n" + e.getMessage(), ToastUtil.LENGTH_LONG).show();
	}
	public static void showException(Context context, Exception e, String message) {
		ToastUtil.makeText(context, message + ":\n"+ context.getClass().getName() + ":\n" + e.getMessage(), ToastUtil.LENGTH_LONG).show();
	}
	public static boolean writeDebugLog(String locat, String tag, String message) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS a, z");
		FileOutputStream fileOut = null;
		PrintStream debugLogWrite = null;
		try {
			File logPath = new File(DEBUG_LOG_WRITE_PATH);
			if (logPath != null) {
				// 保证目录合法
				File parentDir = logPath.getParentFile();
				if (!parentDir.exists()) 
					parentDir.mkdirs();
				// 定期清理日志文件: Size > 128KB
				if (logPath.exists() && logPath.isFile() && logPath.length() > 131072)
					logPath.delete();
				// 写入日志
				fileOut = new FileOutputStream(DEBUG_LOG_WRITE_PATH, true);
				debugLogWrite = new PrintStream(fileOut, true, "UTF-8");
				debugLogWrite.printf("[%s] >> %s\nTAG: \"%s\" MSG: \"%s\"\n", dateFormat.format(new Date()), locat, tag, message);
			}
		} catch (FileNotFoundException e) {
			Logger.w(TAG, e.toString());
		} catch (UnsupportedEncodingException e) {
			Logger.w(TAG, e.toString());
		} finally {
			try {
				if (debugLogWrite != null)
					debugLogWrite.close();
				if (fileOut != null)
					fileOut.close();
			} catch (IOException e) {}
		}
		return true;
	}
	public static boolean writeDebugLog(Class<?> type, String tag, String message) {
		writeDebugLog(type.getName(), tag, message);
		return true;
	}
	public static boolean writeDebugLog(String tag, String message) {
		writeDebugLog("", tag, message);
		return true;
	}
}
