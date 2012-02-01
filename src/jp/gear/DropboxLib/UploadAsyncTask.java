package jp.gear.DropboxLib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class UploadAsyncTask extends
		AsyncTask<Void, Long, Boolean> {
	@SuppressWarnings("unused")
	private static final String TAG = "UploadAsyncTask";

	private Context mContext;
	private DropboxAPI<?> mApi;
	private String mLocalPath;
	private String mDropboxPath;
	private boolean useDialog;
	private ProgressDialog mDialog;
	private UploadRequest mRequest;
	private String mErrorMsg;
	private long mFileLen;

	public UploadAsyncTask(Context context, DropboxAPI<?> api,
			String localPath, String dropboxPath, boolean useDialog) {
		this.mContext = context;
		this.mApi = api;
		this.mLocalPath = localPath;
		this.mDropboxPath = dropboxPath;
		this.useDialog = useDialog;
		// ProgressDialog
		this.mDialog = new ProgressDialog(this.mContext);
		this.mDialog.setMax(100);
		this.mDialog.setMessage("Uploading\n" + " LOCAL :" + this.mLocalPath
				+ "\n" + "   ↓\n" + "DROPBOX:" + this.mDropboxPath);
		this.mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.mDialog.setProgress(0);
		this.mDialog.setButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		if (this.useDialog) {
			this.mDialog.show();
		}
	}

	protected Boolean doInBackground(Void... params) {
		try {
			// By creating a request, we get a handle to the putFile operation,
			// so we can cancel it later if we want to
		//	FileInputStream fis = new FileInputStream();
		//	String path = mPath + mFile.getName();
			File inputFile = new File(this.mLocalPath);
			this.mFileLen = inputFile.length();
			FileInputStream inputStream = new FileInputStream(inputFile);
			mRequest = mApi.putFileOverwriteRequest(this.mDropboxPath, inputStream, inputFile.length(), 
					new ProgressListener() {
						@Override
						public long progressInterval() {
							// Update the progress bar every half-second or so
							return 500;
						}

						@Override
						public void onProgress(long bytes, long total) {
							publishProgress(bytes);
						}
					});

			if (mRequest != null) {
				mRequest.upload();
				return true;
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
		return false;
	}
	
	@Override
	protected void onProgressUpdate(Long... progress) {
		int percent = (int)(100.0 * (double)progress[0] / mFileLen + 0.5);
		this.mDialog.setProgress(percent);
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		this.mDialog.dismiss();
	}
}
