/*
 * Copyright (C) 2012 Prasanta Paul, http://prasanta-paul.blogspot.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.intentextractor;

import android.util.Log;

import com.app.intentextractor.abx.Android_BX2;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Unpackage APK file with readable class, resources and XML.
 * You can Read AndroidManifest.xml from code using- android.content.pm.PackageManager.queryXX
 * TODO: 
 * 1. Parse resources.arsc
 * 2. Fail free. It shouldn't halt once first error occures.
 * 
 * @author Prasanta Paul
 *
 */
public class Extract {

	final int BUFFER = 2048;
	ArrayList<String> xmlFiles = new ArrayList<>();
	
	String tag = getClass().getSimpleName();
	
	public String getManifest(String apkFile) throws Exception
	{

		File file = new File(apkFile);
		FileInputStream fin = new FileInputStream(apkFile);
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fin));

		ZipEntry ze;
		BufferedOutputStream dest;
		
		byte[] binaryXMLManifest = null;
		while((ze = zin.getNextEntry()) != null)
		{
			Log.d(tag, "Zip entry: " + ze.getName() + " Size: "+ ze.getSize());
			if (ze.getName().equalsIgnoreCase("AndroidManifest.xml"))
			{
				String zeFolder = ze.getName();

				if(ze.isDirectory())
					continue;
				
				// Write Zip entry File to string
				int count;
				byte[] data = new byte[BUFFER];
				
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				FileOutputStream fos = new FileOutputStream(zeFile.getPath() + File.separator + zeName);
//				dest = new BufferedOutputStream(fos, BUFFER);

				while ((count = zin.read(data, 0, BUFFER)) != -1) 
				{
					baos.write(data, 0, count);
					//dest.write(data, 0, count);
				}
				baos.flush();
				baos.close();
				
				binaryXMLManifest = baos.toByteArray();
				
				break;
			}
		}

		// Close Zip InputStream
		zin.close();
		fin.close();
		
		
		Android_BX2 abx2;
		GenXML dataReceiver = new GenXML();

		abx2 = new Android_BX2(dataReceiver);
		abx2.parse( binaryXMLManifest );


		return dataReceiver.getXml();
	}

	public void decodeBX() {
		Log.d(tag, "Decode Binary XML...");
		Log.d(tag, "Number of Binary XML files: "+ xmlFiles.size());
		Log.d(tag, "-> "+ xmlFiles);

		for(int i=0; i<xmlFiles.size(); i++)
		{
			Log.d(tag, "XML File: "+ xmlFiles.get(i));

			Android_BX2 abx2;
			try{
				abx2 = new Android_BX2(new GenXML());
				abx2.parse(xmlFiles.get(i));
				
			}catch(Exception ex){
				Log.e(tag, "Fail to parse - "+ xmlFiles.get(i), ex);
			}
		}
	}

	public static ArrayList<String> main(File file){
		try{
			Extract ex = new Extract();
			
			System.out.println("Parsing data, please wait...");

			System.out.println("Done!");

			InputStream targetStream = new ByteArrayInputStream(ex.getManifest(String.valueOf(file.toPath())).getBytes());

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(targetStream);
			NodeList nl = doc.getElementsByTagName("action");

			System.out.println("Printing");

			Set<String> set = new HashSet<>();

			for(int i = 0; i<nl.getLength(); i++)
			{
				Node elem = nl.item(i);
				StringWriter buf = new StringWriter();
				Transformer xform = TransformerFactory.newInstance().newTransformer();
				xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // optional
				xform.setOutputProperty(OutputKeys.INDENT, "yes"); // optional
				xform.transform(new DOMSource(elem), new StreamResult(buf));
				set.add(buf.toString().substring(buf.toString().indexOf("\"")+1,buf.toString().lastIndexOf("\"")));
			}

			return(new ArrayList<>(set));

			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
}