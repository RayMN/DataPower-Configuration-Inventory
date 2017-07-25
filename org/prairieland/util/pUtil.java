/*===================================================================
**
**     Program: pUtil
**
**     Package: org.prairieland.util.pString
**
**     Description:
**
**     Author:      Ray Wilson
**
**     Revisions:   Date       Who  Description
**                  ---------- ---- ----------------------------------------
**                  07/17/2017 prw	Created
**
**-------------------------------------------------------------------------*/

package org.prairieland.util;

import java.io.*;
import java.util.stream.Collectors;
import java.util.Enumeration;
import java.nio.file.*;
import java.util.zip.*;
import java.nio.file.attribute.BasicFileAttributes;

public class pUtil {
	public static boolean DEBUG = false;


	/** upzipit(String, String)
	* This is the method that extracts the zip files that make up the DataPower export.
	* It is called on the main file then again for each domain export zip file.
	* param - theZip - a string containing a full path to a zip file.
	* param - theFolder - a string containinga full path to a extract directory ending with a "/" .
	*/
	public static void unZipIt(String theZip, String theFolder) {
		try {
			// Creat the zipFile handle
			ZipFile zipFile = new ZipFile(theZip);
			// Create an enumeration of all the items in the zip file
			Enumeration<?> enu = zipFile.entries();

			if(DEBUG) { System.out.printf("DEBUG :: Extracting: "+theZip); }
			// Extract all of the individual items
			while (enu.hasMoreElements()) {
				// Create a ZipEntry (the format of a zip file entry) from the enumeration
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();
				// Get the name of the item (we will use is a few times)
				String name = zipEntry.getName();
				if(DEBUG) {	System.out.printf("Name: %-30s | Size: %8d | Compressed: %8d\n", name, zipEntry.getSize(), zipEntry.getCompressedSize()); }

				// Create a folder if this time is a folder
				File file = new File(theFolder+name);
				if (name.endsWith("/")) { file.mkdirs(); continue; }

				// Make sure all of the parent directories are in place.
				File parent = file.getParentFile();
				if (parent != null) { parent.mkdirs(); }

				// Do the byte level extract of the compressed file.
				InputStream is = zipFile.getInputStream(zipEntry);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = is.read(bytes)) >= 0) { fos.write(bytes, 0, length); }
				// Close the streams used by the byte level extraction.
				is.close();
				fos.close();
			}
			// Close the zip file.
			zipFile.close();
		} catch (IOException ex) {
			System.out.println(":: ERROR ::");
			ex.printStackTrace();
		}
	}

	/** removeTmpDir(String)
	* This is method that cleans up and removes all of the extracted files.
	* param - tmpDir - a string containing the path to the tmpDir.
	*/
	public static void killDir(String tmp){
		if(DEBUG){ System.out.println("DEBUG :: About to remove "+tmp); }
		//Remove the temporary directory
		try {
			Path directory = Paths.get(tmp);
   			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
	   			@Override
	   			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException { Files.delete(file); return FileVisitResult.CONTINUE; }
	   			@Override
	   			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException { Files.delete(dir); return FileVisitResult.CONTINUE; }
   			});
		} catch (IOException ex) {
				System.out.println(":: ERROR ::");
				ex.printStackTrace();
		}
		if(DEBUG) { System.out.println("DEBUG :: "+ tmp +" has been removed."); }
	}

	/** placeHolder(String)
	* This is method that returns the value String.
	* param - String - someStr - a string containing xml.
	* return - String - str - the value of someStr+someStr.
	*/
	/*
	public static String placeHolder(String someStr){
		String str = "oink";
		str = someStr+someStr;
		return str;
	}
	/* ===== END OF placeHolder(String) =====*/

}
/*---------------------------------------------------------------------------
**     END OF MODULE
**-------------------------------------------------------------------------*/

