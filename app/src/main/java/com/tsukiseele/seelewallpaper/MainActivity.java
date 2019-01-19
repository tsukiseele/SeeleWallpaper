package com.tsukiseele.seelewallpaper;

import android.widget.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.tsukiseele.seelewallpaper.service.WallpaperService;
import com.tsukiseele.seelewallpaper.ui.FileSelectorDialog;
import com.tsukiseele.seelewallpaper.utils.ToastUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor preferencesEditor;
	
	private EditText intervalEditText;
	private EditText wallpaperPathEditText;
	private Button startButton;
	private Button stopButton;
	private Button dirSelectButton;
	private RadioGroup modeRadioGroup;
	private Switch strictSwitch;
	private Switch compatSwitch;
	private Switch sleepModeSwitch;
	private Switch systemBootSwitch;
	private CheckBox timerSwitchCheckBox;
	private CheckBox unlockSwitchCheckBox;
	private LinearLayout customWHLayout;
	private CheckBox customWHCheckBox;
	private EditText customWidthEditText;
	private EditText customHeightEditText;
	private Spinner updateModeSpinner;
	private SeekBar alphaSeekbar;
	private TextView alphaTextView;
	private Switch graySwitch;
	private boolean isCompatMode = false;
	private boolean isCustomWH = false;
	
	private int windowWidth = -1;
	private int windowHeight = -1;
	
	private FileSelectorDialog.OnDialogDataCallback onDialogDataCallback = new FileSelectorDialog.OnDialogDataCallback() {
		@Override
		public void onDataCallback(Object obj) {
			File path = (File) obj;
			wallpaperPathEditText.setText(path.getAbsolutePath());
		}
	};
	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.mainLayoutStart_Button :	
					// 写入配置
					String filePath = wallpaperPathEditText.getText().toString().trim();
					int intervalTime = 600;
					try {
						intervalTime = Integer.valueOf(intervalEditText.getText().toString());
						if (intervalTime < 10) intervalTime = 10;
					} catch (NumberFormatException e) {
						intervalTime = 600;
						intervalEditText.setText(String.valueOf(intervalTime));
						ToastUtil.makeText(MainActivity.this, "数值超过限制", ToastUtil.LENGTH_SHORT).show();
					}
					int width = -1;
					int height = -1;
					if (!isCustomWH) {
						// 是否为兼容模式
						if (isCompatMode) {
							width = windowWidth;
							height = windowHeight;
						} else {
							width = getWallpaperDesiredMinimumWidth();
							height = getWallpaperDesiredMinimumHeight();
						}
					} else {
						try {
							width = Integer.valueOf(customWidthEditText.getText().toString());
							height = Integer.valueOf(customHeightEditText.getText().toString());
						} catch (NumberFormatException e) {
							customWidthEditText.setText(String.valueOf(windowWidth));
							customHeightEditText.setText(String.valueOf(windowHeight));
							width = windowWidth;
							height = windowHeight;
							ToastUtil.makeText(MainActivity.this, "数值超过限制", ToastUtil.LENGTH_SHORT).show();
						}
					}
					preferencesEditor.putString(WallpaperService.TYPE_WALLPAPER_PATH, filePath);
					preferencesEditor.putInt(WallpaperService.TYPE_INTERVAL_TIME, intervalTime);
					preferencesEditor.putInt(WallpaperService.TYPE_WINDOW_WIDTH, width);
					preferencesEditor.putInt(WallpaperService.TYPE_WINDOW_HEIGHT, height);
					preferencesEditor.putBoolean(WallpaperService.TYPE_STATE, true);
					preferencesEditor.apply();
					// 启动服务
					Intent intent = new Intent(MainActivity.this, WallpaperService.class);
					startService(intent);
					
					ToastUtil.makeText(MainActivity.this, "服务已开启", ToastUtil.LENGTH_SHORT).show();
					break;
					
				case R.id.mainLayoutStop_Button :
					if (WallpaperService.getInstance() != null)
						WallpaperService.getInstance().cancel();
					preferencesEditor.putBoolean(WallpaperService.TYPE_STATE, false);
					preferencesEditor.commit();
					ToastUtil.makeText(MainActivity.this, "服务成功关闭", ToastUtil.LENGTH_SHORT).show();
					break;
					
				case R.id.mainLayoutDiecrtorySelect_Button :
					FileSelectorDialog dialog = new FileSelectorDialog(Environment.getExternalStorageDirectory(), onDialogDataCallback);
					dialog.show(getFragmentManager(), "选择目录");
					break;
			}
		}
	};
	private RadioGroup.OnCheckedChangeListener rgOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup radioGroup, int id) {
			switch (id) {
				case R.id.mainLayoutSimpleType_RadioButton :
					preferencesEditor.putInt(WallpaperService.TYPE_WALLPAPER_TYPE, WallpaperService.MODE_SIMPLE);
					break;
				case R.id.mainLayoutScrollType_RadioButton :
					preferencesEditor.putInt(WallpaperService.TYPE_WALLPAPER_TYPE, WallpaperService.MODE_SCROLL);
					break;
				case R.id.mainLayoutScrollFixed_RadioButton :
					preferencesEditor.putInt(WallpaperService.TYPE_WALLPAPER_TYPE, WallpaperService.MODE_SCROLL_FIXED);
			}
			preferencesEditor.apply();
		}
	};
	private CompoundButton.OnCheckedChangeListener cbOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton button, boolean bool) {
			switch (button.getId()) {
				case R.id.mainLayoutStrict_Switch :
					preferencesEditor.putBoolean(WallpaperService.TYPE_STRICT_MODE, bool);
					break;
				case R.id.mainLayoutCompat_Switch :
					isCompatMode = bool;
					preferencesEditor.putBoolean(WallpaperService.TYPE_COMPAT_MODE, bool);
					break;
				case R.id.mainLayoutSleepMode_Switch :
					preferencesEditor.putBoolean(WallpaperService.TYPE_SLEEP_MODE, bool);
					break;
				case R.id.activityMainBoot_Switch :
					preferencesEditor.putBoolean(WallpaperService.TYPE_SYSTEM_BOOT, bool);
					break;
				case R.id.mainLayoutCustomWH_CheckBox :
					isCustomWH = bool;
					if (bool)
						customWHLayout.setVisibility(View.VISIBLE);
					else
						customWHLayout.setVisibility(View.GONE);
					break;
				case R.id.activityMainTimerSwitch_CheckBox :
					preferencesEditor.putBoolean(WallpaperService.TYPE_SWITCH_TIMER, bool);
					break;
				case R.id.activityMainUnlockSwitch_CheckBox :
					preferencesEditor.putBoolean(WallpaperService.TYPE_SWITCH_UNLOCK, bool);
					break;
				case R.id.activityMainGray_Switch :
					preferencesEditor.putBoolean(WallpaperService.TYPE_GRAY_BITMAP, bool);
					break;
			}
			preferencesEditor.apply();
		}
	};
	private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
			switch(pos) {
				case 0 :
					preferencesEditor.putInt(WallpaperService.TYPE_UPDATE_MODE, WallpaperService.UPDATE_MODE_ORDER);
					break;
				case 1 :
					preferencesEditor.putInt(WallpaperService.TYPE_UPDATE_MODE, WallpaperService.UPDATE_MODE_RANDOM);
					break;
			}
			preferencesEditor.apply();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapter) {
			
		}
	};
	private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean p3) {
			alphaTextView.setText(progress + "%");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			alphaTextView.setText(seekBar.getProgress() + "%");
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekbar) {
			preferencesEditor.putInt(WallpaperService.TYPE_ALPHA, seekbar.getProgress());
			preferencesEditor.apply();
		}
	};
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 获取窗口大小
		DisplayMetrics dm = new DisplayMetrics();  
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		windowWidth = dm.widthPixels; 
		windowHeight = dm.heightPixels;
		
		sharedPreferences = getSharedPreferences(WallpaperService.SETTING_FILENAME, Context.MODE_PRIVATE);
		preferencesEditor = sharedPreferences.edit();
		
		wallpaperPathEditText = (EditText) this.findViewById(R.id.mainLayoutWallpaperPath_EditText);
		intervalEditText = (EditText) this.findViewById(R.id.mainLayoutIntervalTime_EditText);
		startButton = (Button) this.findViewById(R.id.mainLayoutStart_Button);
		stopButton = (Button) this.findViewById(R.id.mainLayoutStop_Button);
		modeRadioGroup = (RadioGroup) this.findViewById(R.id.mainLayout_RadioGroup);
		dirSelectButton = (Button) this.findViewById(R.id.mainLayoutDiecrtorySelect_Button);
		strictSwitch = (Switch) this.findViewById(R.id.mainLayoutStrict_Switch);
		compatSwitch = (Switch) this.findViewById(R.id.mainLayoutCompat_Switch);
		sleepModeSwitch = (Switch) this.findViewById(R.id.mainLayoutSleepMode_Switch);
		systemBootSwitch = (Switch) this.findViewById(R.id.activityMainBoot_Switch);
		timerSwitchCheckBox = (CheckBox) this.findViewById(R.id.activityMainTimerSwitch_CheckBox);
		unlockSwitchCheckBox = (CheckBox) this.findViewById(R.id.activityMainUnlockSwitch_CheckBox);
		customWHLayout = (LinearLayout) this.findViewById(R.id.mainLayoutCustomWH_LinearLayout);
		customWHCheckBox = (CheckBox) this.findViewById(R.id.mainLayoutCustomWH_CheckBox);
		customWidthEditText = (EditText) this.findViewById(R.id.mainLayoutCustomWidth_EditText);
		customHeightEditText = (EditText) this.findViewById(R.id.mainLayoutCustomHeight_EditText);
		updateModeSpinner = (Spinner) this.findViewById(R.id.mainLayoutUpdateMode_Spinner);
		alphaSeekbar = (SeekBar) this.findViewById(R.id.activityMainAlpha_SeekBar);
		alphaTextView = (TextView) this.findViewById(R.id.activityMainAlpha_TextView);
		graySwitch = (Switch) this.findViewById(R.id.activityMainGray_Switch);
		
		String[] items = new String[] {"顺序切换", "随机切换"};
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		updateModeSpinner.setAdapter(arrayAdapter);
		
		loadConfig();
		
		modeRadioGroup.setOnCheckedChangeListener(rgOnCheckedChangeListener);
		
		dirSelectButton.setOnClickListener(onClickListener);
		startButton.setOnClickListener(onClickListener);
		stopButton.setOnClickListener(onClickListener);
		
		strictSwitch.setOnCheckedChangeListener(cbOnCheckedChangeListener);
		compatSwitch.setOnCheckedChangeListener(cbOnCheckedChangeListener);
		sleepModeSwitch.setOnCheckedChangeListener(cbOnCheckedChangeListener);
		systemBootSwitch.setOnCheckedChangeListener(cbOnCheckedChangeListener);
		timerSwitchCheckBox.setOnCheckedChangeListener(cbOnCheckedChangeListener);
		unlockSwitchCheckBox.setOnCheckedChangeListener(cbOnCheckedChangeListener);
		customWHCheckBox.setOnCheckedChangeListener(cbOnCheckedChangeListener);
		graySwitch.setOnCheckedChangeListener(cbOnCheckedChangeListener);
		
		updateModeSpinner.setOnItemSelectedListener(onItemSelectedListener);
		
		alphaSeekbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menuAbout :
				showAbouDialog();
				break;
		}
		return true;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU :
				// 菜单键直接显示帮助
				showAbouDialog();
				return true;
			case KeyEvent.KEYCODE_HOME :
				onBackPressed();
				return super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
	// 返回键直接销毁
	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void finish() {
		super.finish();
		onDestroy();
	}
	@Override
	protected void onStop() {
		super.onStop();
		onDestroy();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	private void showAbouDialog() {
		StringBuilder stringBuilder = new StringBuilder();
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = getAssets().open("About.txt");
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null)
				stringBuilder.append(line + "\n");
		} catch (IOException e) {
			ToastUtil.makeText(this, "帮助文档损坏", ToastUtil.LENGTH_SHORT).show();
		}
		new AlertDialog.Builder(this)
			.setTitle("使用帮助")
			.setMessage(stringBuilder.toString())
			.setPositiveButton("哇嘎哒哟", null)
			.show();
	}
	
	private void loadConfig() {
		// 读取载入配置
		if (sharedPreferences != null) {
			String wallpaperDir = sharedPreferences.getString(WallpaperService.TYPE_WALLPAPER_PATH, "");
			int intervalTime = sharedPreferences.getInt(WallpaperService.TYPE_INTERVAL_TIME, 600);
			int wallpaperType = sharedPreferences.getInt(WallpaperService.TYPE_WALLPAPER_TYPE, WallpaperService.MODE_SIMPLE);
			boolean isStrictMode = sharedPreferences.getBoolean(WallpaperService.TYPE_STRICT_MODE, true);
			boolean isCompatMode = sharedPreferences.getBoolean(WallpaperService.TYPE_COMPAT_MODE, false);
			boolean isSleepMode = sharedPreferences.getBoolean(WallpaperService.TYPE_SLEEP_MODE, true);
			boolean isSystemBoot = sharedPreferences.getBoolean(WallpaperService.TYPE_SYSTEM_BOOT, false);
			boolean isCustomWHMode = sharedPreferences.getBoolean(WallpaperService.TYPE_CUSTOM_WH_MODE, false);
			int mWindowWidth = sharedPreferences.getInt(WallpaperService.TYPE_WINDOW_WIDTH, windowWidth);
			int mWindowHeight = sharedPreferences.getInt(WallpaperService.TYPE_WINDOW_HEIGHT, windowHeight);
			int updateMode = sharedPreferences.getInt(WallpaperService.TYPE_UPDATE_MODE, WallpaperService.UPDATE_MODE_ORDER);
			boolean isSwitchTimer = sharedPreferences.getBoolean(WallpaperService.TYPE_SWITCH_TIMER, true);
			boolean isSwitchUnlock = sharedPreferences.getBoolean(WallpaperService.TYPE_SWITCH_UNLOCK, false);
			int alpha = sharedPreferences.getInt(WallpaperService.TYPE_ALPHA, 80);
			boolean isGray = sharedPreferences.getBoolean(WallpaperService.TYPE_GRAY_BITMAP, false);
			
			wallpaperPathEditText.setText(wallpaperDir);
			intervalEditText.setText(String.valueOf(intervalTime));
			customWidthEditText.setText(String.valueOf(mWindowWidth));
			customHeightEditText.setText(String.valueOf(mWindowHeight));
			strictSwitch.setChecked(isStrictMode);
			compatSwitch.setChecked(isCompatMode);
			sleepModeSwitch.setChecked(isSleepMode);
			systemBootSwitch.setChecked(isSystemBoot);
			timerSwitchCheckBox.setChecked(isSwitchTimer);
			unlockSwitchCheckBox.setChecked(isSwitchUnlock);
			customWHCheckBox.setChecked(isCustomWHMode);
			alphaSeekbar.setProgress(alpha);
			alphaTextView.setText(alpha + "%");
			graySwitch.setChecked(isGray);
			
			switch (wallpaperType) {
				case WallpaperService.MODE_SIMPLE :
					modeRadioGroup.check(R.id.mainLayoutSimpleType_RadioButton);
					break;
				case WallpaperService.MODE_SCROLL :
					modeRadioGroup.check(R.id.mainLayoutScrollType_RadioButton);
					break;
				case WallpaperService.MODE_SCROLL_FIXED :
					modeRadioGroup.check(R.id.mainLayoutScrollFixed_RadioButton);
					break;
			}
			switch(updateMode) {
				case WallpaperService.UPDATE_MODE_ORDER :
					updateModeSpinner.setSelection(0);
					break;
				case WallpaperService.UPDATE_MODE_RANDOM :
					updateModeSpinner.setSelection(1);
					break;
				default :
					updateModeSpinner.setSelection(0);
					break;
			}
		}
	}
}

