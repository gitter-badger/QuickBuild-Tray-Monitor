/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jun 13, 2010 3:47:21 AM
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.event;

import org.eclipse.jface.action.IAction;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class ViewHistoryAction extends AbstractAction {
	public ViewHistoryAction(TrayMonitor appWindow) {
		super("View History...", IAction.AS_PUSH_BUTTON, appWindow);
	}

	@Override
	public void run() {
		getAppWindow().windowManager.showHistoryDialog();
	}
}
