/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 9, 2010 2:28:00 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.event;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class AbstractAction extends Action {
	private final TrayMonitor appWindow;
	
	public AbstractAction(String text, int style, TrayMonitor appWindow) {
		super(text, style);
		this.appWindow = appWindow;
	}
	
	public TrayMonitor getAppWindow() {
		return appWindow;
	}
	
	public Shell getParentShell() {
		return appWindow.getShell();
	}
	
	@Override
	public boolean isEnabled() {
		return appWindow.hasModalWindow == false;
	}
}
