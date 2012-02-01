package jp.gear.DropboxLib.FileTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import jp.gear.DropboxLib.DownloadAsyncTask;
import jp.gear.DropboxLib.DropboxAuthActivity;
import jp.gear.DropboxLib.UploadAsyncTask;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestDropboxActivity extends DropboxAuthActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "TestDropboxActivity";
	
	UploadAsyncTask upTask = null;
	DownloadAsyncTask downTask = null;
	TestDropboxActivity act = null;

	@Override
	public boolean funcOnCreateBefore(Bundle savedInstanceState) {
		return true;
	}

	@Override
	public void funcOnCreateAfter(Bundle savedInstanceState) {
		this.act = this;
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		Button btn;
		TextView text;
		
		text = new TextView(this);
		SharedPreferences prefs = this.getSharedPreferences("prefs", Context.MODE_PRIVATE);
		String path = prefs.getString("prev_dropbox_path", "");
		text.setText(path);
		layout.addView(text);
		
		btn = new Button(this);
		btn.setText("Upload");
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendar = Calendar.getInstance();
				String fileName = String.format("uptest_%d_%d.txt", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
				String localPath = "/sdcard/" + fileName;
				String dropboxPath = fileName;
				File outFile = new File(localPath);
				try {
					OutputStream os = new FileOutputStream(outFile);
					String strData = fileName;
					os.write(strData.getBytes());
					os.close();
					
					upTask = new UploadAsyncTask(v.getContext(), 
							act.mApi, localPath, dropboxPath, true);
					upTask.execute();
					SharedPreferences prefs = act.getSharedPreferences("prefs", Context.MODE_PRIVATE);
					prefs.edit().putString("prev_dropbox_path", dropboxPath).commit();
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		layout.addView(btn);
		
		btn = new Button(this);
		btn.setText("Download");
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			//	SharedPreferences prefs = act.getSharedPreferences("prefs", Context.MODE_PRIVATE);
			//	String path = prefs.getString("prev_dropbox_path", "");
				String path = "uptest_20_48.txt";
				if(!path.equals("")){
					String fileName = path;
					String localPath = "/sdcard/" + fileName;
					String dropboxPath = fileName;
					downTask = new DownloadAsyncTask(v.getContext(), 
							act.mApi, localPath, dropboxPath, true);
					downTask.execute();
				}
			}
		});
		layout.addView(btn);
		this.setContentView(layout);
	}

	@Override
	protected boolean funcOnResumeBefore() {
		return true;
	}

	@Override
	protected void funcOnResumeAfter() {
		
	}

}
