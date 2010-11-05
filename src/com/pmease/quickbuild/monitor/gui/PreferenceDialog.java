/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 11, 2010 2:54:20 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.gui;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pmease.quickbuild.monitor.cache.ColorCache;
import com.pmease.quickbuild.monitor.cache.FontCache;
import com.pmease.quickbuild.monitor.utils.EncryptUtils;

public class PreferenceDialog extends AbstractDialog {
	private Map<String, Text> controls = new HashMap<String, Text>();

	public PreferenceDialog(TrayMonitor trayMonitor) {
		super(trayMonitor);
		
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.NONE);
	}

	@Override
	protected Control createDialogArea(Composite container) {
		Composite parent = (Composite) super.createDialogArea(container);
//		parent.setLayoutData(new GridData(SWT.FILL));
		
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Tray Monitor Preferences");
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginLeft = layout.marginRight = 20;
		layout.marginTop = layout.marginBottom = 10;
		layout.verticalSpacing = 8;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		FontData fd = group.getFont().getFontData()[0];
		fd.height = 14;
		fd.setStyle(SWT.BOLD);
		group.setFont(FontCache.getFont(fd));
		
		addField(group, "host", "QuickBuild Host:", monitor.settings.getHost());
		addField(group, "username", "QuickBuild User:", monitor.settings.getUsername());
		addField(group, "password", "QuickBuild Password:", monitor.settings.getPassword(), SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		addField(group, "interval", "Check notifications every", String.valueOf(monitor.settings.getInterval()), "seconds");
		addField(group, "notificationDisplayTime", "Close notification after", String.valueOf(monitor.settings.getNotificationDisplayTime()), "seconds");
		addField(group, "lengthOfNotifications", "Total notifications in history:", String.valueOf(monitor.settings.getLengthOfNotifications()), null,
				"Defines the number of notifications which will be stored.\nZero means all notifications will be stored in history.",
				SWT.SINGLE | SWT.BORDER);
		addField(group, "statusWindow", "Tray icon based on last", String.valueOf(monitor.settings.getStatusWindow()), "notifications", 
				"Zero if you want tray icon is based on all notifications in history.", SWT.SINGLE | SWT.BORDER);

		return parent;
	}
	
	private void addField(Composite parent, String property, String fieldName, String value) {
		addField(parent, property, fieldName, value, -1);
	}
	
	private void addField(Composite parent, String property, String fieldName, String value, int style) {
		addField(parent, property, fieldName, value, null, null, style);
	}
	
	private void addField(Composite parent, String property, String fieldName, String value, String moreWords) {
		addField(parent, property, fieldName, value, moreWords, null, -1);
	}
	
	private void addField(Composite parent, String property, String fieldName, String value, String moreWords, String description, int style) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(fieldName);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);
		
		if (style < 0) {
			style = SWT.SINGLE | SWT.BORDER;
		}
		
		Text input = new Text(parent, style);
		input.setText(value);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
		input.setLayoutData(gridData);
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(input);
		
		Label msgLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(msgLabel);
		if (!StringUtils.isEmpty(moreWords)) {
			msgLabel.setText(moreWords);
		}
		
		controls.put(property, input);
		
		addDescription(parent, description);
	}
	
	private void addDescription(Composite parent, String text) {
		if (StringUtils.isEmpty(text)) {
			return;
		}
		
		Label empty = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(empty);
		
		Label description = new Label(parent, SWT.WRAP);
		description.setText(text);
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).span(2, 0).applyTo(description);
		FontData fd = description.getFont().getFontData()[0];
		fd.height *= 0.9;
		description.setForeground(ColorCache.getColor(102, 102, 102));
		description.setFont(FontCache.getFont(fd));
		description.pack();
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		
		shell.setText("Preferences");
	}
	
	private void error(String message) {
		MessageBox messageBox = new MessageBox(getAppWindow().getShell(), SWT.APPLICATION_MODAL | SWT.OK | SWT.ICON_ERROR);
		messageBox.setMessage(message);
		messageBox.setText("Error!");
		
		messageBox.open();
	}
	
	@Override
	protected void okPressed() {
		if (!validateIntegerField("interval", false)) {
			error("The intervals should be an integer and should be greater than 0!");
			return;
		}
		
		if (!validateIntegerField("notificationDisplayTime", false)) {
			error("The notification display time should be an integer and should be greater than 0!");
			return;
		}
		
		if (!validateIntegerField("lengthOfNotifications", true)) {
			error("The total notifications field should be an integer.");
			return;
		}
		
		if (!validateIntegerField("statusWindow", true)) {
			error("The field 'Tray icon based on' should be an integer.");
			return;
		}
		
		for (String property : controls.keySet()) {
			Text text = controls.get(property);
			String value = text.getText();
			if ("host".equalsIgnoreCase(property)) {
				value = StringUtils.stripEnd(value, "/\\");
			} else if ("password".equalsIgnoreCase(property)) {
				value = EncryptUtils.encrypt(value);
			}
			
			monitor.settings.put(property, value);
		}
		
		monitor.settings.save(monitor.getSettingFile());
		
		setReturnCode(OK);
		close();
	}

	private boolean validateIntegerField(String key, boolean zeroValid) {
		String value = controls.get(key).getText();
		try {
			Integer i = Integer.valueOf(value);
			if (i == null) {
				return false;
			}

			return zeroValid ? i >= 0 : i > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected String getPreferenceName() {
		return "Preferences";
	}

	@Override
	protected String getDefaultSize() {
		return "650,445";
	}
}
