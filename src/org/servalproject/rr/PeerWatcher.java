/**
 * 
 */
package org.servalproject.rr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

/**
 * This class checks the peers Rhizome repository watching for new files.
 * 
 * @author rbochet
 */
public class PeerWatcher extends Thread {

	/** Time between two checks in milliseconds */
	private static final long SLEEP_TIME = 15 * 1000;
	
	/** TAG for debugging */
	public static final String TAG = "R2";

	/** If the thread works or not */
	private boolean run = true;


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		super.run();
		List<String> repos;
		// Works forever
		while (run) {
			Log.v(TAG, "Update procedure launched @ "+ new Date().toLocaleString());
			
			repos = getPeersRepo();
			
			for (String repo : repos) {
				// For each repo, download the interesting content
				new StuffDownloader(repo);
			}
			// Wait before the new lookup
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
			}
		}
		Log.i(TAG, "Updates stop.");
	}

	/**
	 * List the peers and return their URLs for using with StuffDownloader. TODO
	 * 
	 * @return The list of all the peers' servers.
	 */
	private List<String> getPeersRepo() {
		List<String> ret = new ArrayList<String>();
		ret.add("fake");
		return ret;
	}

	/** 
	 * Stop the thread on the next iteration.
	 */
	public void stopUpdate() {
		run = false;
	}

}
