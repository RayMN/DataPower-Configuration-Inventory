/**
*   Module: ConfigInventory.java
*
*   Written by Ray Wilson
*
*   Description: Parse DataPower export files and put the data into a csv file.
*                The output file will be stored written in the base directory of the export.
*				 This file can then be imported into your favorite spreadsheet.
*
*   History:
*	2017-07-06 v0.0.1 Created.
*	2017-07-07 v0.1.0 Basic functionality achieved.
*	2017-07-08 v1.0.0 Added auto extract and removal of extracted folders.
*              v2.0.0 Added Configuration Details option to the data collected on each gateway
*                     Added the ability to turn on debug from the command line
*   2017-07-25 v2.5.0 Moving it to the new package structure for PrairieLand.
*                     Added a -all switch to get all named objects
*                     Rewrote the core to use the DOM model and XML tools for java better.
*
*   KNOWN ISSUES:
*              Not the most efficient file handling but it works and it's not a "performance"
*              software so for now I'm willing to live with it.
*
*   Author: Ray Wilson
*           wilsonprw@gmail.com
*
*   Date:   2017-07-06
*
*   Copyright (C) 2017  Paul Ray Wilson
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.prairieland.datapower.ConfigInventory;
import org.prairieland.util.pXML;
import org.prairieland.util.pDPutil;
import org.prairieland.util.pUtil;

import java.io.*;
import java.util.Scanner;
import java.nio.file.*;
import org.w3c.dom.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;
import java.util.Enumeration;
import java.util.zip.*;

public class ConfigInventory {
	public static String zipFile = "";
	public static String tmpDir = "";
	public static String csvFile = "";
	public static boolean DEBUG = false;
	public static boolean DETAILS = false;
	public static boolean GETALL = false;
	public static String EXP = "export.xml"; // Export file name to work with ... all of them should be the same.

	/** main()
	*
	* This is the "main" method that checks for command line arguments and
	* for starting the createInventory method below where all the actual work will happen
	*
	*/
	public static void main(String arg[]) throws Exception {

		// Validate the command line arguments.
		boolean argsValid = checkArgs(arg); // Check command line arugments

		if (!argsValid) {
    	  	System.out.println("\n===============================================================================");
    	  	System.out.println(" ");
    	  	System.out.println("Usage: java -jar ConfigInventory.jar zipFile tmpDir csvFile [-h (-details | -all) -debug]");
    	  	System.out.println("    Where:");
    	  	System.out.println("          zipFile  = Absolute path to the DataPower complete appliance export zip file");
    	  	System.out.println("          tmpDir   = Absolute path to temporary directory to extract zip files including a tailing /");
    	  	System.out.println("          csvFile  = Absolute path and name of the output csv file");
    	  	System.out.println("          -h       = Optional: This message");
			System.out.println("          -details = Optional: When present adds the object details to the output - ! used with -all");
    	  	System.out.println("          -all     = Optional: When present generates a list of all named objects - ! used with -details");
    	  	System.out.println("          -debug   = Optional: When present generates VERY verbose DEBUG messages in the console");
    	  	System.out.println(" ");
    	  	System.out.println("Example > java -jar /Classes/ConfigInventory.jar /dir/IDG-20-A.zip /dir/dp-export/ /dir/IDG-20-report.csv");
    	  	System.out.println(" ");
    	  	System.out.println("===============================================================================\n");
			System.exit(0);
		} else {
			try {
				zipFile = arg[0];
				tmpDir = arg[1];
				csvFile = arg[2];

				// This is where the work happens ....
				String result = createInventory();
				System.out.println(result);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(0);
			}
		}
	}
	/* ===== END OF public static void main(String arg[]) =====*/


	/** createInventory()
	*
	* This is the module where the following happens:
	*   1) The zip file from the DataPower Export is extracted
	*   2) Basic appliance information collected from export.xml in the root of the extract
	*	3) A list of domains is read from the export.xml file in the root of the extract
	*   4) For each domain in the list
	*   5)    The zip file for that domain is extracted
	*   6)    The export.xml file in that domain is examined and information on gateways collected.
	*	7) All the upziped files and folders are removed. (still pending)
	*
	* return - String - The results of the doWork opperation.
	*/
	public static String createInventory () {
		if(DEBUG) {System.out.println("DEBUG :: Entering createInventory");}

		// Create helper classes
		pXML px = new pXML();
		pDPutil dp = new pDPutil();
		pUtil pu = new pUtil();

		File fout = new File(csvFile);
		try {
			// Create the output file if it does not exist.
			if(!fout.exists()){
				fout.createNewFile();
				if(DEBUG){System.out.println("DEBUG :: Creating output file - " + csvFile);}
			}
			// Output file handle for writing
			BufferedWriter out = new BufferedWriter(new FileWriter(csvFile,true));

			// Extract the main export file. It will contain multiple zip files with it for each domain
			// on the appliance. We will unzip those later in the loop where we gather the gateway informaiton
			pu.unZipIt(zipFile, tmpDir);

			// ******
			// Get the data we want from the root export file about the appliance itself.
			// ******
			// Open the export file in the root folder to get some some of the data
			String content = new Scanner(new File(tmpDir+EXP)).useDelimiter("\\Z").next();

			// Get the node that contains the export details from the root export.xml file
			NodeList epxDetails = px.getAllNodes(content,"export-details");

			// Get the Date-Time of the export and write it to the file.
			String expDate = px.getAllNodes(content,"current-date").item(0).getTextContent();
			String expTime = px.getAllNodes(content,"current-time").item(0).getTextContent();
			out.write("Export Date, "+ expDate + " - " + expTime + "\n"); out.flush();
			if(DEBUG){ System.out.println("DEBUG :: Export Date :"+expDate+" "+expTime); }

			// Get the Device Name and write it to the file.
			String deviceName = px.getAllNodes(content,"device-name").item(0).getTextContent();
			out.write("Device Name,"+deviceName+"\n"); out.flush();
			if(DEBUG){ System.out.println("DEBUG :: Device Name :"+deviceName ); }

			// Get the Product ID and write it to the file.
			String productId = px.getAllNodes(content, "product-id").item(0).getTextContent();
			out.write("Product ID,"+productId+"\n"); out.flush();
			if(DEBUG){ System.out.println("DEBUG :: Product ID  :"+productId ); }

			// Get the serial number and write it to the file.
			String serialNum = px.getAllNodes(content,"serial-number").item(0).getTextContent();
			out.write("Serial Number,"+serialNum+"\n"); out.flush();
			if(DEBUG){ System.out.println("DEBUG :: Serial No   :"+serialNum ); }

			// Get the firmware version level and write it to the file.
			String firmwareVer = px.getAllNodes(content,"firmware-version").item(0).getTextContent();
			out.write("Firmware Version,"+firmwareVer+"\n"); out.flush();
			if(DEBUG){ System.out.println("DEBUG :: Firmware    :"+firmwareVer ); }

			// Get a list of domains on the appliance from the base export.xml file.
			NodeList domainList = px.getAllNodes(content,"domain");

			// Write the header for the list of gateways based on the DETAILS switch.
			if(DETAILS){
				out.write("\n,\nDomain, Object Type, Object Name, Object Attribute, Attribute Value"); out.flush();
			} else {
				out.write("\n,\nDomain, Object Type, Object Name"); out.flush();
			}

			// ******
			// Now get the object information from each domain.
			// ******
			for(int k = 1; k < domainList.getLength(); k++) {

				String thisDomainName = px.getElementAtrribute(domainList.item(k), "name");
				if(DEBUG){ System.out.println("\n\nDEBUG :: ++++++++++\nDEBUG :: ++++++++++ Working in Domain :" + thisDomainName + " ++++++++++\nDEBUG :: ++++++++++"); }

				// Extract the zip file for this domain
				String thisZipFile = tmpDir+thisDomainName+".zip";
				String thisTmpDir = tmpDir+thisDomainName+"/";
				pu.unZipIt(thisZipFile, thisTmpDir);

				// Read in the export.xml file for this domain.
				String domainExportFile = thisTmpDir+"/"+EXP;
				File expFile = new File(domainExportFile);

				// Create a workList of objects to return from each domain.
				String[] workList = null;
				if(GETALL){
					if(DEBUG) {System.out.println("DEBUG :: Using getAllObjectsList to generate the workList.");}
					workList = dp.getAllObjectsList(expFile);
				} else {
					if(DEBUG) {System.out.println("DEBUG :: Using OBJECT_LIST for workList.");}
					workList = dp.OBJECT_LIST;
				}

				// Create on big string from the export.xml file for this domain.
				String domainContent = new Scanner(expFile).useDelimiter("\\Z").next();
				
				// Write out the domain name to the file with a blank line before it.
				out.write(", \n"+thisDomainName+"\n"); out.flush();

				// Generate an object list for each object in the dp.OBJECT_LIST ... found in pDPutil.java
				for(int l=0; l< workList.length; l++){
					if(DEBUG){ System.out.println("\nDEBUG :: +++++ Searching for " + workList[l] + " objects in " + thisDomainName); }

					// The list of object for this OBJECT_LISTtype.
					NodeList thisList = px.getAllNodes(domainContent, workList[l]);
					if(DEBUG){ System.out.println("DEBUG :: Found " + thisList.getLength() + " object(s) of type : " + workList[l] ); }

					// Check to see if there are any gateways in the list.
					if(thisList.getLength() > 0){
						for(int m = 0; m < thisList.getLength(); m++){
							// Only process elements that have name attribute (some are only for when the element is used ... bug in IBM export.xml format.)
							if (px.getElementAtrribute(thisList.item(m), "name").length() > 1) {
								if(DEBUG){ System.out.println("DEBUG :: === " + (m+1) + " === Working element -" + px.getElementAtrribute(thisList.item(m), "name")+"-"); }
								// Write the type and name of each gateway in the list out to the file.
								out.write("," + workList[l] + "," + px.getElementAtrribute(thisList.item(m), "name") + "\n"); out.flush();								
								if(DETAILS){
									// Get the element for the object, including all child elements.
									if(DEBUG){ System.out.println("DEBUG :: Requesting Element: " + workList[l] + " with the name " + px.getElementAtrribute(thisList.item(m), "name")); }

									// From the Node of this OBJECT get all of the applicable Attributes of the object from dp.DETAIL_LIST ... found in pDPutil.java
									for(int n = 0; n < dp.DETAIL_LIST.length; n++){
										NodeList aList = px.getAllNodes(px.xmlNodeToString(thisList.item(m)), dp.DETAIL_LIST[n]);
										for (int p = 0; p < aList.getLength(); p++) {
											if(DEBUG){ System.out.println("DEBUG ::  Found " + dp.DETAIL_LIST[n] + " - " + aList.item(p).getTextContent()); }

											// If the object admin state is disabled make a marker under the name to let someone know.
											if(dp.DETAIL_LIST[n].equals("mAdminState") && aList.item(p).getTextContent().equals("disabled")) {
												out.write(",X^_DISABLED_^X, ," + dp.DETAIL_LIST[n] + "," + aList.item(p).getTextContent() + "\n"); out.flush();
											} else {
												out.write(", , ," + dp.DETAIL_LIST[n] + "," + aList.item(p).getTextContent() + "\n"); out.flush();
											}
										}
									}
								}
                            }
                        }
					}
				}
			}
			// House keeping, Flush the buffer for the csvFile and close it.
			out.flush();
			out.close();
			// Remove the extracted files
			pu.killDir(tmpDir);
		} catch (Exception ex) {
			System.out.println(":: ERROR ::");
			ex.printStackTrace();
		}
		return("INVENTORY COMPLETE: The file "+csvFile+" has been created");
	}
	/* ===== END OF createInventory() =====*/


	/** checkArgs(String[])
	* This is method validates the command line arguments as best as we can.
	* Needless to say, it's barely idiot proof .. but there is no what it's stupid proof. :)
	* param - arg[]
	* return - boolean - true of no errors detected else false.
	*/
	public static boolean checkArgs(String[] arg){
		boolean valid = true;

		if(arg.length < 3) { return false; }

		// Check for the zip file
		File zFile = new File(arg[0]);
		if (!zFile.exists()) { valid = false; System.out.println("ERROR :: zipFile -" + arg[0] + "- is not valid"); }

		// Check for a temporary directory name. It does not have to exist, so just check that it's on the command line.
		if(!(arg[1].length() > 0)) { valid = false; System.out.println("ERROR :: tmpDir -" + arg[1] + "- is not valid"); }

		// Check for a csvFile name.
		if(!(arg[2].length() > 0)) { valid = false; System.out.println("ERROR :: csvFile -" + arg[2] + "- is not valid"); }

		// OPTIONAL SWITCH PARAMETERS
		if(arg.length > 3){
			for(int j = 3; j < arg.length; j++) {
				switch (arg[j]) {
					case "-h":				// Check for help switch
						valid=false;
						break;
					case "-details":
						DETAILS=true;		// Set the DETAILS switch
						break;
					case "-all":
						GETALL=true;		// Set the GETALL switch
						DETAILS=false;		// unSet the DETAILS switch - really fubars the output if you use both.
						break;
					case "-debug":
						DEBUG=true;			// Set the DEBUG switch
						break;
					default:
						System.out.println("ERROR :: switch -" + arg[j] + "- is not valid");
						valid=false;
						break;
				}
			}
		}
		if(DEBUG) {
			System.out.println();
			for(int i=0;i<arg.length;i++) { System.out.println("DEBUG :: arg " + i + " -" + arg[i] + "-"); }
			System.out.println("DEBUG :: DETAILS = " + DETAILS);
			System.out.println("DEBUG :: GETALL  = " + GETALL);
			System.out.println("DEBUG :: DEBUG   = " + DEBUG);
		}
		return valid;
	}
	/* ===== END OF checkArgs(String[]) =====*/

}
/* ===== END OF Class ConfigInventory =====*/

