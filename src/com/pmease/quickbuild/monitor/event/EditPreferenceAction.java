/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 11, 2010 3:19:33 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.event;

import org.eclipse.jface.action.IAction;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class EditPreferenceAction extends AbstractAction {

	public EditPreferenceAction(TrayMonitor appWindow) {
		super("Preferences ...", IAction.AS_PUSH_BUTTON, appWindow);
	}

	@Override
	public void run() {
		getAppWindow().windowManager.showPreferenceDialog();
	}
}
