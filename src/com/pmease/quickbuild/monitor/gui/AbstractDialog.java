/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 15, 2010 10:24:34 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.quickbuild.monitor.cache.ImageCache;
import com.pmease.quickbuild.monitor.utils.SWTUtils;

public abstract class AbstractDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(AbstractDialog.class);
	
	protected final TrayMonitor monitor;
	
	abstract protected String getPreferenceName();
	abstract protected String getDefaultSize();
	
	protected AbstractDialog(TrayMonitor monitor) {
		super(monitor.getShell());
		this.monitor = monitor;
	}

	@Override
	public int open() {
		Shell shell = getShell();
		
		if (shell == null || shell.isDisposed()) {
            shell = null;
			// create the window
			create();
			
			shell = getShell();
		}

		boolean isModal = (shell.getStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL;
		if (isModal) {
			monitor.setHasModalWindow(true);
		}
		
		// open the window
		shell.open();

		shell.forceActive();
		shell.forceFocus();
		
		// run the event loop if specified
		runEventLoop(shell);
		
		return OK;
	}
	
	protected GridData controlLayoutData() {
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		gd.verticalSpan = 1;
		gd.verticalIndent= 0;
		gd.grabExcessVerticalSpace = true;
		
		gd.horizontalAlignment = SWT.CENTER;
		
		return gd;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(ImageCache.getImage("logo16.png"));
	}
	
	private void runEventLoop(Shell loopShell) {

		//Use the display provided by the shell if possible
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
			} catch (Throwable e) {
				logger.error("Exception occurred!", e);
			}
		}
		
		if (!display.isDisposed()) display.update();
	}

	@Override
	protected Point getInitialSize() {
		return SWTUtils.stringToPoint(monitor.settings.getProperty(getPreferenceName() + ".size", getDefaultSize())); 
	}
	
	@Override
	protected Point getInitialLocation(Point size) {
		Point location = SWTUtils.stringToPoint(monitor.settings.getProperty(getPreferenceName() + ".location"));
		
		if (location == null) {
			Rectangle rect = monitor.getShell().getMonitor().getClientArea();

			int x = rect.x + rect.width - (size.x + 10);
			int y = 0;
			
			if (SWTUtils.windows()) {
				y = rect.y + rect.height - (size.y + 10);
			} else {
				y = rect.y + 10;
			}
			
			return new Point(x, y);
		}
		
		return location;
	}
	
	@Override
	public boolean close() {
		boolean isModal = (getShell().getStyle() & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL;

		if (isModal) {
			monitor.setHasModalWindow(false);
		}
		
		Point size = this.getShell().getSize();
		Point location = this.getShell().getLocation();

		monitor.settings.put(getPreferenceName() + ".size", SWTUtils.pointToString(size));
		monitor.settings.put(getPreferenceName() + ".location", SWTUtils.pointToString(location));
		
		return super.close();
	}
	
	public TrayMonitor getAppWindow() {
		return monitor;
	}
}
