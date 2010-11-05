/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 12, 2010 1:13:27 AM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.notifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;
import com.pmease.quickbuild.monitor.notifier.Notifier.NotificationType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class NotificationHandler extends Thread {
	private final Client client;
	private final TrayMonitor monitor;
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);
	
	private boolean paused = false;
	
	private boolean shutdown = false;
	private boolean connectionLost = false;
	
	public NotificationHandler(TrayMonitor monitor) {
		this.monitor = monitor;
		
		this.setName("NotificationHandler");
		this.setPriority(Thread.MIN_PRIORITY);
		
		client = new Client();
	}
	
	public void shutdown() {
		shutdown = true;
	}
	
	@Override
	public void run() {
		long lastUpdated = System.currentTimeMillis();
		
		while (!shutdown) {
			try {
				synchronized (this) {
					while (paused) {
						try {
							this.wait(300);
						} catch (InterruptedException e) {
							// ignore
						}
					}
				}

				
				while (System.currentTimeMillis() <= (lastUpdated + monitor.settings.getInterval() * 1000)) {
					Thread.yield();
				}
				
				lastUpdated = System.currentTimeMillis();

				Long lastNotifiedId = monitor.settings.getLastNotifiedId();
				List<BuildFragment> builds = getBuilds(lastNotifiedId);
				if (!builds.isEmpty()) {
					lastNotifiedId = builds.get(builds.size()-1).getBuildId();
					monitor.settings.setLastNotifiedId(lastNotifiedId);
					monitor.history.add(builds);
					monitor.windowManager.refreshHistoryDialog();
				}
				
				try {
					for (BuildFragment build : builds) {
						monitor.showBuildNotification(build);
						Thread.sleep(1000);
					}
				
					Thread.sleep(monitor.settings.getInterval() * 1000);
				} catch (InterruptedException e) {
					//Ignore
				}
			} catch (Exception e) {
				logger.error("Checking notifications failed!", e);
				shutdown = true;
				monitor.close();
			}
		}
	}

	public void pause() {
		paused = true;
	}
	
	public void unpause() {
		paused = false;
	}
	
	@SuppressWarnings("unchecked")
	public List<BuildFragment> getBuilds(Long lastNotifiedBuildId) {
		client.removeAllFilters();
		client.addFilter(new HTTPBasicAuthFilter(monitor.settings.getUsername(), monitor.settings.getPassword()));
		
		String response = null;
		
		try {
			WebResource resource = client.resource(monitor.settings.getRestUrl("notifications"));
			if (lastNotifiedBuildId != null)
				resource = resource.queryParam("last_notified_build_id", lastNotifiedBuildId.toString());
			response = resource.get(String.class);
			
			if (connectionLost == true) {
				monitor.showMessage("Connection recovered!", 
							"The connection with server " + monitor.settings.getHost() + " is recovered now.", 
							NotificationType.SUCCESS);
				connectionLost = false;
				monitor.updateStatus();
			}
		} catch (Exception e) {
			if (!connectionLost) {
				logger.error("Connection failed!", e);
				monitor.showError("Connection failed!", 
						"Connect to server " + monitor.settings.getHost() + " failed!\nMaybe server is down or your host url is not correctly configured.");
			}

//			monitor.setStatus(Status.CONNECTION_LOST);
			connectionLost = true;
			monitor.updateStatus();
			return Collections.emptyList();
		}

		if (StringUtils.isEmpty(response)) {
			return Collections.emptyList();
		}
		
		Document doc;
		try {
			doc = DocumentHelper.parseText(response);
		} catch (DocumentException e) {
			logger.error("Parse document failed!", e);
			monitor.showError("Parse response failed!", "The response from server contains error and can't be parsed!");
			return Collections.emptyList();
		}
		
		List<Element> elements = doc.getRootElement().elements();
		List<BuildFragment> builds = new ArrayList<BuildFragment>();
		for (Element element : elements) {
			BuildFragment build = new BuildFragment();
			build.fromRest(element);
			String configurationId = element.elementText("configuration");
        	String configurationPath = getConfigurationPath(configurationId);
        	build.setConfigurationPath(configurationPath);
        	if (StringUtils.isEmpty(build.getTriggeredBy())) {
        		if (build.isScheduled()) {
        			build.setTriggeredBy("Unknown");
        		} else {
        			build.setTriggeredBy("Scheduler");
        		}
        	} else {
        		build.setTriggeredBy(getTriggeredBy(build.getTriggeredBy()));
        	}
        	
        	builds.add(build);
		}
		
		return builds;
	}
	
	/*
	@SuppressWarnings({ "unchecked" })
	public void checkNotifications() {
		Date now = new Date();
		String response;
		
		try {
			client.addFilter(new HTTPBasicAuthFilter(monitor.setting.getUsername(), monitor.setting.getPassword()));
			
			response = client.resource(monitor.setting.getRestUrl("notifications"))
						.queryParam("from", new SimpleDateFormat(DATETIME_FORMAT).format(lastUpdateDate))
						.queryParam("to", new SimpleDateFormat(DATETIME_FORMAT).format(now))
						.get(String.class);
			
			monitor.updateStatus(TrayMonitor.Status.OK);
		} catch (Exception e) {
			Throwable throwable = ExceptionUtils.getCause(e);
			if (throwable instanceof ConnectException) {
				monitor.showErrorNotification("Connection failed!", 
						  "Connect to host " + monitor.setting.getHost() 
						  + " failed!\nPlease configure your host correctly in 'Preferences'.");
			} else {
				monitor.showErrorNotification("Retrieve Response Error!", e.getMessage());
			}
			
			monitor.updateStatus(TrayMonitor.Status.ERROR);
			return;
		}

		Document doc;
		
		try {
			doc = DocumentHelper.parseText(response);
		} catch (DocumentException e) {
			monitor.showErrorNotification("Parse Response Error!", "Parse quickbuild notifications response failed!");
			return;
		}
		
		List<Element> elements = doc.getRootElement().elements();
		
		for (Element element : elements) {
			final BuildWrapper build = new BuildWrapper(element);
			String configurationId = element.elementText("configuration");
        	String configurationPath = getConfigurationPath(configurationId);
        	build.setConfigurationPath(configurationPath);
        	
        	System.out.println(element.asXML());
        	
        	monitor.showBuildNotification(build);
		}
		
		lastUpdateDate = now;
	}
	*/
	
	private String getConfigurationPath(String id) {
		return client.resource(monitor.settings.getRestUrl("configurations/" + id + "/path")).get(String.class);
	}

	private String getTriggeredBy(String id) {
		return client.resource(monitor.settings.getRestUrl("users/" + id + "/display_name")).get(String.class);
	}
	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isConnectionLost() {
		return connectionLost;
	}

	public void setConnectionLost(boolean connectionLost) {
		this.connectionLost = connectionLost;
	}

}
