package jp.gear.DropboxLib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class UploadService extends DropboxAuthService {
//	@SuppressWarnings("unused")
	private static final String TAG = "UploadService";

	private String mLocalPath;
	private String mDropboxPath;
	private UploadRequest mRequest;
	private String mErrorMsg;
	@SuppressWarnings("unused")
	private long mFileLen;
	@SuppressWarnings("unused")
	private Intent intent = null;
	@SuppressWarnings("unused")
	private int startId;
	
	public static Intent createIntent(Context context, String localPath, String dropboxPath) {
		Intent intent = new Intent(context, UploadService.class);
		intent.putExtra("localPath", localPath);
		intent.putExtra("dropboxPath", dropboxPath);
		return intent;
	}

	@Override
	public void funcOnStart(Intent intent, int startId) {
		this.intent = intent;
		this.startId = startId;
		this.mLocalPath = intent.getStringExtra("localPath");
		this.mDropboxPath = intent.getStringExtra("dropboxPath");
		
		thread.start();
	}
	
	Thread thread = new Thread() {
		@Override
		public void run() {
			boolean endFlag = false;
			
			try {
				// By creating a request, we get a handle to the putFile operation,
				// so we can cancel it later if we want to
			//	FileInputStream fis = new FileInputStream();
			//	String path = mPath + mFile.getName();
				File inputFile = new File(mLocalPath);
				mFileLen = inputFile.length();
				FileInputStream inputStream = new FileInputStream(inputFile);
				mRequest = mApi.putFileOverwriteRequest(
						mDropboxPath, inputStream, inputFile.length(), null);

				if (mRequest != null) {
					mRequest.upload();
					endFlag = true;
				}
			} catch (DropboxUnlinkedException e) {
				// This session wasn't authenticated properly or user unlinked
				mErrorMsg = "This app wasn't authenticated properly.";
			} catch (DropboxFileSizeException e) {
				// File size too big to upload via the API
				mErrorMsg = "This file is too big to upload";
			} catch (DropboxPartialFileException e) {
				// We canceled the operation
				mErrorMsg = "Upload canceled";
			} catch (DropboxServerException e) {
				// Server-side exception. These are examples of what could happen,
				// but we don't do anything special with them here.
				if (e.error == DropboxServerException._401_UNAUTHORIZED) {
					// Unauthorized, so we should unlink them. You may want to
					// automatically log the user out in this case.
				} else if (e.error == DropboxServerException._403_FORBIDDEN) {
					// Not allowed to access this
				} else if (e.error == DropboxServerException._404_NOT_FOUND) {
					// path not found (or if it was the thumbnail, can't be
					// thumbnailed)
				} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
					// user is over quota
				} else {
					// Something else
				}
				// This gets the Dropbox error, translated into the user's language
				mErrorMsg = e.body.userError;
				if (mErrorMsg == null) {
					mErrorMsg = e.body.error;
				}
			} catch (DropboxIOException e) {
				// Happens all the time, probably want to retry automatically.
				mErrorMsg = "Network error.  Try again.";
			} catch (DropboxParseException e) {
				// Probably due to Dropbox server restarting, should retry
				mErrorMsg = "Dropbox error.  Try again.";
			} catch (DropboxException e) {
				// Unknown error
				mErrorMsg = "Unknown error.  Try again.";
			} catch (FileNotFoundException e) {
			}
			
			if(endFlag){
			//	Toast.makeText(getApplicationContext(), "Background upload end", Toast.LENGTH_LONG).show();
				Log.d(TAG, "Background upload end");
			}else{
			//	Toast.makeText(getApplicationContext(), "Background upload error", Toast.LENGTH_LONG).show();
				Log.d(TAG, "Background upload end");
			}
		}
	};
}
