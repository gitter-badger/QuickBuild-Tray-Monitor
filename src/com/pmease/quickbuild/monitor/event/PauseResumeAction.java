/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 14, 2010 1:08:48 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.event;

import org.eclipse.jface.action.IAction;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class PauseResumeAction extends AbstractAction {

	public PauseResumeAction(TrayMonitor appWindow) {
		super("Pause Check", IAction.AS_PUSH_BUTTON, appWindow);
	}

	@Override
	public void run() {
		final TrayMonitor monitor = getAppWindow();
		if (monitor.notificationHandler.isPaused()) {
			monitor.notificationHandler.unpause();
		} else {
			monitor.notificationHandler.pause();
		}
		
		monitor.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (monitor.notificationHandler.isPaused()) {
					PauseResumeAction.this.setText("Resume Check");
				} else {
					PauseResumeAction.this.setText("Pause Check");
				}
				
				monitor.updateStatus();
			}
		});
	}
}
