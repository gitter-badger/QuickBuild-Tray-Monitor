/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 9, 2010 11:07:23 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.event;

import org.eclipse.jface.action.IAction;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class TriggerViewAction extends AbstractAction {
	public TriggerViewAction(TrayMonitor trayMonitor) {
		super("View/Run Build...", IAction.AS_PUSH_BUTTON, trayMonitor);
	}
	
	@Override
	public void run() {
		getAppWindow().windowManager.showBrowser();
//		EmbeddedBrowser browser = new EmbeddedBrowser();
//		browser.displayUrl(getAppWindow().settings.getQBLiteUrl());
	}
}
