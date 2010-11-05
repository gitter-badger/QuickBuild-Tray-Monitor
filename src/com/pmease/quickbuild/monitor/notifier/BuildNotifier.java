/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 16, 2010 11:58:35 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.notifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.pmease.quickbuild.monitor.cache.ColorCache;
import com.pmease.quickbuild.monitor.cache.FontCache;
import com.pmease.quickbuild.monitor.gui.TrayMonitor;

public class BuildNotifier extends Notifier {
	
	private final BuildFragment build;
	
	public BuildNotifier(TrayMonitor trayMonitor, BuildFragment build) {
		super(trayMonitor, build.getNotifierTitle(), build.getNotifierMessage(), build.getNotificationType());
		
		this.build = build;
	}
	
	@Override
	protected Control createMessageComponent(Composite inner) {
		StyledText text = new StyledText(inner, getLabelStyle());
		String message = getMessage();
		text.setText(message);
		
		StyleRange range = new StyleRange();
		range.start = 0;
		range.length = message.length();
		range.underline = true;
		range.foreground = ColorCache.getColor(195, 195, 195);
		
		Font f = text.getFont();
		
        FontData fd = f.getFontData()[0];
        fd.setStyle(SWT.BOLD);
        fd.height = 12;
        range.font = FontCache.getFont(fd);
        
        text.setStyleRange(range);
        
        text.setCursor(new Cursor(monitor.getDisplay(), SWT.CURSOR_HAND));
		
        text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				Program.launch(monitor.settings.getHost() + build.getUrl());
			}
		});

        GridData data = new GridData(GridData.FILL_BOTH);
    	data.verticalSpan = 1;
    	data.horizontalSpan = 2;
    	text.setLayoutData(data);
		
		return text;
	}

	
	@Override
	protected int getLabelStyle() {
		return SWT.WRAP | SWT.UNDERLINE_SINGLE;
	}
}
