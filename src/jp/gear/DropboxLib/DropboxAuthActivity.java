package jp.gear.DropboxLib;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public abstract class DropboxAuthActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "DropboxAuthActivity";
	
	private String APP_KEY = null;
	private String APP_SECRET = null;
	private AccessType ACCESS_TYPE = null;		//	T:独自のフォルダのみ
												//	F:フルアクセス
	//	Preference に保存するためのKEY
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	//	Dropbox API
	protected DropboxAPI<AndroidAuthSession> mApi;
	
	public abstract boolean funcOnCreateBefore(Bundle savedInstanceState);
	public abstract void funcOnCreateAfter(Bundle savedInstanceState);
	protected abstract boolean funcOnResumeBefore();
	protected abstract void funcOnResumeAfter();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boolean flag = this.funcOnCreateBefore(savedInstanceState);
		
		//	アプリのキーなどを取得
		Intent intent = this.getIntent();
		this.APP_KEY = intent.getStringExtra("APP_KEY");
		this.APP_SECRET = intent.getStringExtra("APP_SECRET");
		if(intent.getBooleanExtra("ACCESS_TYPE", true)){
			this.ACCESS_TYPE = AccessType.APP_FOLDER;
		}else{
			this.ACCESS_TYPE = AccessType.DROPBOX;
		}

		//	Dropbox へ認証
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		if (!mApi.getSession().isLinked()) { // 認証済みでなかったら
			// 以下を実行することで、AuthActivityが呼び出されます
			mApi.getSession().startAuthentication(this);
		}
		
		if(flag){
			this.funcOnCreateAfter(savedInstanceState);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		boolean flag = this.funcOnResumeBefore();
		
		AndroidAuthSession session = mApi.getSession();
		// session.startAuthentication() に対応した
		// session.finishAuthentication() を呼んでやる必要があります
		if (session.authenticationSuccessful()) {
			try {
				session.finishAuthentication();

				// session key を保存
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
			} catch (IllegalStateException e) {
				// TODO: エラー処理
			}
		}
		
		if(flag){
			this.funcOnResumeAfter();
		}
	}
	
	/**
	 * 明示的にLogOutしないと、最初に使用したアカウントが使用され続けるので、 ユーザにlogOutさせたいときに、このメソッドを呼ぶ
	 */
	@SuppressWarnings("unused")
	private void logOut() {
		mApi.getSession().unlink();
		clearKeys();
	}

	/**
	 * Preference に保存されているKeyがある場合は、それを返す ない場合は null
	 * @return Array of [access_key, access_secret], or null if none stored
	 */
	private String[] getKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Preference に Key を保存する
	 */
	private void storeKeys(String key, String secret) {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	/**
	 * Preference の Key を削除する
	 */
	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}
		return session;
	}
}
