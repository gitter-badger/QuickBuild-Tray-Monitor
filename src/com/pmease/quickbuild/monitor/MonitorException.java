/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jun 13, 2010 9:11:38 AM
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor;

public class MonitorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MonitorException() {
	}
	
	public MonitorException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public MonitorException(String msg) {
		super(msg);
	}
	
	public MonitorException(Throwable e) {
		super(e);
	}
}
