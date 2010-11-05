/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 10, 2010 12:48:20 AM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowAdapter;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.pmease.quickbuild.monitor.utils.SWTUtils;

public class BrowserWindow extends AbstractDialog {
	private Browser browser;
	private Browser externalBrowser;
	
	public BrowserWindow(TrayMonitor trayMonitor) {
		super(trayMonitor);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		browser = new Browser(parent.getShell(), SWT.NONE);
		browser.addOpenWindowListener(new OpenWindowListener() {
			public void open(WindowEvent event) {
				if (SWTUtils.isDisposed(externalBrowser)) {
					final Shell externalShell = new Shell(getShell(), SWT.NONE);
					externalBrowser = new Browser(externalShell, SWT.NONE);
					externalBrowser.addLocationListener(new LocationAdapter() {
						@Override
						public void changing(LocationEvent event) {
							event.doit = false;
							Program.launch(event.location);
						}
					});
					
					externalShell.setVisible(false);
				}
				
				event.browser = externalBrowser;
			}
		});
		
		browser.addTitleListener(new TitleListener() {
			public void changed(TitleEvent event) {
				browser.getShell().setText(event.title);
			}
    	});
    	
    	browser.addVisibilityWindowListener(new VisibilityWindowAdapter() {
			@Override
			public void hide(WindowEvent event) {
				Browser browser = (Browser) event.widget;
				Shell shell = browser.getShell();
				shell.setVisible(false);
			}

			@Override
			public void show(WindowEvent event) {
				Browser browser = (Browser) event.widget;
				final Shell shell = browser.getShell();
				if (event.location != null)
					shell.setLocation(event.location);
				
				Point size = new Point(880, 400);
				shell.setSize(shell.computeSize(size.x, size.y));
				
				shell.open();
			}
		});
    	
		browser.setUrl(monitor.settings.getQBLiteUrl(), null, monitor.settings.getAuthCode());
		
		return parent;
	}
	
	@Override
	protected boolean canHandleShellCloseEvent() {
		return true;
	}
	
	@Override
	public boolean close() {
		if (browser != null && !browser.isDisposed()) {
			browser.dispose();
		}
		
		if (externalBrowser != null && !externalBrowser.isDisposed()) {
			externalBrowser.dispose();
		}
		
		return super.close();
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}
	
	@Override
	protected Layout getLayout() {
		return new FillLayout();
	}

	@Override
	protected String getPreferenceName() {
		return "QuickView";
	}

	@Override
	protected String getDefaultSize() {
		return "550,300";
	}
}
