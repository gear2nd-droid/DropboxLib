package jp.gear.DropboxLib;

import android.os.Environment;

public class DropboxUtils {
	@SuppressWarnings("unused")
	private static final String TAG = "DropboxUtils";
	
	public static String getPath_sdcarddata(String packageName, String filePath) {
		String path = Environment.getExternalStorageDirectory().getPath() + 
				"/Android/data/" + packageName + "/" + filePath;
		return path;
	}
	
	public static String getPath_sdcardroot(String packageName, String filePath) {
		String path = Environment.getExternalStorageDirectory().getPath();
		return path;
	}
	
	public static String getPath_data(String packageName, String filePath) {
		String path = "/data/data/" + packageName + "/" + filePath;
		return path;
	}
	
	public static String getPath_dropbox(String appName, String filePath) {
		String path = "/アプリ/" + appName + "/" + filePath;
		return path;
	}
}
