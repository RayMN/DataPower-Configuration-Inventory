/*===================================================================
**
**     Program: pXml
**
**     Package: org.prairieland.util.pXml
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
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.SAXException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class pXML {

	public static boolean DEBUG = false;

	/** getTagValue(String, String)
	* This is method that gets the value of a given tag in an xml string.
	* Right now it only gets the first occurance so does not work on repeating
	* elements like front side handlers. See KNOW ISSUES above!
	* param - xml - a string containing xml.
	* param - tagName - a string containg the tag name with out the "<" or ">".
	* return - string - the value inside the tags.
	*/
	public static String getTagValue(String xml, String tagName){
		String str = "NA";
		try {
			// Try to get the value of a normal/plain tag
			str = xml.split("<"+tagName+">")[1].split("</"+tagName+">")[0];
		} catch (ArrayIndexOutOfBoundsException ex1) {
			try {
				// If that fails try to get the value of a tag that has attributes
				str = xml.split("<"+tagName+" ")[1].split("</"+tagName+">")[0];
				str = str.split(">")[1].split("</"+tagName+">")[0];
			} catch (ArrayIndexOutOfBoundsException ex2) {
				// if that fails assume that it does not exit in the element.
				str = "NA";
			}
		}
		return str;
	}
	/* ===== END OF getTagValue(String, String) =====*/

    /** getAllNodes(String, String)
     * This is method that gets the object xml string by name.
     * Then returns it to the main program as a seperate xml string.
     * param - xml - a string containing xml.
     * param - tag - a string containg the tag name with out the "<" or ">".
     * return - theList - an XML NodeList of node elements of <tag> type.
     */
	public static NodeList getAllNodes(String xml, String tag){
		NodeList theList = null;
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
			doc.getDocumentElement().normalize();
			theList = doc.getElementsByTagName(tag);
		} catch (IOException | ParserConfigurationException | SAXException ex) {
			System.out.println(":: ERROR ::\n");
			ex.printStackTrace();
		}
		return theList;
	}
	/* ===== END OF getAllNodes(String, String) =====*/

    /** getElementAtrribute(Node, String)
     * This is method that gets the object xml string by name.
     * Then returns it to the main program as a seperate xml string.
     * param - node - an XML node containing xml.
     * param - attr - a string containg the attribute name.
     * return - string - the attribute value.
     */
	public static String getElementAtrribute(Node xml, String attr){
		Element el = (org.w3c.dom.Element) xml;
		String theAttrValue = el.getAttribute(attr);
		return theAttrValue;
	}
	/* ===== END OF getElementAtrribute(Node, String) =====*/

    /** xmlNodeToString(Node)
     * This is method that gets the object xml string by name.
     * Then returns it to the main program as a seperate xml string.
     * param - node - an XML node.
     * return - string - the XML node as a string.
     */
	public static String xmlNodeToString(Node node) {
		StringWriter buf = new StringWriter();
		try {
			Transformer xform = TransformerFactory.newInstance().newTransformer();
			xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			xform.transform(new DOMSource(node), new StreamResult(buf));
		} catch (TransformerException ex) {
			System.out.println(":: ERROR ::\n");
			ex.printStackTrace();
		}
		return(buf.toString());
	}
	/* ===== END OF xmlNodeToString(Node) =====*/

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

