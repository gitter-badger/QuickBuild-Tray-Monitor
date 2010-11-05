package com.pmease.quickbuild.monitor.gui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.pmease.quickbuild.monitor.Version;
import com.pmease.quickbuild.monitor.cache.FontCache;
import com.pmease.quickbuild.monitor.cache.ImageCache;

public class AboutDialog extends AbstractDialog {

	public AboutDialog(TrayMonitor monitor) {
		super(monitor);
		
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.NONE);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		
		shell.setText("About TrayMonitor");
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
        /* Create OK button */
        Button okButton = this.createButton(parent, IDialogConstants.OK_ID,
                "OK", true);
        okButton.forceFocus();
        this.setButtonLayoutData(okButton);        
    } // end method createButtonsForButtonBar(Composite)
    
	@Override
	protected Control createDialogArea(Composite container) {
		Composite parent = (Composite) super.createDialogArea(container);
		
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 5;
		layout.marginWidth = 0;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label logo = new Label(parent, SWT.NONE);
		logo.setImage(ImageCache.getImage("logo.png"));
		GridData data = this.controlLayoutData();
		data.minimumHeight = 55;
		data.verticalAlignment = SWT.FILL;
		logo.setLayoutData(data);
		
		CLabel infoLabel = createLabel(parent, "TrayMonitor");
		data = this.controlLayoutData();
		infoLabel.setLayoutData(data);
		
		Font font = infoLabel.getFont();
		FontData fd = font.getFontData()[0];
		fd.height *= 1.2;
		fd.setStyle(SWT.BOLD);
		infoLabel.setFont(FontCache.getFont(fd));
		
		CLabel versionLabel = createLabel(parent, "Version: " + Version.getVersion());       
		font = versionLabel.getFont();
		fd = font.getFontData()[0];
		fd.height = 10;
		font = FontCache.getFont(fd);
		versionLabel.setFont(font);
		
		CLabel copy1 = createLabel(parent, "Copyright 2004-2010 PMEase Inc.");
		copy1.setFont(font);
		
		CLabel copy2 = createLabel(parent, "All rights reserved.");
		copy2.setFont(font);
		
		Link thanks = new Link(parent, SWT.NONE);
		thanks.setText("(The logo is designed by <A href=\"http://openclipart.org\">http://openclipart.org</A>)");
//		CLabel thanks = createLabel(parent, "(The logo is designed by http://openclipart.org)");
		thanks.setLayoutData(controlLayoutData());
		thanks.setFont(font);
		thanks.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event event) {
		    	  Program.launch(event.text);
		      }
	    });
		
		return parent;
	}
	
	private CLabel createLabel(Composite parent, String text) {
		CLabel label = new CLabel(parent, SWT.NONE);
		label.setText(text);
		label.setLayoutData(controlLayoutData());
		
		return label;
	}
	
	@Override
	protected String getDefaultSize() {
		return "360,290";
	}

	@Override
	protected String getPreferenceName() {
		return "About";
	}
}
