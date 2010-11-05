/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 9, 2010 2:25:27 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.event;

import org.eclipse.jface.action.IAction;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class ExitAction extends AbstractAction {
	public ExitAction(TrayMonitor appWindow) {
		super("Exit", IAction.AS_PUSH_BUTTON, appWindow);
	}
	
	@Override
	public void run() {
		getAppWindow().close();
	}
}
