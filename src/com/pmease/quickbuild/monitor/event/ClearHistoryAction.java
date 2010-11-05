/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jun 13, 2010 9:34:49 PM
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.event;

import org.eclipse.jface.action.IAction;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class ClearHistoryAction extends AbstractAction {

	public ClearHistoryAction(TrayMonitor appWindow) {
		super("Clear History", IAction.AS_PUSH_BUTTON, appWindow);
	}
	
	@Override
	public void run() {
		getAppWindow().clearHistory();
	}
}
