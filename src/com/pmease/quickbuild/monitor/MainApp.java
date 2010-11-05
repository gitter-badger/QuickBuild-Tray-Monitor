/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 14, 2010 12:24:37 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Handler;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class MainApp {
	public static void main(String[] args) throws IOException {
		// add this to skip certificate validation when connect to QuickBuild via https protocol.
		trustAll();
		
		Properties log4jProps = new Properties();
		log4jProps.load(MainApp.class.getClassLoader().getResourceAsStream("log4j.default"));
		File logFile = new File(System.getProperty("user.home"), ".qbmonitor/traymonitor.log");
		log4jProps.setProperty("log4j.appender.file.File", logFile.getAbsolutePath());
		PropertyConfigurator.configure(log4jProps);
		
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");
		for (Handler handler: logger.getHandlers())
			logger.removeHandler(handler);

		Display.setAppName("QuickBuild TrayMonitor");
		
		Display display = new Display();
		if (display.getSystemTray() == null) {
			String msg = "The system doesn't support system tray.\nPlease install QuickBuild tray monitor to below platform: Windows, Linux, Mac OS X.";
			
			MessageBox messageBox = new MessageBox(new Shell(display));
			messageBox.setText("Error");
			messageBox.setMessage(msg);
			
			messageBox.open();
			
			System.exit(-1);
		}
		
		TrayMonitor trayMonitor = new TrayMonitor(display);
		
		try {
			trayMonitor.open();
			display.dispose();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			trayMonitor.close();
			display.dispose();
		}
	}

	private static void trustAll() {
	    TrustManager[] trustAllCerts = new TrustManager[1];
	    trustAllCerts[0] = new TrustAllManager();

	    SSLContext sc;
		try {
			sc = javax.net.ssl.SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	    HostnameVerifier hv = new HostnameVerifier() {
			
	        public boolean verify(String urlHostName, SSLSession session) {
	            return true;
	        }
	        
	    };
	    HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
	
	public static class TrustAllManager implements javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
		public boolean isServerTrusted(X509Certificate[] certs) {
			return true;
		}
		
		public boolean isClientTrusted(X509Certificate[] certs) {
			return true;
		}
		
		public void checkServerTrusted(X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
		
		public void checkClientTrusted(X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}
}
