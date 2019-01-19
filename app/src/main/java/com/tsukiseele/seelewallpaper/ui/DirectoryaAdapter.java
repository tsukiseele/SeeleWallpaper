package com.tsukiseele.seelewallpaper.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.tsukiseele.seelewallpaper.R;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class DirectoryaAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private File[] directoryArray;
	public DirectoryaAdapter(Context context, File[] directoryArray) {
		inflater = LayoutInflater.from(context);
		
		Arrays.sort(directoryArray, new Comparator<File>() {
				@Override
				public int compare(File p1, File p2) {
					if(p1.isDirectory() && p2.isFile())
						return -1;
					else if(p1.isFile() && p2.isDirectory())
						return 1;
					else if(p1.isDirectory() && p2.isDirectory()) 
						return p1.getName().compareToIgnoreCase(p2.getName());
					else
						return 0;
				}
		});
		this.directoryArray = directoryArray;
	}

	public void setdirectoryArray(File[] directoryArray) {
		this.directoryArray = directoryArray;
		notifyDataSetChanged();
	}

	public File[] getdirectoryArray() {
		return directoryArray;
	}
	
	@Override
	public int getCount() {
		return directoryArray.length;
	}

	@Override
	public Object getItem(int pos) {
		return directoryArray[pos];
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int pos, View item, ViewGroup parent) {
		item = inflater.inflate(R.layout.item_fileselect_layout, null);
		File file = directoryArray[pos];
		TextView fileName = (TextView) item.findViewById(R.id.itemFileSelectLayoutFileName_TextView);
		fileName.setText(file.getName());
		if (file.isFile()) {
			fileName.setTextColor(Color.BLUE);
		}
		return item;
	}
}
