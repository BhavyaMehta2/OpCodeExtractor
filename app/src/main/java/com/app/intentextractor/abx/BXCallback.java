package com.app.intentextractor.abx;


public interface BXCallback {

	/**
	 * Start XML document
	 */
	void startDoc(String xmlFile);
	
	/**
	 * Start of XML document
	 */
	void startNode(Node node);
	
	/**
	 * TODO: read Node value
	 *
	 */
	void nodeValue(int lineNumber, String name, String value);
	
	/**
	 *
	 */
	void endNode(Node node);
	
	void endDoc() throws Exception;
}