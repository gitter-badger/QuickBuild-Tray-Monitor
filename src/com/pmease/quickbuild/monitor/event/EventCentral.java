/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 9, 2010 2:33:03 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.event;

import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class EventCentral {
	private final TrayMonitor appWindow;
	
	public AboutAction aboutAction;
	public ExitAction exitAction;
	public EditPreferenceAction editPreferenceAction;
	public TriggerViewAction triggerViewAction;
	public PauseResumeAction pauseResumeAction;
	public ViewHistoryAction viewHistoryAction;
	public ClearHistoryAction clearHistoryAction;
	
	public EventCentral(TrayMonitor appWindow) {
		this.appWindow = appWindow;
		
		registerActions();
	}
	
	private void registerActions() {
		aboutAction = new AboutAction(appWindow);
		exitAction = new ExitAction(appWindow);
		editPreferenceAction = new EditPreferenceAction(appWindow);
		triggerViewAction = new TriggerViewAction(appWindow);
		pauseResumeAction = new PauseResumeAction(appWindow);
		viewHistoryAction = new ViewHistoryAction(appWindow);
		clearHistoryAction = new ClearHistoryAction(appWindow);
	}

	public TrayMonitor getAppWindow() {
		return appWindow;
	}
}
