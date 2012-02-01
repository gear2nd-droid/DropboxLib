package jp.gear.DropboxLib.FileTest;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

import jp.gear.DropboxLib.DownloadService;
import jp.gear.DropboxLib.UploadService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DropboxLibActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "DropboxLibActivity";
	
	//	AppKey AppSecret 設定
	final static private String APP_KEY = "hogehoge_APP_KEY";		//	App Key
	final static private String APP_SECRET = "hogehoge_APP_SECRET";		//	App Secret
	//	AccessType アクセス可能な権限設定（登録したものしか選択できない）
	//	T:独自のフォルダのみ
	//	F:フルアクセス
	final static private boolean ACCESS_TYPE = true;
//	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;	// フルアクセス
//	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX		// 独自のフォルダのみ
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView text = new TextView(this);
		text.setText("申請しないと、アプリの所有者以外のアカウントからはアクセスできません。\n" +
				"Androidanifest.xmlとDropboxAuthActitiyに各種値を設定してください。\n");
		layout.addView(text);
		
		Button btn = new Button(this);
		btn.setText("ログインする");
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), TestDropboxActivity.class);
				intent.putExtra("APP_KEY", APP_KEY);
				intent.putExtra("APP_SECRET", APP_SECRET);
				intent.putExtra("ACCESS_TYPE", ACCESS_TYPE);
				v.getContext().startActivity(intent);
			}
		});
		layout.addView(btn);
		
		
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
					
					Intent upIntent = UploadService.createIntent(v.getContext(), localPath, dropboxPath);
					upIntent.putExtra("APP_KEY", APP_KEY);
					upIntent.putExtra("APP_SECRET", APP_SECRET);
					upIntent.putExtra("ACCESS_TYPE", ACCESS_TYPE);
					v.getContext().startService(upIntent);
				} catch (Exception e) {
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
				SharedPreferences prefs = v.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
				String path = prefs.getString("prev_dropbox_path", "");
				if(!path.equals("")){
					String fileName = path;
					String localPath = "/sdcard/" + fileName;
					String dropboxPath = fileName;
					
					Intent downService = DownloadService.createIntent(v.getContext(), localPath, dropboxPath);
					downService.putExtra("APP_KEY", APP_KEY);
					downService.putExtra("APP_SECRET", APP_SECRET);
					downService.putExtra("ACCESS_TYPE", ACCESS_TYPE);
					v.getContext().startService(downService);
				}
			}
		});
		layout.addView(btn);
		
		
		setContentView(layout);
	}
}