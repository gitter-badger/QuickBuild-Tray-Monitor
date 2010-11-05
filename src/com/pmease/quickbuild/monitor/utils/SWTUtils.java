/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 9, 2010 1:43:38 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowAdapter;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class SWTUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private SWTUtils() {
		
	}

	public static DateFormat getDateFormat() {
		return new SimpleDateFormat(DATE_FORMAT);
	}
	
	public static DateFormat getISO8601Format() {
		return new SimpleDateFormat(ISO8601_FORMAT);
	}
	
	public static String pointToString(Point point) {
		return point.x + "," + point.y;
	}
	
	public static Point stringToPoint(String str) {
		if (str == null) {
			return null;
		}
		
		String[] parts = str.split(",");
		
		int x = Integer.valueOf(parts[0]);
		int y = Integer.valueOf(parts[1]);
		
		return new Point(x, y);
	}
	
	public static String formatDuration(long duration) {
		if (duration == 0) {
			return "0 milisecond";
		} else if (duration < 1000) {
			return duration + " miliseconds";
		} else {
			return DurationFormatUtils.formatDurationWords(duration, true, true);
		}
	}
	
	/**
     * <p>
     * Returns <code>true</code> if the specified <code>Widget</code> is
     * disposed.
     * A <code>Widget</code> is considered as disposed if it is either
     * <code>null</code> or its {@link org.eclipse.swt.widgets.Widget#isDisposed()} 
     * method returns <code>true</code>.
     * </p>
     * 
     * @param widget the <code>Widget</code> to verify for disposal
     * @return <code>true</code> if the specified <code>Widget</code> is disposed;
     * <code>false</code> otherwise
     */
    public static boolean isDisposed(Widget widget) {
        return (widget == null || widget.isDisposed());
    }
    
    
    /**
     * <p>
     * Centers the specified <code>Shell</code> on the given
     * <code>Display</code>.
     * </p>
     * 
     * @param display the <code>Display</code> to center the 
     * <code>Shell</code> on
     * @param shell the <code>Shell</code> to center
     */
    public static void centerShell(Display display, Shell shell) {
        Rectangle displayBounds = display.getPrimaryMonitor().getBounds();
		Rectangle shellBounds = shell.getBounds();
		int x = displayBounds.x + 
			(displayBounds.width - shellBounds.width) >> 1;
		int y = displayBounds.y + 
			(displayBounds.height - shellBounds.height) >> 1;
		shell.setLocation(x, y);
    }
    
    public static Browser createBrowser(final Shell shell) {
    	final Browser browser;
    	shell.setLayout(new FillLayout());
    	
    	try {
    		browser = new Browser(shell, SWT.NONE);
    	} catch (SWTError e) {
    		throw new RuntimeException(e);
    	}
    	
    	initBrowser(shell, browser);
    	
    	return browser;
    }
    
    private static final Browser externalBrowser = new Browser(new Shell(Display.getDefault(), SWT.NONE), SWT.NONE);
    
    static {
    	externalBrowser.getShell().setVisible(false);
    }
    
    public static void initBrowser(final Shell shell, Browser browser) {
    	browser.addOpenWindowListener(new OpenWindowListener() {
    		public void open(WindowEvent event) {
    			final Shell newShell = new Shell(shell);
    			final Browser browser = new Browser(newShell, SWT.NONE);
    			
    			browser.addLocationListener(new LocationAdapter() {
    				@Override
					public void changing(final LocationEvent e) {
    					e.doit = false;
    					Program.launch(e.location);
    				}
    			});
    			
    			newShell.setVisible(false);
    			
    			event.browser = browser;
    		}
    	});
    	
    	browser.addTitleListener(new TitleListener() {
			public void changed(TitleEvent event) {
				shell.setText(event.title);
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
    	
    	browser.addCloseWindowListener(new CloseWindowListener() {
    		public void close(WindowEvent event) {
    			Browser browser = (Browser)event.widget;
    			Shell shell = browser.getShell();
    			shell.close();
    		}
    	});
    }
    
    /**
     * <p>
     * Creates a new <code>GridLayout</code> using the specified settings.
     * </p>
     * 
     * @param columns the number of cell columns
     * @param makeColumnsEqualWidth <code>true</code> to force all columns to
     * have the same width; <code>false</code> if not to force sam width
     * @param marginHeight the number of pixels of vertical margin
     * that will be placed along the top and bottom edges of the layout
     * @param marginWidth the number of pixels of horizontal margin
     * that will be placed along the left and right edges of the layout
     * @param marginTop the number of pixels of vertical margin
     * that will be placed along the top edge of the layout
     * @param marginBottom the number of pixels of vertical margin
     * that will be placed along the bottom edge of the layout
     * @param marginLeft the number of pixels of horizontal margin
     * that will be placed along the left edge of the layout
     * @param marginRight the number of pixels of horizontal margin
     * that will be placed along the right edge of the layout
     * @param verticalSpacing the number of pixels between the bottom
     * edge of one cell and the top edge of its neighbouring cell underneath
     * @param horizontalSpacing the number of pixels between the right
     * edge of one cell and the left edge of its neighbouring cell to
     * the right
     * 
     * @return a <code>GridLayout</code> instance according to the
     * specified settings
     */
    public static GridLayout createGridLayout(int columns, 
            boolean makeColumnsEqualWidth,
            int marginHeight,
            int marginWidth,
            int marginTop,
            int marginBottom,
            int marginLeft,
            int marginRight,
            int verticalSpacing,
            int horizontalSpacing) {
        GridLayout layout = new GridLayout();
        layout.numColumns = columns;
        layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
        layout.marginHeight = marginHeight;
        layout.marginWidth = marginWidth;
        layout.marginTop = marginTop;
        layout.marginBottom = marginBottom;
        layout.marginLeft = marginLeft;
        layout.marginRight = marginRight;
        layout.verticalSpacing = verticalSpacing;
        layout.horizontalSpacing = horizontalSpacing;  
        
        return (layout);
    } // end method createGridLayout(int, boolean, int, int, int, int, int, int, int, int)
    
    
    /**
     * <p>
     * Creates a new <code>GridData</code> object using the specified 
     * settings.
     * </p>
     * 
     * @param horizontalAlignment specifies how controls will be positioned 
     * horizontally within a cell. 
     *
     * Possible values are: <ul>
     * <li>SWT.BEGINNING (or SWT.LEFT): Position the control at the left of the cell</li>
     * <li>SWT.CENTER: Position the control in the horizontal center of the cell</li>
     * <li>SWT.END (or SWT.RIGHT): Position the control at the right of the cell</li>
     * <li>SWT.FILL: Resize the control to fill the cell horizontally</li>
     * </ul>
     * @param verticalAlignment specifies how controls will be positioned 
     * vertically within a cell. 
     *
     * Possible values are: <ul>
     * <li>SWT.BEGINNING (or SWT.TOP): Position the control at the top of the cell</li>
     * <li>SWT.CENTER: Position the control in the vertical center of the cell</li>
     * <li>SWT.END (or SWT.BOTTOM): Position the control at the bottom of the cell</li>
     * <li>SWT.FILL: Resize the control to fill the cell vertically</li>
     * </ul>
     * @param grabExcessHorizontalSpace whether cell will be made wide enough to fit the remaining horizontal space
     * @param grabExcessVerticalSpace whether cell will be made high enough to fit the remaining vertical space
     * @param horizontalSpan the number of column cells that the control
     * will take up
     * @param verticalSpan the number of row cells that the control
     * will take up
     * @param heightHint the preferred height in pixels. This value
     * is the hHint passed into Control.computeSize(int, int, boolean) 
     * to determine the preferred size of the control
     * @param widthHint the preferred width in pixels. This value
     * is the wHint passed into Control.computeSize(int, int, boolean) 
     * to determine the preferred size of the control
     * 
     * @return a new <code>GridData</code> object according to the 
     * specified settings
     */
    public static GridData createGridData(int horizontalAlignment,
            int verticalAlignment,
            boolean grabExcessHorizontalSpace,
            boolean grabExcessVerticalSpace,
            int horizontalSpan,
            int verticalSpan,
            int heightHint,
            int widthHint) {
        GridData data = new GridData();
        data.horizontalAlignment = horizontalAlignment;
        data.verticalAlignment = verticalAlignment;
        data.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        data.grabExcessVerticalSpace = grabExcessVerticalSpace;
        data.horizontalSpan = horizontalSpan;
        data.verticalSpan = verticalSpan;
        data.heightHint = heightHint;
        data.widthHint = widthHint;
        
        return (data);
    } // end method createGridData(int, int, boolean, boolean, int, int, int, int)
    
    
    /**
     * <p>
     * Creates a new <code>FillLayout</code> using the specified settings.
     * </p>
     * 
     * @param type specifies how controls will be positioned 
     * within the layout.
     *
     * Possible values are: 
     * <ul>
     * <li>HORIZONTAL: Position the controls horizontally from left to 
     * right</li>
     * <li>VERTICAL: Position the controls vertically from top to bottom</li>
     * </ul>
     * @param marginHeight the number of pixels of vertical margin
     * that will be placed along the top and bottom edges of the layout
     * @param marginWidth the number of pixels of horizontal margin
     * that will be placed along the left and right edges of the layout
     * @param spacing the number of pixels between the edge of one cell
     * and the edge of its neighbouring cell
     * 
     * @return a <code>FillLayout</code> instance according to the
     * specified settings
     */
    public static FillLayout createFillLayout(int type, 
            int marginHeight, 
            int marginWidth,
            int spacing) {
        FillLayout layout = new FillLayout();
        layout.type = type;
        layout.marginHeight = marginHeight;
        layout.marginWidth = marginWidth;
        layout.spacing = spacing;
        
        return (layout);
    } // end method createFillLayout(int, int, int, int)
    
    public static boolean hasAlphaSupport() {
    	String platform = SWT.getPlatform();
    	
    	if (platform.startsWith("win") || platform.equalsIgnoreCase("carbon") || platform.equalsIgnoreCase("cocoa")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    private static final String OS = System.getProperty("os.name");
    
    public static boolean mac() {
    	return OS.toLowerCase().contains("mac");
    }
    
    public static boolean windows() {
    	return OS.toLowerCase().contains("windows");
    }
    
    public static boolean linux() {
    	return OS.toLowerCase().contains("linux");
    }
}
