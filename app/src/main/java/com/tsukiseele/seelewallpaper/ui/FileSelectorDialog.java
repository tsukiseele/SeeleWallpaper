package com.tsukiseele.seelewallpaper.ui;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.tsukiseele.seelewallpaper.R;
import com.tsukiseele.seelewallpaper.utils.ToastUtil;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayDeque;
import java.util.Deque;

public class FileSelectorDialog extends DialogFragment {
	private Context context;
	private View layout;
	
	private ListView directoryListView;
	private Button upperDirButton;
	private Button checkedButton;
	private TextView dialogSubtitleTextView;
	
	private File parentDir;
	private File[] dirs;
	private Deque<File> dirStack = new ArrayDeque<>();
	
	private OnDialogDataCallback onDialogDataCallback;
	public interface OnDialogDataCallback {
		void onDataCallback(Object obj)
	}
	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.fragmentFileSelectorDialog_Button :
					if (onDialogDataCallback != null) {
						onDialogDataCallback.onDataCallback(parentDir);
					}
					dismiss();
					break;
				case R.id.fragmentFileSelectorDialogUpper_Button :
					if (!dirStack.isEmpty()) {
						// 弹出一层目录栈
						intoChildDir(parentDir = dirStack.pop());
						// 更新子标题
						dialogSubtitleTextView.setText(parentDir.getAbsolutePath());
					} else {
						ToastUtil.makeText(context, "已经是根♂目录了", ToastUtil.LENGTH_SHORT).show();
					}
					break;
			}
		}
	};
	public FileSelectorDialog(File parentDirName, OnDialogDataCallback onDialogDataCallback) {
		this.parentDir = parentDirName;
		this.onDialogDataCallback = onDialogDataCallback;
	}
	@Override
	public void onStart() {
		super.onStart();
		// 改变窗口尺寸
		DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		Window window = getDialog().getWindow();
		int width = (int) (dm.widthPixels * 0.85f);
		int height = window.getAttributes().height;
        window.setLayout(width, height);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().requestWindowFeature(getActivity().getWindow().FEATURE_NO_TITLE);
		layout = inflater.inflate(R.layout.fragment_fileselector_dialog, null);
		context = getActivity();
		
		directoryListView = (ListView) layout.findViewById(R.id.fragmentFileSelectorDialog_ListView);
		upperDirButton = (Button) layout.findViewById(R.id.fragmentFileSelectorDialogUpper_Button);
		checkedButton = (Button) layout.findViewById(R.id.fragmentFileSelectorDialog_Button);
		dialogSubtitleTextView = (TextView) layout.findViewById(R.id.fragmentFileSelectorDialogSubtitle_TextView);
		
		upperDirButton.setOnClickListener(onClickListener);
		checkedButton.setOnClickListener(onClickListener);
		
		directoryListView.setOnItemClickListener(new ListView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapter, View item, int pos, long p4) {
					if (dirs[pos].isDirectory()) {
						// 压入一层目录栈
						dirStack.push(parentDir);
						// 改变父目录
						parentDir = dirs[pos];
						// 推进
						intoChildDir(parentDir);
						// 更新子标题
						dialogSubtitleTextView.setText(parentDir.getAbsolutePath());
					}
				}
		});
		intoChildDir(parentDir);
		
		return layout;
	}
	// 进入目录
	private void intoChildDir(final File parentDirName) {
		dirs = parentDirName.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					String[] suffixs = new String[] {
						".jpg", ".jpeg", ".png", ".bmp"
					};
					if (file.isDirectory()) {
						return true;
					} else if (file.isFile()) {
						for (String suffix : suffixs) {
							if (file.getName().endsWith(suffix))
								return true;
						}
					}
					return false;
				}
			});
		// 更新列表
		directoryListView.setAdapter(new DirectoryaAdapter(context, dirs));
		// 更新子标题
		dialogSubtitleTextView.setText(parentDir.getAbsolutePath());
		
	}
}
