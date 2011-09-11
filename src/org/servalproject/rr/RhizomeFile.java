/**
 * 
 */
package org.servalproject.rr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.util.Log;

/**
 * @author rbochet A Rhizome logical file is composed by three files : - The
 *         actual file (ie <file>) - The .<file>.manifest - The .<file>.meta
 */
public class RhizomeFile {

	/** TAG for debugging */
	public static final String TAG = "R2";

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
		ts.append("File:        " + getFile() + "\n");
		ts.append("-> Manifest: " + getManifest() + "\n");
		ts.append("-> Meta:     " + getMeta() + "\n");
		ts.append("-- EOF --");

		return ts.toString();
	}

	/**
	 * Delete the file
	 */
	public void delete() {
		Log.e(TAG, "TODO : delete()");
	}

	/**
	 * Export the file in the given dir.
	 * The directory is defined in the main app.
	 * 
	 * @throws IOException
	 *             If the copy fails.
	 */
	public void export() throws IOException {
		Log.e(TAG, "TODO: export()");
		CopyFileToDir(file, Main.dirExport);
	}

	/**
	 * mark the file for expiration
	 */
	public void markForExpiration() {
		Log.e(TAG, "TODO : markForExpiration()");

	}

	/**
	 * Copy a file.
	 * 
	 * @param sourceFile
	 *            The source
	 * @param destFile
	 *            The dest
	 * @throws IOException
	 *             Everything fails sometimes
	 */
	private static void CopyFileToDir(File sourceFile, File destDir)
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

}
