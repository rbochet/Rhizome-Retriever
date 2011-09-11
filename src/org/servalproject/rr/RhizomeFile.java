/**
 * 
 */
package org.servalproject.rr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;

import android.util.Log;

/**
 * @author rbochet A Rhizome logical file is composed by three files : - The
 *         actual file (ie <file>) - The .<file>.manifest - The .<file>.meta
 */
public class RhizomeFile {

	/** TAG for debugging */
	public static final String TAG = "R2";

	/**
	 * Copy a file.
	 * 
	 * @param sourceFile
	 *            The source
	 * @param destDir
	 *            The destination directory
	 * @throws IOException
	 *             Everything fails sometimes
	 */
	protected static void CopyFileToDir(File sourceFile, File destDir)
			throws IOException {

		File destFile = new File(destDir, sourceFile.getName());

		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	/** The actual file */
	File file = null;

	/** The associated manifest file */
	File manifest = null;

	/** The associated meta file */
	File meta = null;

	/**
	 * Create the logical file
	 * 
	 * @param path
	 *            The root of the rhizome directory
	 * @param fileName
	 *            the relative path
	 */
	public RhizomeFile(File path, String fileName) {
		// We know that the actual file exists because it has been detected
		file = new File(path, fileName);

		// Create the manifest/meta path if they exists
		setManifest(path, fileName);
		setMeta(path, fileName);
	}

	/**
	 * Delete the logical file -- ie all 3 files if they exists. If this
	 * function is called, the object should be destroyed quickly.
	 * 
	 * @throws IOException
	 *             If the deletion fails
	 */
	public void delete() throws IOException {
		file.delete();
		if (manifest != null)
			manifest.delete();
		if (meta != null)
			meta.delete();
	}

	/**
	 * Export the file in the given dir. The directory is defined in the main
	 * app.
	 * 
	 * @throws IOException
	 *             If the copy fails.
	 */
	public void export() throws IOException {
		CopyFileToDir(file, Main.dirExport);
	}

	/**
	 * @return the file path
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return the manifest path
	 */
	public File getManifest() {
		return manifest;
	}

	/**
	 * @return the meta path
	 */
	public File getMeta() {
		return meta;
	}

	/**
	 * mark the file for expiration
	 */
	public void markForExpiration() {
		Log.e(TAG, "TODO : markForExpiration()");

	}

	/**
	 * If the manifest file exists, sets it up.
	 * 
	 * @param path
	 *            The root of the rhizome directory
	 * @param fileName
	 *            The name of the actual file
	 */
	private void setManifest(File path, String fileName) {
		File tmp = new File(path, "." + fileName + ".manifest");
		if (tmp.exists())
			manifest = tmp;
	}

	/**
	 * If the meta file exists, sets it up.
	 * 
	 * @param path
	 *            The root of the rhizome directory
	 * @param fileName
	 *            The name of the actual file
	 */
	private void setMeta(File path, String fileName) {
		File tmp = new File(path, "." + fileName + ".meta");
		if (tmp.exists())
			meta = tmp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer ts = new StringBuffer();

		ts.append("-- BOF --\n");
		ts.append(" File:        " + getFile() + "\n");
		ts.append(" -> Manifest: " + getManifest() + "\n");
		ts.append(" -> Meta:     " + getMeta() + "\n");
		ts.append("-- EOF --");

		return ts.toString();
	}

	/**
	 * When a file appears (imported or downloaded), this method creates the
	 * associated meta file. The meta file is only for the current handset. The
	 * meta file is created in Rhizome home directory.
	 * 
	 * @param fileName
	 *            The name of the incoming file
	 */
	public static void GenerateMetaForFilename(String fileName) {
		try {
			Properties meta = new Properties();
			// Setup the property object
			meta.put("date", System.currentTimeMillis() + "");
			meta.put("read", false + ""); // the file is just created
			meta.put("marked_expiration", false + ""); // Just imported

			// Save the file
			File tmpMeta = new File(Main.dirRhizome, "." + fileName + ".meta");
			Log.v(TAG, tmpMeta+"");
			meta.store(new FileOutputStream(tmpMeta), "Rhizome meta data for "
					+ fileName);
		} catch (Exception e) {
			Log.e(TAG, "Error when creating meta for " + fileName);
		}
	}

}
