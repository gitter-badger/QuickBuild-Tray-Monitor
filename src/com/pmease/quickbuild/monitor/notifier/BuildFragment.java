/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 12, 2010 1:22:45 AM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.notifier;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.pmease.quickbuild.monitor.notifier.Notifier.NotificationType;
import com.pmease.quickbuild.monitor.utils.SWTUtils;

public class BuildFragment {
	public enum Status {SUCCESSFUL, FAILED, CANCELLED, RUNNING, RECOMMENDED};
	
	private String configurationPath;
	
	private Long buildId;
	private String version;
	private Date beginDate;
	private long duration;
	private Date endDate;
	private Status status;
	private boolean isScheduled;
	private String triggeredBy;
	
	public BuildFragment() {
	}
	
	public void fromRest(Element node) {
		if (node == null) {
			return;
		}
		
		buildId = Long.valueOf(node.elementTextTrim("id"));
		version = node.elementTextTrim("version");
		status = Status.valueOf(node.elementTextTrim("status"));
		isScheduled = Boolean.valueOf(node.elementTextTrim("scheduled"));
		triggeredBy = node.elementTextTrim("requester");
		
		try {
			beginDate = SWTUtils.getISO8601Format().parse(node.elementTextTrim("beginDate"));
		} catch (ParseException e) {
			beginDate = null;
		}
		
		duration = Long.valueOf(node.elementTextTrim("duration"));
		endDate = new Date(beginDate.getTime() + duration);
	}
	
	public void fromXML(Element node) {
		configurationPath = node.attributeValue("path");
		buildId = Long.valueOf(node.attributeValue("id"));
		version = node.attributeValue("version");
		try {
			beginDate = SWTUtils.getISO8601Format().parse(node.attributeValue("beginDate"));
			endDate = SWTUtils.getISO8601Format().parse(node.attributeValue("endDate"));
		} catch (ParseException e) {
			// ignore
		}
		
		duration = Long.valueOf(node.attributeValue("duration"));
		status = Status.valueOf(node.attributeValue("status"));
		triggeredBy = node.attributeValue("triggeredBy");
	}
	
	public Element toXML() {
		Element node = DocumentHelper.createElement("build");
		node.addAttribute("path", configurationPath);
		node.addAttribute("id", buildId.toString());
		node.addAttribute("version", version);
		node.addAttribute("beginDate", SWTUtils.getISO8601Format().format(beginDate));
		node.addAttribute("endDate", SWTUtils.getISO8601Format().format(endDate));
		node.addAttribute("duration", String.valueOf(duration));
		node.addAttribute("status", status.name());
		node.addAttribute("triggeredBy", triggeredBy);
		
		return node;
	}
	
	public String getUrl() {
		return "/build/" + buildId;
	}
	
	public String getNotifierTitle() {
		if (status == Status.SUCCESSFUL) {
			return getBuildName() + " finished successfully!";
		} else {
			return getBuildName() + " is " + status.name().toLowerCase();
		}
	}
	
	public String getBuildName() {
		String[] tokens = StringUtils.split(configurationPath, '/');
		
		return tokens[tokens.length - 1] + " " + version;
	}
	
	public String getNotifierMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("Build (").append(getBuildName()).append(")");
		
		if (status == Status.SUCCESSFUL) {
			sb.append(" finished successfully");
		} else {
			sb.append(" is ").append(status.name().toLowerCase());
		}
		
		sb.append(" at ").append(SWTUtils.getDateFormat().format(endDate));
		sb.append(" (in total ").append(SWTUtils.formatDuration(duration)).append(").");
		
		return sb.toString();
	}
	
	public NotificationType getNotificationType() {
		switch (status) {
		case SUCCESSFUL:
			return NotificationType.SUCCESS;
			
		case CANCELLED:
			return NotificationType.ERROR;
			
		case FAILED:
			return NotificationType.FATAL;
			
		default:
			return NotificationType.WARNING;
		}
	}
	
	public String getConfigurationPath() {
		return configurationPath;
	}
	
	public void setConfigurationPath(String configurationPath) {
		this.configurationPath = configurationPath;
	}
	
	public Long getBuildId() {
		return buildId;
	}
	
	public void setBuildId(Long buildId) {
		this.buildId = buildId;
	}
	
	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getTriggeredBy() {
		return triggeredBy;
	}

	public void setTriggeredBy(String triggeredBy) {
		this.triggeredBy = triggeredBy;
	}

	public boolean isScheduled() {
		return isScheduled;
	}

	public void setScheduled(boolean isScheduled) {
		this.isScheduled = isScheduled;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Build ")
		  .append(version)
		  .append(" for configuration ")
		  .append(configurationPath)
		  .append(" started at ")
		  .append(SWTUtils.getDateFormat().format(beginDate))
		  .append(" and finished at ")
		  .append(SWTUtils.getDateFormat().format(endDate))
		  .append(" ").append(status);
		
		return sb.toString();
	}
}
