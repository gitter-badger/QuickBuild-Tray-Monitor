/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 17, 2010 12:46:27 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.gui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.quickbuild.monitor.notifier.Notifier;
import com.pmease.quickbuild.monitor.utils.SWTUtils;

public class WindowManager {
	private static final Logger logger = LoggerFactory.getLogger(WindowManager.class);
	private Map<String, AbstractDialog> dialogs = new HashMap<String, AbstractDialog>();
	
	private static Map<String, Class<? extends AbstractDialog>> dialogFactory = new HashMap<String, Class<? extends AbstractDialog>>();
	
	static {
		dialogFactory.put("About", AboutDialog.class);
		dialogFactory.put("QuickView", BrowserWindow.class);
		dialogFactory.put("Preference", PreferenceDialog.class);
		dialogFactory.put("History", HistoryDialog.class);
	}
	
	public BrowserWindow browserWindow;
	public PreferenceDialog preferenceDialog;
	public AboutDialog aboutDialog;
	public HistoryDialog historyDialog;
	
	public List<Notifier> notifiers = new ArrayList<Notifier>();
	
	private final TrayMonitor monitor;

	private final Boolean[] areas;
	
	private int totalX;
	private int totalY;
	
	private static final int NOTIFICATION_TOTALS = 10;
	
	private static final int NOTIFIER_WIDTH = 320;
	private static final int NOTIFIER_HEIGHT = 120;
	
	public WindowManager(TrayMonitor monitor) {
		this.monitor = monitor;
		
		Rectangle clientArea = monitor.getShell().getMonitor().getClientArea();
		totalY = clientArea.height / (NOTIFIER_HEIGHT + 10);

		if (totalY > NOTIFICATION_TOTALS) {
			totalY = NOTIFICATION_TOTALS;
		}
		
		totalX = NOTIFICATION_TOTALS / totalY;
		
		if (totalX == 0) {
			totalX = 1;
		}
		
		areas = new Boolean[totalX * totalY];
		for (int i = 0; i < areas.length; i++) {
			areas[i] = Boolean.TRUE;
		}
	}
	
	private void activateWindow(Shell shell) {
		if (shell.getMinimized()) {
			shell.setMinimized(false);
		}
		
		shell.setVisible(true);
		shell.forceActive();
		shell.forceFocus();
	}
	
	public AbstractDialog createDialog(String name) {
		Class<? extends AbstractDialog> clazz = dialogFactory.get(name);
		try {
			Constructor<? extends AbstractDialog> constructor = clazz.getConstructor(TrayMonitor.class);
			AbstractDialog dialog = constructor.newInstance(new Object[] { monitor });
			dialogs.put(name, dialog);
			
			return dialog;
		} catch (SecurityException e) {
			logger.error("Failed create dialog " + name, e);
		} catch (NoSuchMethodException e) {
			logger.error("Failed create dialog " + name, e);
		} catch (IllegalArgumentException e) {
			logger.error("Failed create dialog " + name, e);
		} catch (InstantiationException e) {
			logger.error("Failed create dialog " + name, e);
		} catch (IllegalAccessException e) {
			logger.error("Failed create dialog " + name, e);
		} catch (InvocationTargetException e) {
			logger.error("Failed create dialog " + name, e);
		}
		
		return null;
	}
	
	public void showDialog(String name) {
		AbstractDialog dialog = dialogs.get(name);
		if (dialog == null || SWTUtils.isDisposed(dialog.getShell())) {
			dialog = createDialog(name);
			dialog.open();
		} else {
			activateWindow(dialog.getShell());
		}
	}
	
	public void showBrowser() {
		showDialog("QuickView");
	}
	
	public void showPreferenceDialog() {
		showDialog("Preference");
	}
	
	public void showAbout() {
		showDialog("About");
	}
	
	public void showHistoryDialog() {
		showDialog("History");
	}
	
	public void refreshHistoryDialog() {
		HistoryDialog dialog = (HistoryDialog) dialogs.get("History");
		if (dialog == null || SWTUtils.isDisposed(dialog.getShell())) {
			return;
		}
		
		dialog.updateTable();
	}
	
	private int getNext() {
		for (int i = 0; i < areas.length; i++) {
			if (areas[i]) {
				return i;
			}
		}
		
		return 0;
	}
	
	public void addNotifier(Notifier notifier) {
		int i = getNext();
		notifier.setIndex(i);
		areas[i] = Boolean.FALSE;
		
		Shell shell = notifier.getShell();
		shell.setSize(NOTIFIER_WIDTH, NOTIFIER_HEIGHT);
		
		Rectangle clientArea = monitor.getShell().getMonitor().getClientArea();
		
		int x = i / totalY;
		int y = i - (x * totalY);
		
		int startX = clientArea.x + clientArea.width - (NOTIFIER_WIDTH + 10) * (x + 1);
		int startY = clientArea.y + clientArea.height - (NOTIFIER_HEIGHT + 10) * (y + 1);
		
		shell.setLocation(new Point(startX, startY));
	}
	
	public void removeNotifier(Notifier notifier) {
		areas[notifier.getIndex()] = Boolean.TRUE;
	}
}
