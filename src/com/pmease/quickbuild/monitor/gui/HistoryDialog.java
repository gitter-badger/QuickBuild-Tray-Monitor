/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jun 13, 2010 3:38:16 AM
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.pmease.quickbuild.monitor.cache.ColorCache;
import com.pmease.quickbuild.monitor.notifier.BuildFragment;
import com.pmease.quickbuild.monitor.notifier.BuildFragment.Status;
import com.pmease.quickbuild.monitor.utils.SWTUtils;

public class HistoryDialog extends AbstractDialog {
	private Table table;
	private TableColumn[] columns;
	
	public HistoryDialog(TrayMonitor monitor) {
		super(monitor);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.NONE);
	}

	@Override
	public boolean close() {
		for (int i = 0; i < columns.length; i++) {
			monitor.settings.setProperty(getColumnId(i),  String.valueOf(columns[i].getWidth()));
		}
		
		return super.close();
	}
	
	static int CLEAR_ID = 9999;
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Close", true);
		createButton(parent, CLEAR_ID, "Clear", false);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == CLEAR_ID) {
			clearHistory();
		} else {
			super.buttonPressed(buttonId);
		}
	}
	
	private void clearHistory() {
		monitor.history.clear();
		table.removeAll();
		this.updateTable();
	}
	
	private String getColumnId(int index) {
		return getPreferenceName() + ".column-" + index;
	}
	
	private TableColumn createColumn(String title, final int index) {
		final TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(title);
		column.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				monitor.settings.setProperty(getColumnId(index), String.valueOf(column.getWidth()));
			}
		});
		return column;
	}
	
	private static int[] WIDTH = new int[] {
		90, 100, 230, 100, 150, 90
	};
	
	@Override
	protected Control createDialogArea(Composite container) {
		final Composite parent = (Composite) super.createDialogArea(container);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 5;
		layout.marginWidth = 0;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		CLabel noteLabel = new CLabel(parent, SWT.WRAP);
		StringBuffer sb = new StringBuffer("To reset your tray monitor status, just clear all messages below.");
//		if (monitor.notificationHandler.isConnectionLost()) {
//			sb.append("\nCan't connect to QuickBuild server: " + monitor.settings.getHost() + " now !");
//		}
//		if (monitor.notificationHandler.isPaused()) {
//			sb.append("\nChecking new notifications is paused!");
//		}
		noteLabel.setText(sb.toString());
		GridDataFactory.fillDefaults().indent(10, 10).align(SWT.LEFT, SWT.CENTER).applyTo(noteLabel);
		
		
		table = new Table(parent, SWT.V_SCROLL|SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		table.setLayoutData(data);
		
		columns = createColumns();
		
		for (int i = 0; i < columns.length; i++) {
			String str = monitor.settings.getProperty(getColumnId(i));
			if (!StringUtils.isEmpty(str)) {
				columns[i].setWidth(Integer.valueOf(str));
			} else {
				columns[i].setWidth(WIDTH[i]);
			}
		}
		
		fillTable();
		
		return parent;
	}

	static final Color RED = ColorCache.getColor(255, 26, 0);
	static final Color GREEN = ColorCache.getColor(0, 140, 0);
	static final Color LIGHT_RED = ColorCache.getColor(255, 238, 238);
	static final Color LIGHT_GREEN = ColorCache.getColor(238, 255, 238);
	
	private TableColumn[] createColumns() {
		return new TableColumn[] {
				createColumn("St.", 0),
				createColumn("Version", 1),
				createColumn("Configuration", 2),
				createColumn("By", 3),
				createColumn("Finished At", 4),
				createColumn("Duration", 5)
		};
	}
	
	private void fillItem(TableItem item, final BuildFragment build) {
		if (build.getStatus() == Status.SUCCESSFUL || build.getStatus() == Status.RECOMMENDED) {
			item.setBackground(LIGHT_GREEN);
			item.setForeground(0, GREEN);
		} else {
			item.setBackground(LIGHT_RED);
			item.setForeground(0, RED);
		}
		
		item.setText(0, build.getStatus().name());
		
    	final TableEditor versionEditor = new TableEditor(table);
		versionEditor.grabHorizontal = true;
		versionEditor.grabVertical = true;
		final Link link = new Link(table, SWT.NONE);
		
		final String buildUrl = monitor.settings.getHost() + build.getUrl();
		link.setText("<a>" + build.getVersion() + "</a>");
		
		if (build.getStatus() == Status.SUCCESSFUL || build.getStatus() == Status.RECOMMENDED) {
			link.setBackground(LIGHT_GREEN);
		} else {
			link.setBackground(LIGHT_RED);
		}
		
		link.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event event) {
		    	  Program.launch(buildUrl);
		      }
	    });

		versionEditor.setEditor(link, item, 1);
		
//		item.setText(1, build.getVersion());
		item.setText(2, build.getConfigurationPath());
		item.setText(3, build.getTriggeredBy());
		item.setText(4, SWTUtils.getDateFormat().format(build.getEndDate()));
		long duration = build.getDuration();
		if (duration < 1000) {
			item.setText(5, duration + " ms");
		} else {
			item.setText(5, DurationFormatUtils.formatDuration(duration, "HH:mm:ss"));
		}
		
		item.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				link.dispose();
			}
			
		});
	}
	
	public void updateTable() {
		Display display = getShell().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		
		display.asyncExec(new Runnable() {
			public void run() {
				fillTable();
			}
		});
	}
	
	private void fillTable() {
		table.setRedraw(false);
		table.removeAll();
	
		List<BuildFragment> builds = new ArrayList<BuildFragment>(monitor.history.getBuilds());
		
		Collections.reverse(builds);
		for (BuildFragment build : builds) {
			TableItem item = new TableItem(table, SWT.BORDER);
			fillItem(item, build);
		}
		
		table.setRedraw(true);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				updateTable();
			}
		});
		
		newShell.setText("History Notifications");
	}
	@Override
	protected String getDefaultSize() {
		return "780,400";
	}

	@Override
	protected String getPreferenceName() {
		return "History";
	}
}
