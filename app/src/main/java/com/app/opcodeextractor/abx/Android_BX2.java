
package com.app.opcodeextractor.abx;

import android.util.Log;

import com.app.opcodeextractor.utils.Utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Android_BX2 implements Resource {

	byte[] chunk_type_buf = new byte[2];
	byte[] header_size_buf = new byte[2];
	byte[] chunk_size_buf = new byte[4];
	int header_size;
	int chunk_size;
	int package_count;
	
	byte[] buf_2 = new byte[2];
	byte[] buf_4 = new byte[4];
	
	String tag = "Android_BX2";
	
	/**
	 * Binary XML String pool
	 */
	ArrayList<String> stringPool = new ArrayList<>();

	/**
	 * Resource Map
	 */
	ArrayList<Integer> resMap = new ArrayList<>();
	
	int ns_prefix_index = -1;
	int ns_uri_index = -1;
	int ns_linenumber = 0;
	
	/**
	 * Order of XML node. Used by XML generator to track root node should include XML Namespace definition
	 * and children should not.
	 */
	int nodeIndex = -1;
	
	BXCallback listener;
	
	public Android_BX2(BXCallback listner){
		this.listener = listner;
	}
	
	
	public void parse(byte[] binaryXml) throws Exception
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(binaryXml);
		BufferedInputStream in = new BufferedInputStream(bais);

		if(listener != null)
			listener.startDoc(null);
		
		parse(in);
	}
	
	/**
	 * Parse XML resourcs...
	 * 
	 * [String Pool]
	 * [Resource Map]
	 * [Namespace Start]
	 * [XML Start]
	 * [XML End]
	 * [XML Start]
	 * [XML End]
	 * .....
	 * [Namespace End]
	 *  * [Namespace Start]
	 * [XML Start]
	 * [XML End]
	 * [XML Start]
	 * [XML End]
	 * .....
	 * [Namespace End]
	 * ....
	 * # There can be multiple Namespace and within one Name space multiple XML nodes.
	 *
	 */
	public void parse(String bxFile) throws Exception 
	{
		BufferedInputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(bxFile)));

		if(listener != null)
			listener.startDoc(bxFile);
		
		parse(in);
	}
	
	private void parse(BufferedInputStream in) throws Exception 
	{
		int header_size;
		int chunk_size;
		
		// Is it an valid BXML ?
		/*
		 * Chunk header meta size - 8 bytes
		 * [Chunk Type] - 2 bytes
		 * [Chunk Header Size] - 2 bytes
		 * [Chunk Size] - 4 bytes
		 */
		in.read(chunk_type_buf);
		
		if(Utils.toInt(chunk_type_buf, false) != RES_XML_TYPE){
			Log.d(tag, "It's an invalid BXML file. Exiting!");
			return;
		}
		
		
		in.read(header_size_buf);
		header_size = Utils.toInt(header_size_buf, false);
		
		in.read(chunk_size_buf);
		chunk_size = Utils.toInt(chunk_size_buf, false);
		
		Log.d(tag, "Header Size: "+ header_size +" Chunk size: "+ chunk_size);
		
		in.read(chunk_type_buf);
		
		if(Utils.toInt(chunk_type_buf, false) == RES_STRING_POOL_TYPE)
		{
			// String Pool/Tokens
			Log.d(tag, "String Pool...");
			in.read(header_size_buf);
			header_size = Utils.toInt(header_size_buf, false);
			
			in.read(chunk_size_buf);
			chunk_size = Utils.toInt(chunk_size_buf, false);
			
			Log.d(tag, "String Pool...Header Size: "+ header_size +" Chunk Size: "+ chunk_size);
			
			byte[] spBuf = new byte[chunk_size - 8];
			in.read(spBuf);
			
			// Parse String pool
			parseStringPool(spBuf, header_size, chunk_size);
			
			// Get the next Chunk
			in.read(chunk_type_buf);
		}
		
		// Resource Mapping- Optional Content
		if(Utils.toInt(chunk_type_buf, false) == RES_XML_RESOURCE_MAP_TYPE)
		{
			in.read(header_size_buf);
			header_size = Utils.toInt(header_size_buf, false);
			
			in.read(chunk_size_buf);
			chunk_size = Utils.toInt(chunk_size_buf, false);
			
			byte[] rmBuf = new byte[chunk_size - 8]; 
			in.read(rmBuf);
			
			// Parse Resource Mapping
			parseResMapping(rmBuf, header_size, chunk_size);
			
			// Get the next Chunk
			in.read(chunk_type_buf);
		}
		
		/*
		 * There can be multiple Name space and XML node sections
		 * [XML_NameSpace_Start]
		 * 	[XML_Start]
		 *  	[XML_Start]
		 * 		[XML_End]
		 *  [XML_END]
		 * [XML_NameSpace_End]
		 * [XML_NameSpace_Start]
		 * 	[XML_Start]
		 * 	[XML_End]
		 * [XML_NameSpace_End]
		 */
		
		// Name space Start
		if(Utils.toInt(chunk_type_buf, false) == RES_XML_START_NAMESPACE_TYPE)
		{
			in.read(header_size_buf);
			header_size = Utils.toInt(header_size_buf, false);
			
			in.read(chunk_size_buf);
			chunk_size = Utils.toInt(chunk_size_buf, false);
			
			byte[] nsStartBuf = new byte[chunk_size - 8]; 
			in.read(nsStartBuf);
			
			// Parse Start of Name space
			parseStartNameSpace(nsStartBuf, header_size, chunk_size);
		}
		
		// Handle multiple XML Elements
		in.read(chunk_type_buf);
		int chunk_type = Utils.toInt(chunk_type_buf, false);
		
		while(chunk_type !=  RES_XML_END_NAMESPACE_TYPE)
		{
			Log.d(tag, "Parsing XML node...Chunk_Type "+ chunk_type);
			/*
			 * XML_Start
			 * 	XML_Start
			 *  XML_End
			 * XML_End
			 * .......
			 */
			in.read(header_size_buf);
			header_size = Utils.toInt(header_size_buf, false);
			
			in.read(chunk_size_buf);
			chunk_size = Utils.toInt(chunk_size_buf, false);
			
			byte[] elementBuf = new byte[chunk_size - 8];
			in.read(elementBuf);
			
			if(chunk_type == RES_XML_START_ELEMENT_TYPE)
			{
				// Start of XML Node
				parseXMLStart(elementBuf, header_size, chunk_size);
			}
			else if(chunk_type == RES_XML_END_ELEMENT_TYPE)
			{
				// End of XML Node
				parseXMLEnd(elementBuf, header_size, chunk_size);
			}
			
			// TODO: CDATA
			
			// Next Chunk type
			in.read(chunk_type_buf);
			chunk_type = Utils.toInt(chunk_type_buf, false);
		}
		
		// End of Name space
		if(chunk_type == RES_XML_END_NAMESPACE_TYPE)
		{
			in.read(header_size_buf);
			header_size = Utils.toInt(header_size_buf, false);
			
			in.read(chunk_size_buf);
			chunk_size = Utils.toInt(chunk_size_buf, false);
			
			byte[] nsEndBuf = new byte[chunk_size - 8]; 
			in.read(nsEndBuf);
			
			// Parse End of Name space
			parseEndNameSpace(nsEndBuf, header_size, chunk_size);
		}
		
		if(listener != null)
			listener.endDoc();
		
		// That's it. TODO: Handle multiple Name spaces
	}

	private void parseStringPool(byte[] spBuf, int header_size, int chunk_size) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(spBuf);
		
		// String pool header
		byte[] int_buf = new byte[4];
		in.read(int_buf);
		
		int string_count = Utils.toInt(int_buf, false);
		in.read(int_buf);
		int style_count = Utils.toInt(int_buf, false);
		in.read(int_buf);
		int flag = Utils.toInt(int_buf, false);
		in.read(int_buf);
		int string_start = Utils.toInt(int_buf, false);
		in.read(int_buf);
		int style_start = Utils.toInt(int_buf, false);
				
		Log.d(tag, "String Count: "+ string_count +" Style Count: "+ style_count +" Flag: "+ flag +" String Start: "+ string_start +" Style Start: "+ style_start);
		
		// String pool data
		// Read index location of each String
		int[] string_indices = new int[string_count];
		if(string_count > 0){
			for(int i=0; i<string_count; i++){
				in.read(int_buf);
				string_indices[i] = Utils.toInt(int_buf, false);
			}
		}
		
		if(style_count > 0){
			// Skip Style
			in.skip(style_count * 4);
		}
		
		// Read Strings
		for(int i=0; i<string_count; i++){
			int string_len = 0;
			if(i == string_count - 1){
				if(style_start == 0)// There is no Style span
				{
					// Length of the last string. Chunk Size - Start position of this String - Header - Len of Indices
					string_len = chunk_size - string_indices[i] - header_size - 4 * string_count;
					Log.d(tag, "Last String size: "+ string_len +" Chunk_Size: "+ chunk_size +" Index: "+ string_indices[i]);
				}
				else
					string_len = style_start - string_indices[i];
			}
			else
				string_len = string_indices[i+1] - string_indices[i];

			byte[] short_buf = new byte[2];
			in.read(short_buf);
			int actual_str_len = 0;
			if(short_buf[0] == short_buf[1]) // Its repeating, happens for Non-Manifest file. e.g. 20 20
				actual_str_len = short_buf[0];
			else
				actual_str_len = Utils.toInt(short_buf, false);
			
			byte[] str_buf = new byte[actual_str_len];
			byte[] buf = new byte[string_len - 2]; // Skip 2 Length bytes, already read.
			in.read(buf);
			int j = 0;
			for (byte b : buf) {
				// Skipp 0x00
				if (b != 0x00)
					str_buf[j++] = b;
			}
			
			stringPool.add(new String(str_buf));
		}
		
		Log.d(tag, "[String Pool] Size: "+ stringPool.size());
		Log.d(tag, "[String Pool] "+ stringPool);
	}

	private void parseResMapping(byte[] rmBuf, int header_size, int chunk_size) throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(rmBuf);
		// Each ID of 4 bytes
		int num_of_res_ids = rmBuf.length/4;
		
		byte[] int_buf = new byte[4];
		for(int i=0; i<num_of_res_ids; i++){
			in.read(int_buf);
			resMap.add(Utils.toInt(int_buf, false));
		}
		Log.d(tag, "[Res Mapping] Resource Mapping "+ resMap);
	}

	private void parseStartNameSpace(byte[] nsStartBuf, int header_size, int chunk_size) throws Exception 
	{
		nodeIndex = 0;
		
		ByteArrayInputStream in = new ByteArrayInputStream(nsStartBuf);
		
		byte[] int_buf = new byte[4];
		in.read(int_buf);
		ns_linenumber = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int comment = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		ns_prefix_index = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		ns_uri_index = Utils.toInt(int_buf, false);
		
		Log.d(tag, "[Namespace Start]Line Number: "+ ns_linenumber +" Prefix: "+ stringPool.get(ns_prefix_index) + " URI: "+ stringPool.get(ns_uri_index));
	}

	private  void parseXMLStart(byte[] xmlStartBuf, int header_size, int chunk_size) throws Exception 
	{
		nodeIndex++;
		Node node = new Node();
		node.setIndex(nodeIndex);
		
		ByteArrayInputStream in = new ByteArrayInputStream(xmlStartBuf);
		
		byte[] int_buf = new byte[4];
		
		in.read(int_buf);
		int lineNumber = Utils.toInt(int_buf, false);
		node.setLinenumber(lineNumber);
		
		in.read(int_buf);
		int comment = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int ns_index = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int name_index = Utils.toInt(int_buf, false);
		
		byte[] short_buf = new byte[2];
		
		in.read(short_buf);
		int attributeStart = Utils.toInt(short_buf, false);
		
		in.read(short_buf);
		int attributeSize = Utils.toInt(short_buf, false);
		
		in.read(short_buf);
		int attributeCount = Utils.toInt(short_buf, false);
		
		// Skip ID, Class and Style index
		in.skip(6);
		
		Log.d(tag, "[XML Node] Name: "+ (name_index == -1 ? "-1" : stringPool.get(name_index)) +" Attr count: "+ attributeCount);
		
		if(name_index != -1){
			node.setName(stringPool.get(name_index));
			
			if(ns_prefix_index != -1 && ns_uri_index != -1)
			{
				node.setNamespacePrefix(stringPool.get(ns_prefix_index));
				node.setNamespaceURI(stringPool.get(ns_uri_index));
			}
		}
		
		if(attributeCount == 0){
			// No Attributes defined
			if(listener != null)
				listener.startNode(node);
			return;
		}
			
		for(int i=0; i<attributeCount; i++)
		{
			Attribute attr = new Attribute();
			
			// attr ns
			in.read(int_buf);
			int attr_ns_index = Utils.toInt(int_buf, false);
			
			// attr name
			in.read(int_buf);
			int attr_name_index = Utils.toInt(int_buf, false);
			
			// Raw value. If user has directly mentioned value e.g. android:value="1". Reference to String Pool
			in.read(int_buf);
			int attr_raw_value =  Utils.toInt(int_buf, false);
			
			String attr_value = "";
			
			if(attr_raw_value == -1){
				// No Raw value defined.
				// Read Typed Value. Reference to Resource table e.g. String.xml, Drawable etc.
				/*
				 * Size of Types value- init16
				 * Res- init8 (Always 0)
				 * Data Type- init8
				 * Data- init32. Interpreted according to dataType
				 */
				in.read(short_buf);
				int data_size = Utils.toInt(short_buf, false);
				
				// Skip res value- Always 0
				in.skip(1);

				int data_type = in.read();
				
				in.read(int_buf);
				int data = Utils.toInt(int_buf, false); // Refer to Resource Table
				attr_value = ""+ data;
				//Log.d(tag, "[Attribute] Value: "+ data);
			}
			else{
				attr_value = stringPool.get(attr_raw_value);
				//Log.d(tag, "[Attribute] Value: "+ attr_value);
				// Skip Typed value bytes
				in.skip(8);
			}
			
			if(attr_name_index != -1)
			{
				attr.setName( stringPool.get(attr_name_index));
				attr.setValue(attr_value);
				attr.setIndex(i);
				node.addAttribute(attr);
			}
			
//			Log.d(tag, "[Attribute] NameSpace: "+ (attr_ns_index == -1 ? "-1": stringPool.get(attr_ns_index)) +
//								" Name: "+ (attr_name_index == -1 ? "-1" : stringPool.get(attr_name_index)));
		}
		
		if(listener != null){
			listener.startNode(node);
		}
			
	}

	private  void parseXMLEnd(byte[] xmlEndBuf, int header_size, int chunk_size) throws Exception 
	{
		ByteArrayInputStream in = new ByteArrayInputStream(xmlEndBuf);
		
		byte[] int_buf = new byte[4];
		in.read(int_buf);
		int lineNumber = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int comment = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int ns_index = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int name_index = Utils.toInt(int_buf, false);
		
		Log.d(tag, "[XML_END] Line Number: "+ lineNumber +" Namespace: "+ ns_index + " Name: "+ (name_index == -1 ? "-1" : stringPool.get(name_index)));
		
		if(name_index != -1){
			Node node = new Node();
			node.setName(stringPool.get(name_index));
			node.setLinenumber(lineNumber);
			node.setNamespacePrefix(stringPool.get(ns_prefix_index));
			node.setNamespaceURI(stringPool.get(ns_uri_index));
			
			if(listener != null)
				listener.endNode(node);
		}
	}

	private void parseEndNameSpace(byte[] nsStartBuf, int header_size, int chunk_size) throws Exception 
	{
		ByteArrayInputStream in = new ByteArrayInputStream(nsStartBuf);
		
		byte[] int_buf = new byte[4];
		in.read(int_buf);
		int lineNumber = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int comment = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int prefix_index = Utils.toInt(int_buf, false);
		
		in.read(int_buf);
		int uri_index = Utils.toInt(int_buf, false);
		
		Log.d(tag, "[Namespace END]Line Number: "+ lineNumber + " Prefix: "+ prefix_index +" URI: "+ uri_index);
	}

	public void parseResourceTable(String arscFile) throws Exception {
		// 1. Parse Resource header
		// 2. Parse Resource string pool
		// 3. Parse Resource pacakges.
		
		// Clear
		stringPool.clear();
		
		Log.d(tag, "[Res_Table] File: "+ arscFile);
	
		
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(arscFile));

		in.read(buf_2);
		Log.d(tag, "[Res_Table] Chunk type: "+ Utils.toInt(buf_2, false));
		
		if(Utils.toInt(buf_2, false) != RES_TABLE_TYPE){
			Log.d(tag, "It's an invalid Resources.arsc file. Exiting!");
			return;
		}
		
		// Header size- 2 bytes
		in.read(buf_2);
		header_size = Utils.toInt(buf_2, false);
		
		// Chunk size- 4 bytes
		in.read(buf_4);
		chunk_size = Utils.toInt(buf_4, false);
		
		// Package count- 4 bytes
		in.read(buf_4);
		package_count = Utils.toInt(buf_4, false);
		
		Log.d(tag, "[Res_Table] Header Size: "+ header_size +" Chunk size: "+ chunk_size +" Package_count: "+ package_count);

		in.read(buf_2);
		
		Log.d(tag, "[Res_Table] Chunk type: "+ Utils.toInt(buf_2, false) +" -->"+ buf_2[0] +" "+ buf_2[1]);
		
		if(Utils.toInt(buf_2, false) == RES_STRING_POOL_TYPE) // String Pool 
		{
			// String Pool/Tokens
			Log.d(tag, "String Pool...");
			in.read(buf_2);
			header_size = Utils.toInt(buf_2, false);
						
			in.read(buf_4);
			chunk_size = Utils.toInt(buf_4, false);
						
			Log.d(tag, "String Pool...Header Size: "+ header_size +" Chunk Size: "+ chunk_size);
						
			byte[] spBuf = new byte[chunk_size - 8];
			in.read(spBuf);
						
			// Parse String pool
			parseStringPool(spBuf, header_size, chunk_size);
						
			// Get the next Chunk
			in.read(buf_2);
		}

		Log.d(tag, "[Res_Table] Chunk type: "+ Utils.toInt(buf_2, false));
		
		if(Utils.toInt(buf_2, false) == RES_TABLE_PACKAGE_TYPE) // RES_Table_Package
		{
			// Parse Resource package stream
			parseResPackage(in);
		}
		
		Log.d(tag, "Resource.arsc parsing done!!");
	}

	private void parseResPackage(BufferedInputStream in) throws Exception 
	{
		// Header size- 2 bytes
		in.read(buf_2);
		header_size = Utils.toInt(buf_2, false);

		// Chunk size- 4 bytes
		in.read(buf_4);
		chunk_size = Utils.toInt(buf_4, false);

		in.read(buf_4);
		int packg_id = Utils.toInt(buf_4, false);

		Log.d(tag, "String Pool...Header Size: " + header_size
				+ " Chunk Size: " + chunk_size + " Packg_ID: " + packg_id);

		// 128 Characters (16-bit Char)
		byte[] packg_name_buf = new byte[256];
		in.read(packg_name_buf);

		String packg_name = Utils.toString(packg_name_buf, false);
		Log.d(tag, "Package Name: " + new String(packg_name));

		// typeStrings- init32
		// Index/Offset position of Type String Pool
		in.read(buf_4);
		int typeStrings = Utils.toInt(buf_4, false);

		// Last public type
		// Index (from end) or Count of Types defined in Type String Pool (last lastPublicType entries)
		in.read(buf_4);
		int lastPublicType = Utils.toInt(buf_4, false);

		// Key String
		// Index/Offset position of Key String Pool
		in.read(buf_4);
		int keyString = Utils.toInt(buf_4, false);

		// Last index into Key string
		// Index (from end) or Count of Keys defined in Key String Pool (last lastPublicKey entries)
		in.read(buf_4);
		int lastPublicKey = Utils.toInt(buf_4, false);

		Log.d(tag, "[Res_Table] typeStrings=" + typeStrings
				+ " lastPublicType=" + lastPublicType + " keyString="
				+ keyString + " lastPublicKey=" + lastPublicKey);
		
		// Parse "Type String Pool"
		in.read(buf_2);
		if (Utils.toInt(buf_2, false) == RES_STRING_POOL_TYPE) 
		{
			// String Pool/Tokens
			Log.d(tag, "String Pool...");
			in.read(buf_2);
			header_size = Utils.toInt(buf_2, false);

			in.read(buf_4);
			chunk_size = Utils.toInt(buf_4, false);

			Log.d(tag, "String Pool...Header Size: " + header_size
					+ " Chunk Size: " + chunk_size);

			byte[] spBuf = new byte[chunk_size - 8];
			in.read(spBuf);

			// Parse String pool
			parseStringPool(spBuf, header_size, chunk_size);

			// Get the next Chunk
			in.read(buf_2);
		}
		
		// Parse "Key String Pool"
		if(Utils.toInt(buf_2, false) == RES_STRING_POOL_TYPE)
		{
			// String Pool/Tokens
			Log.d(tag, "String Pool...");
			in.read(buf_2);
			header_size = Utils.toInt(buf_2, false);

			in.read(buf_4);
			chunk_size = Utils.toInt(buf_4, false);

			Log.d(tag, "String Pool...Header Size: " + header_size
					+ " Chunk Size: " + chunk_size);

			byte[] spBuf = new byte[chunk_size - 8];
			in.read(spBuf);

			// Parse String pool
			parseStringPool(spBuf, header_size, chunk_size);

			// Get the next Chunk
			in.read(buf_2);
		}

	}
}
