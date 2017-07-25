/*===================================================================
**
**     Program: pDPutil
**
**     Package: org.prairieland.util.pString
**
**     Description:
**
**     Author:      Ray Wilson
**
**     Revisions:   Date       Who  Description
**                  ---------- ---- ----------------------------------------
**                  01/01/2017 prw	Created
**
**-------------------------------------------------------------------------*/

package org.prairieland.util;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import org.w3c.dom.Node.*;
import org.xml.sax.SAXException;

public class pDPutil {

	public static boolean DEBUG = false;

	// =============================== OBJECT LIST ===============================
	// The list of objects we will search for. You can extend this list to include about an named object.
	// in the export.xml files. Make sure you use the spelling of a declaration tag, not the class tag.
	// Get the spelling from a tag that has a "name=" attribute and not the "class=" attribute.
	public static String[] OBJECT_LIST = {"HTTPSourceProtocolHandler", "HTTPSSourceProtocolHandler","MQQM", "B2BGateway", "MultiProtocolGateway", "WebAppFW", "WSGateway", "XMLFirewallService"};

	// =============================== DETAIL ATTRIBUTE LIST ===============================
	// This is a list of attributes that we will try to collect on each obect up above, if it is not
	// found then it will simply be ignored. That allow for the entry of tags that may or may not exist in
	// all of the objecs you are looking for. See KNOWN ISSUES above!
	public static String[] DETAIL_LIST = {"mAdminState", "LocalAddress", "LocalPort", "HostName", "QMname", "ChannelName", "SSLProxy", "XMLManager", "FrontProtocol", "StylePolicy", "BackendUrl", "Type"};

	public static ArrayList<String> fullList = new ArrayList<>();
	public static String[] getAllObjectsList(File expFile) {
		// Make sure the list is empty before we start.
		fullList.clear();
		String[] finalList = null;
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document document = docBuilder.parse(expFile);
			recurseElementList(document.getDocumentElement());
			if(DEBUG) { System.out.println("\n\nDEBUG: FOUND -"+fullList.size()+"- TOTAL ELEMENTS\n\n"); }
			// add elements of inputlist to a set, this will deduplicate the list.
			Set<String> theSet = new HashSet<>();
			theSet.addAll(fullList);
			// Convert to a string for use in ConfigInventory for get all elements.
			finalList = theSet.toArray(new String[0]);
			if(DEBUG) { System.out.println("\n\nDEBUG: FOUND -"+finalList.length+"- UNIQUE ELEMENTS\n\n"); } 
		} catch (SAXException | IOException | ParserConfigurationException ex) {
			System.out.println(":: ERROR ::");
			ex.printStackTrace();
		}
		return finalList;
	}

	public static void recurseElementList(Node node) {
		NamedNodeMap nnm = node.getAttributes();
		Node tNode = nnm.getNamedItem("name");
		if (tNode != null) {
			String nn = node.getNodeName();
			if(DEBUG) { System.out.println(nn); }
			fullList.add(nn);
		}
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				recurseElementList(currentNode);
			}
		}
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

