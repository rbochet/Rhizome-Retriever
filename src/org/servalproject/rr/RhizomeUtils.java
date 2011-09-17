package org.servalproject.rr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.Environment;
import android.util.Log;

/**
 * Useful function for the whole Rhizome.
 * 
 * @author rbochet
 * 
 */
public class RhizomeUtils {

	/**
	 * Calculates the MD5 digest of the file.
	 * 
	 * @param in
	 * 
	 * @return the digest
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static byte[] DigestFile(File file) {
		byte[] digest = null;
		try {
			FileInputStream in = new FileInputStream(file);
			MessageDigest digester = MessageDigest.getInstance("MD5");
			byte[] bytes = new byte[8192];
			int byteCount;
			while ((byteCount = in.read(bytes)) > 0) {
				digester.update(bytes, 0, byteCount);
			}
			digest = digester.digest();
		} catch (Exception e) {
			Log.e(RhizomeFile.TAG, "Error with hashing " + file.getName());
		}
		return digest;
	}

	/**
	 * Transforms a byte array in a hex string
	 * 
	 * @param digest
	 *            Digest
	 * @return Display ready string
	 */
	public static String ToHexString(byte[] digest) {
		StringBuffer hexStr = new StringBuffer(40);
		for (byte b : digest) {
			hexStr.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
		}
		return hexStr.toString();
	}

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

	/**
	 * Delete a directory and its content.
	 * 
	 * @param path
	 *            The directory to delete.
	 */
	static public void deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			if (files == null) {
				return;
			}
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		path.delete();
	}

	/** Directory where the files are exported */
	public static final File dirExport = new File(
			Environment.getExternalStorageDirectory()
					+ "/serval-rhizome-export");
	/** Rhizome's home directory */
	public static final File dirRhizome = new File(
			Environment.getExternalStorageDirectory() + "/serval-rhizome");
	/** Rhizome's temp directory for manifests download */
	public static final File dirRhizomeTemp = new File(
			Environment.getExternalStorageDirectory() + "/serval-rhizome-temp");

}
