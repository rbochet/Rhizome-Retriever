/**
 * 
 */
package org.servalproject.rr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author rbochet
 * 
 */
public class StuffDownloader {

	/** The repository where we should get the manifest */
	private String repository;

	/** TAG for debugging */
	public static final String TAG = "R2";

	/**
	 * Constructor of the class.
	 * 
	 * @param repository
	 *            The root of server where the manifests are stored.
	 */
	public StuffDownloader(String repository) {
		this.repository = repository;

		Log.v(TAG, "Start downloading from " + this.repository);

		List<String> manifests = fetchManifests();

		List<String> dlManifests = chooseManifests(manifests);

		for (String manifest : dlManifests) {
			dlFile(manifest);
		}

	}

	/**
	 * Download the manifest, grab the file name and download it. If the hash of
	 * the file downloaded is different from the hash of the manifest, discard
	 * both the file and the manifest.
	 * 
	 * @param manifest
	 *            The manifest address
	 */
	private void dlFile(String manifest) {
		try {
			// Download the manifest in the Rhizome directory
			Log.v(TAG, "Downloading " + manifest);
			String[] tokenizedUrl = manifest.split("/");
			String mfName = tokenizedUrl[tokenizedUrl.length - 1];
			downloadFile(new URL(manifest), RhizomeUtils.dirRhizomeTemp + "/" + mfName);

			// Check the key TODO
			Log.v(TAG, "Loading properties from " + mfName);
			Properties pManifest = new Properties();
			pManifest.load(new FileInputStream(RhizomeUtils.dirRhizomeTemp + "/"
					+ mfName));

			// If alright, compute the actual file URL and name
			tokenizedUrl[tokenizedUrl.length - 1] = pManifest
					.getProperty("name");
			StringBuilder fileNameB = new StringBuilder(tokenizedUrl[0]);
			for (int i = 1; i < tokenizedUrl.length; i++) {
				fileNameB.append("/" + tokenizedUrl[i]);
			}
			String file = fileNameB.toString();

			// Download it
			Log.v(TAG, "Downloading " + file);
			downloadFile(new URL(file),
					RhizomeUtils.dirRhizomeTemp + "/" + pManifest.getProperty("name"));

			// Check the hash
			String hash = RhizomeUtils.ToHexString(RhizomeUtils
					.DigestFile(new File(RhizomeUtils.dirRhizomeTemp + "/"
							+ pManifest.getProperty("name"))));

			if (!hash.equals(pManifest.get("hash"))) {
				// Hell, the hash's wrong! Delete the logical file
				Log.w(TAG, "Wrong hash detected for manifest " + manifest);
			} else { // If it's all right, copy it to the real repo
				RhizomeUtils.CopyFileToDir(new File(RhizomeUtils.dirRhizomeTemp,
						pManifest.getProperty("name")), RhizomeUtils.dirRhizome);

				RhizomeUtils.CopyFileToDir(new File(RhizomeUtils.dirRhizomeTemp + "/"
						+ mfName), RhizomeUtils.dirRhizome);

				// Generate the meta file for the newly received file
				RhizomeFile.GenerateMetaForFilename(pManifest
						.getProperty("name"));

				// Notify the main view that a file has been updated
				Handler handler = Main.getHandlerInstance();
				Message updateMessage = handler.obtainMessage(Main.MSG_UPD,
						pManifest.getProperty("name"));
				handler.sendMessage(updateMessage);
			}
			// Delete the files in the temp dir
			new RhizomeFile(RhizomeUtils.dirRhizomeTemp, pManifest.getProperty("name"))
					.delete();

		} catch (MalformedURLException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Choose the interesting manifests among a list of the manifest that can be
	 * downloaded from an host. If a manifest doesn't exist in the local FS,
	 * we'll download it. If a manifest already exists, we'll see if it's a new
	 * version.
	 * 
	 * @param manifests
	 *            The list of all the manifests URL
	 * @return A list of the selected manifests
	 */
	private List<String> chooseManifests(List<String> manifests) {
		List<String> ret = new ArrayList<String>();

		// Iterate
		for (String manifest : manifests) {
			// "Unwrapp" the name
			String mfName = manifest.split("/")[manifest.split("/").length - 1];
			// Check if it exists on the local repo
			if (!(new File(RhizomeUtils.dirRhizome, mfName).exists())) {
				// We add it to the DL list
				ret.add(manifest);
			} else { // The manifest already exists ; but is it a new version ?
				try {
					// DL the new manifest in a temp directory
					Log.v(TAG, "Downloading " + manifest);
					downloadFile(new URL(manifest), RhizomeUtils.dirRhizomeTemp + "/"
							+ mfName);

					// Compare the two manifests ; if new.version > old.version,
					// DL
					Properties newManifest = new Properties();
					newManifest.load(new FileInputStream(RhizomeUtils.dirRhizomeTemp
							+ "/" + mfName));
					float nmversion = Float.parseFloat((String) newManifest
							.get("version"));

					Properties oldManifest = new Properties();
					oldManifest.load(new FileInputStream(RhizomeUtils.dirRhizome + "/"
							+ mfName));
					float omversion = Float.parseFloat((String) oldManifest
							.get("version"));

					if (nmversion > omversion) {
						ret.add(manifest);
					}
				} catch (IOException e) {
					Log.e(TAG, "Error evaluating if the manifest " + manifest
							+ " version.");
					e.printStackTrace();
				}

			}
		}
		return ret;
	}

	/**
	 * Fetch the list of the manifests on the server.
	 * 
	 * @return The list of manifests.
	 */
	private List<String> fetchManifests() {
		List<String> manifests = new ArrayList<String>();
		manifests
				.add("http://dl.dropbox.com/u/3505759/serval/.database.xml.manifest");
		manifests
				.add("http://dl.dropbox.com/u/3505759/serval/.disable-lagfix.truite.manifest");

		// Return !
		return manifests;
	}

	/**
	 * Download a file using HTTP.
	 * 
	 * @param file
	 *            The URL of the file
	 * @param path
	 *            The path were the file will be saved on the local FS.
	 * @throws IOException
	 *             If something goes wrong
	 */
	private void downloadFile(URL url, String path) throws IOException {
		URLConnection uc = url.openConnection();
		int contentLength = uc.getContentLength();
		InputStream raw = uc.getInputStream();
		InputStream in = new BufferedInputStream(raw);
		byte[] data = new byte[contentLength];
		int bytesRead = 0;
		int offset = 0;
		while (offset < contentLength) {
			bytesRead = in.read(data, offset, data.length - offset);
			if (bytesRead == -1)
				break;
			offset += bytesRead;
		}
		in.close();

		if (offset != contentLength) {
			throw new IOException("Only read " + offset + " bytes; Expected "
					+ contentLength + " bytes");
		}
		// Save it !
		FileOutputStream out = new FileOutputStream(path);
		out.write(data);
		out.flush();
		out.close();

	}

}
