package org.servalproject.rr;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Rhizome Retriever main activity. Extends ListActivity to be able to list the
 * files in a table.
 * 
 * 
 * @author rbochet
 */
public class Main extends ListActivity implements OnClickListener {

	/** The file picker dialog */
	private FolderPicker mFileDialog;

	/** TAG for debugging */
	public static final String TAG = "R2";

	/** The list of file names */
	private String[] fList = null;

	/** The list of logical files */
	private RhizomeFile[] rList = null;

	/** Directory where the files are exported */
	public static final File dirExport = new File(
			Environment.getExternalStorageDirectory()
					+ "/serval-rhizome-export");

	/** Rhizome's home directory */
	public static final File dirRhizome = new File(
			Environment.getExternalStorageDirectory() + "/serval-rhizome");

	/**
	 * Create a new key pair. Delete the old one if still presents.
	 */
	private void createKeyPair() {
		Log.e(TAG, "TODO : createKeyPair()");
	}

	/**
	 * Display a toast message in a short popup
	 * 
	 * @param text
	 *            The text displayed
	 */
	private void goToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * Import a file in the Rhizome directory
	 * 
	 * @param fileName
	 *            The path of the file we need to import
	 */
	private void importFile(String fileName) {
		try {
			File file = new File(fileName);
			RhizomeFile.CopyFileToDir(file, dirRhizome);
			// Reset the UI
			setUpUI();
			// Alright
			goToast("Success: " + file.getName() + " imported.");
		} catch (IOException e) {
			Log.e(TAG, "Importation failed.");
			goToast("Importation failed.");
		}
	}

	/**
	 * List files of the directory serval on the SD Card
	 */
	private void listFiles() {
		Log.v(TAG, dirRhizome.getAbsolutePath());

		// If the path exists, list all the non-hidden files (no dir)
		if (dirRhizome.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return (sel.isFile() && !sel.isHidden());
				}
			};

			// List of the relative paths
			fList = dirRhizome.list(filter);
			// List of the RhizomeFile
			rList = new RhizomeFile[fList.length];

			for (int i = 0; i < rList.length; i++) {
				rList[i] = new RhizomeFile(dirRhizome, fList[i]);
				Log.v(TAG, rList[i].toString());
			}

		} else { // The pass does not exist
			Log.e(TAG, "No serval-rhizome path found on the SD card.");
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Log.v(TAG, "Context menu pressed: " + info.id);
		switch (item.getItemId()) {

		// Delete the file
		case R.id.cm_delete:
			try {
				rList[(int) info.id].delete();
				// Need also to reset the UI
				setUpUI();

				goToast("Deletion successed.");
			} catch (IOException e1) {
				Log.e(TAG, "Deletion failed.");
				goToast("Deletion failed.");
			}
			return true;
			// Export the file in a known folder
		case R.id.cm_export:
			try {
				rList[(int) info.id].export();
				goToast("Export successed.");
			} catch (IOException e) { // The copy failed. Warn the user
				Log.e(TAG, "Export failed.");
				goToast("Export failed.");
			}
			return true;
		case R.id.cm_mark:
			rList[(int) info.id].markForExpiration();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setUpUI();

	}

	/**
	 * Set up the interface based on the list of files
	 */
	private void setUpUI() {
		listFiles();

		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, fList));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		// The click behavior
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.v(TAG, rList[(int) id].getFile().toString());

				// Process the click
				processClick(position, id);

			}
		});

		// Register the context menu
		registerForContextMenu(getListView());

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.m_import:
			mFileDialog = new FolderPicker(this, this, android.R.style.Theme,
					true);
			mFileDialog.show();
			return true;
		case R.id.m_new_keys:
			createKeyPair();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Process a short click received from the list view
	 * 
	 * @param position
	 *            The pos of the view in the adapter
	 * @param id
	 *            The id in the list view.
	 */
	private void processClick(int position, long id) {
		try {
			Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse("file://"
							+ rList[(int) id].getFile().getAbsolutePath()));

			startActivity(myIntent);
		} catch (Exception e) {
			Log.e(TAG, "Not possible to resolve this intent.s");
			goToast("This file cannot be opened from here.");
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == mFileDialog) { // security, not really needed
			String path = mFileDialog.getPath();
			importFile(path);
		}

	}
}