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

package com.app.opcodeextractor;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.app.opcodeextractor.abx.Android_BX2;
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
	ArrayList<String> xmlFiles = new ArrayList<String>();
	String dexFile = null;
	String resFile = null;
	boolean debug = true;
	
	String tag = getClass().getSimpleName();

	public void unZip(String apkFile) throws Exception 
	{
		Log.d(tag, apkFile);
		File file = new File(apkFile);
	
		/*
		 * Create the Base Directory, whose name should be same as Zip file name.
		 * All decompressed contents will be placed under this folder.
		 */
		String apkFileName = file.getName();
		
		if(apkFileName.indexOf('.') != -1)
			apkFileName = apkFileName.substring(0,  apkFileName.indexOf('.'));

		Log.d(tag, "Folder name: "+ apkFileName);
		
		File extractFolder = new File((file.getParent() == null ? "" : file.getParent() + File.separator) + apkFileName);
		if(!extractFolder.exists())
			extractFolder.mkdir();
		
		/*
		 * Read zip entries.
		 */
		FileInputStream fin = new FileInputStream(apkFile);
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fin));
		
		/*
		 * Zip InputStream shifts its index to every Zip entry when getNextEntry() is called.
		 * If this method returns null, Zip InputStream reaches EOF.
		 */
		ZipEntry ze = null;
		BufferedOutputStream dest;
		
		while((ze = zin.getNextEntry()) != null)
		{
			Log.d(tag, "Zip entry: " + ze.getName() + " Size: "+ ze.getSize());
			/*
			 * Create decompressed file for each Zip entry. A Zip entry can be a file or directory.
			 * ASSUMPTION: APK Zip entry uses Unix style File seperator- "/"
			 * 
			 * 1. Create the prefix Zip Entry folder, if it is not yet available
			 * 2. Create the individual Zip Entry file.
			 */
			String zeName = ze.getName();
			String zeFolder = zeName;
			
			if(ze.isDirectory())
			{
				zeName = null; // Don't create  Zip Entry file
			}
			else{
				if(!zeName.contains("/")) // Zip entry uses "/"
					zeFolder = null; // It is File. don't create Zip entry Folder
				else {
					zeFolder = zeName.substring(0, zeName.lastIndexOf("/"));
					zeName = zeName.substring( zeName.lastIndexOf("/") + 1);
				}
			}

			Log.d(tag, "zeFolder: "+ zeFolder +" zeName: "+ zeName);
			
			// Create Zip Entry Folder
			File zeFile = extractFolder;
			if(zeFolder != null)
			{
				zeFile = new File(extractFolder.getPath() + File.separator + zeFolder);
				if(!zeFile.exists())
					zeFile.mkdirs();
			}
			
			// Create Zip Entry File
			if(zeName == null)
				continue;
			
			// Keep track of XML files, they are in Android Binary XML format
			if(zeName.endsWith(".xml"))
				xmlFiles.add(zeFile.getPath() + File.separator + zeName);
			
			// Keep track of the Dex/ODex file. Need to convert to Jar
			if(zeName.endsWith(".dex") || zeName.endsWith(".odex"))
				dexFile = zeFile.getPath() + File.separator + zeName;
			
			// Keep track of Resources.arsc file- resources.arsc
			if(zeName.endsWith(".arsc"))
				resFile = zeFile.getPath() + File.separator + zeName;
			
			// Write Zip entry File to the disk
			int count;
			byte[] data = new byte[BUFFER];
			
			FileOutputStream fos = new FileOutputStream(zeFile.getPath() + File.separator + zeName);
			dest = new BufferedOutputStream(fos, BUFFER);

			while ((count = zin.read(data, 0, BUFFER)) != -1) 
			{
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
		}

		// Close Zip InputStream
		zin.close();
		fin.close();
	}
	
	public String getManifest(String apkFile) throws Exception
	{

		File file = new File(apkFile);
	
		
		/*
		 * Read zip entries.
		 */
		FileInputStream fin = new FileInputStream(apkFile);
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fin));
		
		/*
		 * Zip InputStream shifts its index to every Zip entry when getNextEntry() is called.
		 * If this method returns null, Zip InputStream reaches EOF.
		 */
		ZipEntry ze = null;
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
				byte data[] = new byte[BUFFER];
				
				
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
		
		abx2 = null;

		
		return dataReceiver.getXml();
	}
	
	/**
	 * Decode binary XML files
	 */
	public void decodeBX() throws Exception {
		Log.d(tag, "Decode Binary XML...");
		Log.d(tag, "Number of Binary XML files: "+ xmlFiles.size());
		Log.d(tag, "-> "+ xmlFiles);

		
		// Convert WBXML to XML
		/*
		 * aapt (Android Assents Packaging Tool) converts XML files to Android Binary XML. It is not same as
		 * WBXML format.
		 */
		for(int i=0; i<xmlFiles.size(); i++)
		{
			Log.d(tag, "XML File: "+ xmlFiles.get(i));
			
			// 23rd March, 2012. Prasanta. 
			// Skip exception while parsing any file and complete the complete parsing cycle.
			Android_BX2 abx2;
			try{
				abx2 = new Android_BX2(new GenXML());
				abx2.parse(xmlFiles.get(i));
				
			}catch(Exception ex){
				Log.e(tag, "Fail to parse - "+ xmlFiles.get(i), ex);
			}
			finally{
				abx2 = null;
			}
		}
	}

	public void decodeResource() throws Exception {
		Android_BX2 abx = new Android_BX2(null);
		abx.parseResourceTable(resFile);
	}

	public static String main(File file){
		try{
			Extract ex = new Extract();
			
			System.out.println("Parsing data, please wait...");

			System.out.println("Done!");

			return(ex.getManifest(String.valueOf(file.toPath())));
			
			// Unzip content
//			ex.unZip(file);
			// Parse Binary XML
//			ex.decodeBX();
			// Decode DEX file
//			ex.decodeDex();

			// EXperimentel. Still working on it...
			//ex.decodeResource();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return "";
	}
}
