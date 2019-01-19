package com.tsukiseele.seelewallpaper.service;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Message;
import com.tsukiseele.seelewallpaper.app.debug.Logger;
import com.tsukiseele.seelewallpaper.utils.ToastUtil;
import java.io.File;
import java.io.FileNotFoundException;

public class WallpaperUpdateTask extends AsyncTask<String, Void, Message> {
	public static final int TYPE_ERROR = -1;
	public static final int TYPE_OK = 0;
	public static final int TYPE_NOTFOUND = 1;
	
	private Context context;
	private int width;
	private int height;
	private int mode;
	private int alpha;
	private boolean gray;
	
	public WallpaperUpdateTask(Context context, int width, int height, int mode, int alpha, boolean gray) {
		this.context = context;
		this.width = width;
		this.height = height;
		this.mode = mode;
		this.alpha = alpha;
		this.gray = gray;
	}
	
	@Override
	protected Message doInBackground(String[] wallpaperPath) {
		Bitmap bitmap = null;
		Bitmap wallpaper = null;
		Message msg = new Message();
		try {
			Logger.i(WallpaperUpdateTask.class, "======================");
			Logger.i(WallpaperUpdateTask.class, "文件 Path = " + wallpaperPath[0]);
			Logger.i(WallpaperUpdateTask.class, "窗口 Width = " + width);
			Logger.i(WallpaperUpdateTask.class, "窗口 Height = " + height);
			
			File wallpaperFile = new File(wallpaperPath[0]);
			if (!wallpaperFile.exists()) {
				throw new FileNotFoundException("壁纸未找到");
			}
			bitmap = BitmapFactory.decodeFile(wallpaperFile.getAbsolutePath());
			
			WallpaperManager wpm = WallpaperManager.getInstance(context);
			
			if (wpm != null && bitmap != null) {
				
				Logger.i(WallpaperUpdateTask.class, "原始壁纸 Width = " + bitmap.getWidth());
				Logger.i(WallpaperUpdateTask.class, "原始壁纸 Height = " + bitmap.getHeight());
				Logger.i(WallpaperUpdateTask.class, "原始壁纸 Ratio = " + (double) bitmap.getWidth() / bitmap.getHeight());
				
				switch (mode) {
					case WallpaperService.MODE_SIMPLE :
						wallpaper = centerCropBitmap(bitmap, width, height);	
						break;
					case WallpaperService.MODE_SCROLL :
						// wallpaper = centerBitmap(bitmap, width, height);
						wallpaper = centerCropBitmap(bitmap, width, height);
						break;
					case WallpaperService.MODE_SCROLL_FIXED :
						wallpaper = centerWidth2xBitmap(bitmap, width, height);
						break;
					default :
						wallpaper = centerCropBitmap(bitmap, width, height);
						break;
				}
				if (gray)
					wallpaper = grayBitmap(bitmap);
				wallpaper = createTransparentBitmap(wallpaper, alpha);
				
				if (wallpaper != null)
					wpm.setBitmap(wallpaper);
				
				Logger.i(WallpaperUpdateTask.class, "裁剪壁纸 Width = " + wallpaper.getWidth());
				Logger.i(WallpaperUpdateTask.class, "裁剪壁纸 Height = " + wallpaper.getHeight());
				Logger.i(WallpaperUpdateTask.class, "裁剪壁纸 Ratio = " + (double) wallpaper.getWidth() / wallpaper.getHeight());
				
				msg.what = TYPE_OK;
				msg.obj = wallpaperFile;
			} else {
				msg.what = TYPE_ERROR;
				msg.obj = new NullPointerException("无法获取壁纸管理器");
			}
		} catch (FileNotFoundException e) {
			msg.what = TYPE_NOTFOUND;
			msg.obj = e;
		} catch (Exception e) {
			// 失败时使用备用路径重试
			if (wallpaperPath[1] != null) {
				new WallpaperUpdateTask(context, width, height, mode, alpha, gray)
					.execute(wallpaperPath[1], null);
			} else {
				msg.what = TYPE_ERROR;
				msg.obj = e;
			}
		} finally {
			recyclerBitmap(bitmap);
			recyclerBitmap(wallpaper);
		}
		return msg;
	}
	@Override
	protected void onPostExecute(Message result) {
		switch (result.what) {
			case TYPE_OK :
				String message;
				if (result.obj == null)
					message = "null result";
				else
					message = result.obj.toString();
				Logger.writeDebugLog(WallpaperUpdateTask.class, "WallpaperUpdateEvent", message);
				break;
			case TYPE_NOTFOUND :
				if (result.obj != null)
					ToastUtil.makeText(context, "找不到壁纸：" + ((Exception) result.obj).toString(), ToastUtil.LENGTH_SHORT).show();
				break;
			case TYPE_ERROR :
				if (result.obj != null)
					ToastUtil.makeText(context, "更改失败，壁纸可能存在问题：" + ((Exception) result.obj).toString(), ToastUtil.LENGTH_SHORT).show();
				break;
		}
		context = null;
		System.gc();
	}
	// 将图片以最大限度居中剪切，并改变为最佳分辨率
	private Bitmap centerCropBitmap(Bitmap bitmap, int width, int height) {
		int mapWidth = bitmap.getWidth();
		int mapHeight = bitmap.getHeight();

		double ratio = (double) width / height;
		double mapRatio = (double) mapWidth / mapHeight;
		// 使用长宽比进行裁剪
		if (mapRatio > ratio) {
			mapWidth = (int) (mapHeight * ratio);
		} else {
			mapHeight = (int) (mapWidth / ratio);
		}
		// 居中裁剪的X轴坐标
		int x = bitmap.getWidth() / 2 - mapWidth / 2;
		int y = bitmap.getHeight() / 2 - mapHeight / 2;
		Bitmap bmp = Bitmap.createBitmap(bitmap, x, y, mapWidth, mapHeight);
		Bitmap b = Bitmap.createScaledBitmap(bmp, width, height, true);
		
		return b;
	}
	private Bitmap grayBitmap(Bitmap bitmap) {
		int[] argb = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(argb, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
		for (int i = 0; i < argb.length; i++) {
			int r = argb[i] >> 16 & 0x000000FF;
			int g = argb[i] >> 8 & 0x000000FF;
			int b = argb[i] & 0x000000FF;
			int gray = (r + g + b) / 3;
			argb[i] = (argb[i] & 0xFF00FFFF) | gray << 16;
			argb[i] = (argb[i] & 0xFFFF00FF) | gray << 8;
			argb[i] = (argb[i] & 0xFFFFFF00) | gray;
		}
		return Bitmap.createBitmap(argb, bitmap.getWidth(), bitmap
								   .getHeight(), Bitmap.Config.ARGB_8888);
	}
	/*
	private Bitmap centerBitmap(Bitmap bitmap, int width, int height) {
		int mapWidth = bitmap.getWidth();
		int mapHeight = bitmap.getHeight();
		
		double ratio = (double) width / height;
		double mapRatio = (double) mapWidth / mapHeight;
		
		Bitmap bmp = null;
		// 使用长宽比进行裁剪
		if (mapRatio > ratio) {
			// 图片比屏幕宽
			if (mapHeight > height) {
				double proportion = (double) mapHeight / height;
				mapHeight = height;
				mapWidth = (int) (mapWidth / proportion);
				bmp = Bitmap.createScaledBitmap(bitmap, mapWidth, mapHeight, true);
				if (bmp == null) {
					bmp = bitmap;
				}
			} else {
				bmp = bitmap;
			}
		} else {
			bmp = centerCropBitmap(bitmap, width, height);
		}
		return bmp;
	}*/
	
	private Bitmap centerWidth2xBitmap(Bitmap bitmap, int width, int height) {
		return centerCropBitmap(bitmap, width * 2, height);
	}
	public static Bitmap createTransparentBitmap(Bitmap sourceImg, int alpha) {  
        if (alpha >= 100 || alpha < 0)
			return sourceImg;
		alpha *= 255 / 100;  
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];  
		
        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg
							.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值  
        
        for (int i = 0; i < argb.length; i++) 
            argb[i] = (alpha << 24) | (argb[i] & 0x00FFFFFF);  
			
        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg  
										.getHeight(), Bitmap.Config.ARGB_8888);
        return sourceImg;  
    }
	/*
	public static Bitmap createTransparentBitmap(Bitmap sourceImg, int alpha, int rad, int green, int blue) {  
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];  

        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg
				.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值  

        alpha = alpha * 255 / 100;

        for (int i = 0; i < argb.length; i++) {  
            argb[i] = (alpha << 24) | (argb[i] & 0x00FFFFFF);  
			argb[i] = (argb[i] & 0xFF00FFFF) | (argb[i] & 0x00FF0000 * rad / 255);
			argb[i] = (argb[i] & 0xFFFF00FF) | (argb[i] & 0x0000FF00 * green / 255);
			argb[i] = (argb[i] & 0xFFFFFF00) | (argb[i] & 0x000000FF * blue / 255);
        }  
        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg  
				.getHeight(), Bitmap.Config.ARGB_8888);
        return sourceImg;  
    }  
	*/
	
	public static void recyclerBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
		bitmap = null;
	}
}


