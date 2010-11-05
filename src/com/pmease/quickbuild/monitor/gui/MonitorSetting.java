/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 11, 2010 2:23:17 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

import com.pmease.quickbuild.monitor.utils.EncryptUtils;

public class MonitorSetting extends Properties {
	private static final long serialVersionUID = 1L;
	
	public MonitorSetting() {
		
	}
	
	public String getHost() {
		return getProperty("host", "http://localhost:8810");
	}

	public void setHost(String host) {
		setProperty("host", host);
	}

	public int getInterval() {
		return Integer.valueOf(this.getProperty("interval", "5"));
	}

	public void setInterval(int interval) {
		setProperty("interval", String.valueOf(interval));
	}

	public String getUsername() {
		return this.getProperty("username", "administrator");
	}
	
	public void setUsername(String username) {
		setProperty("username", username);
	}
	
	public String getPassword() {
		return EncryptUtils.decrypt(getProperty("password", "password"));
	}
	
	public void setPassword(String password) {
		setProperty("password", EncryptUtils.encrypt(password));
	}

	public Long getLastNotifiedId() {
		String str = getProperty("lastNotifiedId");
		if (str == null) {
			return null;
		} else {
			return Long.valueOf(str);
		}
	}
	
	public void setLastNotifiedId(Long lastNotifiedId) {
		setProperty("lastNotifiedId", lastNotifiedId.toString());
	}
	
	public int getNotificationDisplayTime() {
		return Integer.valueOf(getProperty("notificationDisplayTime", "5"));
	}
	
	public int getStatusWindow() {
		return Integer.valueOf(getProperty("statusWindow", "0"));
	}
	
	public int getLengthOfNotifications() {
		return Integer.valueOf(getProperty("lengthOfNotifications", "0"));
	}
	
	public String getQBLiteUrl() {
		return new StringBuffer(getHost()).append("/lite?refresh=").append(getInterval() * 1000).toString();
	}
	
	public String getRestUrl(String subPath) {
		return new StringBuffer(getHost()).append("/rest/").append(subPath).toString();
	}
	
	public String[] getAuthCode() {
		String username = getUsername();
		String password = getPassword();
		
		StringBuffer sb = new StringBuffer();
		byte[] encoded = Base64.encodeBase64(sb.append(username).append(":").append(password).toString().getBytes());
		
		return new String[] { "Authorization: Basic " + new String(encoded) };
	}
	
	public void load(File file) {
		try {
			InputStream in;
			if (file.exists()) {
				in =  new FileInputStream(file);
			} else {
				in = MonitorSetting.class.getClassLoader().getResourceAsStream("settings.properties");
			}
			
			if (in != null) {
				super.load(in);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void save(File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			super.store(out, "QuickBuild Monitor Settings");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
