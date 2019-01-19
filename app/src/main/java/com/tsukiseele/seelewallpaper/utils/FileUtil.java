package com.tsukiseele.seelewallpaper.utils;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

public class FileUtil {
	public static void writeText(String text, String path, String character) throws FileNotFoundException, IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), character));
		bw.write(text);
		bw.close();
	}
	public static void writeText(String text, String path) throws FileNotFoundException, IOException {
		writeText(text, path, "UTF-8");
	}
	public static String readText(String path, String character) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), character));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while (null != (line = br.readLine())) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}
	public static String readText(String path) throws FileNotFoundException, IOException {
		return readText(path, "UTF-8");
	}
}
