package org.servalproject.rr;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * Rhizome Retriever main activity
 * 
 * @author rbochet
 */
public class Main extends Activity {

	/** TAG for debugging */
	public static final String TAG = "R2";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.v(TAG, "Launch the listing");
		listFiles();
	}

	/**
	 * List files of the directory serval on the SD Card
	 */
	private void listFiles() {

		File path = new File(Environment.getExternalStorageDirectory()
				+ "/serval-rhizome");
		Log.v(TAG, path.getAbsolutePath());

		// If the path exists, list all the non-hidden files (no dir)
		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return (sel.isFile() && !sel.isHidden());
				}
			};

			// List of the relative paths
			String[] fList = path.list(filter);
			// List of the RhizomeFile 
			RhizomeFile[] rList = new RhizomeFile[fList.length];
			
			for (int i = 0; i < rList.length; i++) {
				rList[i] = new RhizomeFile(path, fList[i]);
				Log.v(TAG, rList[i].toString());
			}
			
			
			
			
	
		} else { // The pass does not exist
			Log.e(TAG, "No serval-rhizome path found on the SD card.");
		}

	}
}