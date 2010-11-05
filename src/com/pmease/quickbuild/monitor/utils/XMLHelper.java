/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jun 13, 2010 9:06:56 AM
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import com.pmease.quickbuild.monitor.MonitorException;

public class XMLHelper {
	public static Document readXML(File file) {
		SAXReader reader = new SAXReader(false);
		try {
			reader.setValidation(false);
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			return reader.read(file);
		} catch (DocumentException e) {
			throw new MonitorException(e);
		} catch (SAXException e) {
			throw new MonitorException(e);
		}
	}
	
	public static void writeXML(Document doc, OutputStream out) throws IOException {
		XMLWriter writer = null;
		try {
			OutputFormat outformat = OutputFormat.createPrettyPrint();
			outformat.setEncoding("UTF-8");
			writer = new XMLWriter(out, outformat);
			writer.write(doc);
			writer.flush();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
			
			IOUtils.closeQuietly(out);
		}
	}
	
	public static void writeXML(Document doc, File file) {
		try {
			writeXML(doc, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new MonitorException(e);
		} catch (IOException e) {
			throw new MonitorException(e);
		}
	}
}
