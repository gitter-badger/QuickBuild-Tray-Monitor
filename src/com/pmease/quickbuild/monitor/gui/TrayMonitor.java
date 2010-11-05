/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 9, 2010 8:38:15 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.gui;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.quickbuild.monitor.BuildHistory;
import com.pmease.quickbuild.monitor.cache.ColorCache;
import com.pmease.quickbuild.monitor.cache.FontCache;
import com.pmease.quickbuild.monitor.cache.ImageCache;
import com.pmease.quickbuild.monitor.event.EventCentral;
import com.pmease.quickbuild.monitor.notifier.BuildFragment;
import com.pmease.quickbuild.monitor.notifier.BuildNotifier;
import com.pmease.quickbuild.monitor.notifier.NotificationHandler;
import com.pmease.quickbuild.monitor.notifier.Notifier;
import com.pmease.quickbuild.monitor.notifier.BuildFragment.Status;
import com.pmease.quickbuild.monitor.notifier.Notifier.NotificationType;
import com.pmease.quickbuild.monitor.utils.SWTUtils;

public class TrayMonitor {
	private static final Logger logger = LoggerFactory.getLogger(TrayMonitor.class);
	
	private final Display display;
	
	private Shell shell;
	private Tray tray;
	private TrayItem trayItem;
	private Menu trayMenu;
	private MenuManager menuManager;
	private EventCentral eventCentral;
	
	public MonitorSetting settings;
	
	public NotificationHandler notificationHandler;
	public WindowManager windowManager;
	
	public BuildHistory history;
	public boolean hasModalWindow;
	
	private static final String APP_NAME = "QuickBuild Monitor";
	
	/*
	public enum Status {
		IDLE("idle.png"), 
		CONNECTION_LOST("connection-failed.png"), 
		BUILDS_PASSED("green-ball.png"),
		BUILDS_FAILED("red-ball.png");
		
		private String iconFile;
		
		Status(String iconFile) {
			this.iconFile = iconFile;
		}
		
		public String getIconFile() {
			return iconFile;
		}
	}
	
	public Status status = Status.IDLE;
	*/
	public final File settingDir;
	
	public TrayMonitor(Display display) {
		settingDir = getSettingDir();
		
		this.display = display;
		this.settings = new MonitorSetting();
		this.settings.load(new File(settingDir, "settings.properties"));
		
		this.notificationHandler = new NotificationHandler(this);
		this.notificationHandler.start();
		
		create();
		
		this.windowManager = new WindowManager(this);
		
		history = new BuildHistory(this);
	}

	public File getSettingFile() {
		return new File(getSettingDir(), "settings.properties");
	}
	
	public File getSettingDir() {
		String home = System.getProperty("user.home");
		String dirName = ".qbmonitor";
		
		File file = new File(home, dirName);
		
		if (!file.exists()) {
			try {
				FileUtils.forceMkdir(file);
			} catch (IOException e) {
				throw new RuntimeException("Unable to create QuickBuild monitor settings directory.");
			}
		}
		
		return file;
	}
	
	private void create() {
		shell = createShell();
		shell.setVisible(false);
		eventCentral = new EventCentral(this);
		
		tray = display.getSystemTray();
		trayItem = new TrayItem(tray, SWT.NONE);
		trayItem.setToolTipText(APP_NAME);
		
		Image image = ImageCache.getImage("notification-good.png");
		trayItem.setImage(image);
		
		menuManager = new MenuManager();
		
		menuManager.add(eventCentral.triggerViewAction);
		menuManager.add(eventCentral.pauseResumeAction);
		menuManager.add(new Separator());
		menuManager.add(eventCentral.viewHistoryAction);
		menuManager.add(eventCentral.clearHistoryAction);
		menuManager.add(new Separator());
		menuManager.add(eventCentral.editPreferenceAction);
		menuManager.add(new Separator());
		menuManager.add(eventCentral.aboutAction);
		menuManager.add(new Separator());
		menuManager.add(eventCentral.exitAction);
		
		trayMenu = menuManager.createContextMenu(shell);

		trayItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				trayMenu.setVisible(true);
			}
		});
	}
	
	public void clearHistory() {
		history.clear();
	}

	public void updateMenus() {
		Display display = shell.getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		
		display.asyncExec(new Runnable() {
			public void run() {
				MenuItem[] items = trayMenu.getItems();
				for (MenuItem each : items) {
					String text = each.getText();
					if (!text.equalsIgnoreCase("Exit")) {
						each.setEnabled(!hasModalWindow);
					}
				}
			}
		});
	}
	
	public void updateStatus() {
		if (shell == null || shell.isDisposed()) {
			return;
		}
		
		Display display = shell.getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		
		display.asyncExec(new Runnable() {
			public void run() {
				StringBuffer sb = new StringBuffer(APP_NAME);
				sb.append(" (").append(history.size()).append(" messages)");
				
				if (notificationHandler.isConnectionLost()) {
					sb.append(" - Disconnected");
				} else if (notificationHandler.isPaused()) {
//					image = ImageCache.getImage("yellow-ball.png");
					sb.append(" - Paused");
//					image = ImageCache.getImage(TrayMonitor.this.status.getIconFile());
				}
				
				trayItem.setToolTipText(sb.toString());
				
				Status status = history.getStatus();
				
				Image image;
				if (notificationHandler.isConnectionLost()) {
					image = ImageCache.getImage("notification-disconnected.png");
				} else if (notificationHandler.isPaused()) {
					image = ImageCache.getImage("notification-paused.png");
				} else if (status == Status.SUCCESSFUL) {
					image = ImageCache.getImage("notification-good.png");
				} else {
					image = ImageCache.getImage("notification-error.png");
				}
				/*	
					boolean paused = notificationHandler.isPaused();
					
					String str = paused ? "paused-" : "connected-";
					switch (status) {
					case FAILED:
						str = str + "error.png";
						break;
						
					case CANCELLED:
						str = str + "warn.png";
						break;
						
					default:
						str = str + "good.png";
						break;
					}
					
					image = ImageCache.getImage(str);
				}
				*/
				trayItem.setImage(image);
			}
		});
	}

	/*
	public void setStatus(Status status) {
		if (this.status == status) {
			return;
		} else {
			this.status = status;
			updateStatus();
		}
	}
	
	public Status getStatus() {
		return this.status;
	}
	*/
	
	public void showBuildNotification(final BuildFragment build) {
		Display display = getDisplay();
		
		display.asyncExec(new Runnable() {
			public void run() {
				BuildNotifier notifier = new BuildNotifier(TrayMonitor.this, build);
				notifier.open();				
			}
		});
	}
	
	public void showMessage(final String title, final String msg, final NotificationType type) {
		Display display = getDisplay();
		
		display.asyncExec(new Runnable() {
			public void run() {
				String message = msg;
				
				if (message == null) {
					message = "Unknown error!";
				}
				
				Notifier notifier = new Notifier(TrayMonitor.this, title, message, type);
				notifier.open();			
			}
		});
	}
	
	public void showError(final String title, final String msg) {
		Display display = getDisplay();
		
		display.asyncExec(new Runnable() {
			public void run() {
				String message = msg;
				if (message == null) {
					message = "Unknown error!";
				}
				
				Notifier notifier = new Notifier(TrayMonitor.this, title, message, NotificationType.ERROR);
				notifier.open();			
			}
		});
	}
	
	private Shell createShell() {
		Shell shell = new Shell(display, SWT.NO_TRIM);
		shell.setText(APP_NAME);
		shell.setToolTipText(APP_NAME);
		Image image = ImageCache.getImage("logo128.png");
		shell.setImage(image);

		shell.setSize(new Point(0, 0));
		
		return shell;
	}
	
	public int open() {
		if (SWTUtils.isDisposed(shell)) {
			shell = null;
			create();
		}
		
		shell.setVisible(false);
//		shell.open();
		
		runEventLoop(shell);
		
		return Window.OK;
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public Display getDisplay() {
		if (display == null || display.isDisposed()) {
			return Display.getCurrent();
		}
		
		return display;
	}
	
	private void runEventLoop(Shell loopShell) {
		Display display;
		Shell shell = getShell();
		
		if (shell == null) {
			display = Display.getCurrent();
		} else {
			display = loopShell.getDisplay();
		}
		
		while (loopShell != null && !loopShell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
		}
	}
	
	public boolean close() {
		ImageCache.dispose();
		ColorCache.dispose();
		FontCache.dispose();
		
		menuManager.dispose();
		menuManager = null;
		
		trayItem.dispose();
		trayItem = null;

		notificationHandler.shutdown();
		
		if (shell == null || shell.isDisposed()) {
			return true;
		}
		
		shell.dispose();
		shell = null;
		
		settings.save(new File(settingDir, "settings.properties"));
		
		return true;
	}
	
	public EventCentral getEventCentral() {
		return eventCentral;
	}
	
	public void setHasModalWindow(boolean b) {
		this.hasModalWindow = b;
		updateMenus();
	}
}
