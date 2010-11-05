/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jun 13, 2010 8:42:06 AM
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;
import com.pmease.quickbuild.monitor.notifier.BuildFragment;
import com.pmease.quickbuild.monitor.notifier.BuildFragment.Status;
import com.pmease.quickbuild.monitor.utils.XMLHelper;

public class BuildHistory {
	private static final Logger logger = LoggerFactory.getLogger(BuildHistory.class);
	
	private List<BuildFragment> builds = Collections.synchronizedList(new ArrayList<BuildFragment>());
	
	static String OUTPUT_NAME = "buildhistory.xml";
	static String VERSION = "1.0";
	
	final TrayMonitor monitor;
	
	public BuildHistory(final TrayMonitor monitor) {
		this.monitor = monitor;
	}
	
	public Status getStatus() {
		synchronized(builds) {
			if (builds.isEmpty()) {
				return Status.SUCCESSFUL;
			}
			
//			Status status = Status.SUCCESSFUL;
			
			int slide = monitor.settings.getStatusWindow();
			List<BuildFragment> window;
			if (slide == 0 || builds.size() <= slide) {
				window = builds;
			} else {
				window = builds.subList(builds.size() - slide, builds.size());
			}
			
			for (BuildFragment build : window) {
				if (build.getStatus() == Status.FAILED || build.getStatus() == Status.CANCELLED) {
					return Status.FAILED;
				}
			}
			
			return Status.SUCCESSFUL;
		}
	}
	
	public int size() {
		synchronized(builds) {
			return builds.size();
		}
	}
	
	public void clear() {
		synchronized(builds) {
			builds.clear();
			monitor.updateStatus();
		}
	}
	
	public void add(List<BuildFragment> received) {
		if (received.isEmpty()) {
			return;
		}
		
		synchronized(builds) {
			int length = monitor.settings.getLengthOfNotifications();
			builds.addAll(received);
			if (length > 0 && builds.size() > length) {
				builds = new ArrayList<BuildFragment>(builds.subList(builds.size() - length, builds.size()));
			}
			
			monitor.updateStatus();
		}
	}
	
	public void save(File dir) {
		Element root = DocumentHelper.createElement("builds");
		root.addAttribute("version", VERSION);
		
		Document doc = DocumentHelper.createDocument(root);
		
		synchronized(builds) {
			for (BuildFragment build : builds) {
				root.add(build.toXML());
			}
		}
		
		File file = new File(dir, OUTPUT_NAME);
		try {
			XMLHelper.writeXML(doc, file);
		} catch (MonitorException e) {
			logger.error("Write document " + file + " failed!", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void load(File dir) {
		File file = new File(dir, OUTPUT_NAME);
		if (!file.exists()) {
			return;
		}
		
		try {
			Document doc = XMLHelper.readXML(file);
			List<Element> elements = doc.getRootElement().elements();
			
			synchronized(builds) {
				for (Element node : elements) {
					BuildFragment build = new BuildFragment();
					build.fromXML(node);
					builds.add(build);
				}
			}
		} catch (MonitorException e) {
			logger.error("Read document " + file + " failed!", e);
		}
	}

	public List<BuildFragment> getBuilds() {
		return builds;
	}

	public void setBuilds(List<BuildFragment> builds) {
		this.builds = builds;
	}
}
