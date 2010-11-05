/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 13, 2010 10:41:01 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.notifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.quickbuild.monitor.cache.ColorCache;
import com.pmease.quickbuild.monitor.cache.FontCache;
import com.pmease.quickbuild.monitor.cache.ImageCache;
import com.pmease.quickbuild.monitor.gui.TrayMonitor;
import com.pmease.quickbuild.monitor.resource.Resource;
import com.pmease.quickbuild.monitor.utils.SWTUtils;

public class Notifier {
	private static final Logger logger = LoggerFactory.getLogger(Notifier.class);
	
    // how long each tick is when fading in (in ms)
    private final int   FADE_TIMER    = 50;
    // how long each tick is when fading out (in ms)
    private final int   FADE_IN_STEP  = 30;
    // how many tick steps we use when fading out 
    private final int   FADE_OUT_STEP = 8;

    // how high the alpha value is when we have finished fading in 
    private final int   FINAL_ALPHA   = 225;

    // title foreground color
    private Color       titleFgColor = ColorCache.getColor(40, 73, 97);
    // text foreground color
    private Color       fgColor      = titleFgColor;

    // shell gradient background color - top
    private Color       bgFgGradient = ColorCache.getColor(51, 51, 51);
    // shell gradient background color - bottom    
    private Color       bgBgGradient = ColorCache.getColor(00, 00, 00);
    // shell border color
    private Color       borderColor  = ColorCache.getColor(40, 73, 97);

    // image used when drawing
    private Image       oldImage;

    private Shell       shell;

    public final TrayMonitor monitor;

    private final String title;
    private final String message;
    
    public enum NotificationType {
    	SUCCESS, WARNING, ERROR, FATAL
    }
    
    private final NotificationType notificationType;
    private WavePlayer player = null;
    
    private int index;
    
    public Notifier(TrayMonitor trayMonitor, String title, String message, NotificationType notificationType) {
    	this.monitor = trayMonitor;
    	this.title = title;
    	this.message = message;
    	this.notificationType = notificationType;
    	
    	try {
    		this.player = new WavePlayer();
    	} catch (NoClassDefFoundError e) {
    		// ignore
    		this.player = null;
    	}
    }
    
    private void createShell() {
    	shell = new Shell(monitor.getShell(), SWT.NO_FOCUS | SWT.ON_TOP | SWT.NO_TRIM);
    	shell.setLayout(new FillLayout());
    	shell.setForeground(fgColor);
    	shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
        
        shell.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event e) {
                try {
                    // get the size of the drawing area
                    Rectangle rect = shell.getClientArea();
                    // create a new image with that size
                    Image newImage = new Image(Display.getDefault(), Math.max(1, rect.width), rect.height);
                    // create a GC object we can use to draw with
                    GC gc = new GC(newImage);

                    // fill background
                    gc.setForeground(bgFgGradient);
                    gc.setBackground(bgBgGradient);
                    gc.fillGradientRectangle(rect.x, rect.y, rect.width, rect.height, true);

                    // draw shell edge
                    gc.setLineWidth(2);
                    gc.setForeground(borderColor);
                    gc.drawRectangle(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
                    // remember to dipose the GC object!
                    gc.dispose();

                    // now set the background image on the shell
                    shell.setBackgroundImage(newImage);

                    // remember/dispose old used iamge
                    if (oldImage != null) {
                        oldImage.dispose();
                    }
                    oldImage = newImage;
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

    	final Composite inner = new Composite(shell, SWT.NONE);
    	
    	GridLayout gl = new GridLayout(2, false);
        gl.marginLeft = 5;
        gl.marginTop = 0;
        gl.marginRight = 5;
        gl.marginBottom = 5;
        gl.numColumns = 3;
        
        inner.setLayout(gl);

        createContents(inner);
        
        if (monitor.getShell() == null || monitor.getShell().getMonitor() == null) { return; }

        monitor.windowManager.addNotifier(this);
        
        shell.setAlpha(0);
        shell.setVisible(true);
    }
    
    protected Composite createContents(Composite inner) {
        createTitleComponent(inner);
        createMessageComponent(inner);
    	
    	return inner;
    }
    
    protected Control createTitleComponent(Composite inner) {
    	CLabel imgLabel = new CLabel(inner, SWT.NONE);
//        imgLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING));
//        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 2).applyTo(imgLabel);
        imgLabel.setImage(ImageCache.getImage(notificationType.name().toLowerCase() + ".png"));
        
        GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING);
    	data.verticalSpan = 2;
    	data.horizontalSpan = 1;
    	imgLabel.setLayoutData(data);
    	
        CLabel titleLabel = new CLabel(inner, SWT.NONE);
//        titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
//        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(titleLabel);
        titleLabel.setText(title);
        Font f = titleLabel.getFont();
        FontData fd = f.getFontData()[0];
        fd.setStyle(SWT.BOLD);
        fd.height = 12;
        titleLabel.setFont(FontCache.getFont(fd));
        titleLabel.setForeground(ColorCache.getColor(255, 255, 255));

        data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
        data.horizontalSpan = 1;
        titleLabel.setLayoutData(data);

        CLabel closeLabel = new CLabel(inner, SWT.NONE);
        closeLabel.setImage(ImageCache.getImage("close.png"));
        data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = 1;
        closeLabel.setLayoutData(data);
        closeLabel.addMouseListener(new MouseAdapter() {
        	@Override
			public void mouseUp(MouseEvent e) {
        		Notifier.this.close();
        	}
        });
        
        return titleLabel;
    }
    
    protected Control createMessageComponent(Composite inner) {
    	try {
	    	Label messageLabel = new Label(inner, getLabelStyle());
	        
	        Font tf = messageLabel.getFont();
	        FontData tfd = tf.getFontData()[0];
	        tfd.setStyle(SWT.BOLD);
	        tfd.height = 11;
	        
	        messageLabel.setFont(FontCache.getFont(tfd));
	        
	        GridData gd = new GridData(GridData.FILL_BOTH);
	        gd.horizontalSpan = 2;
	        messageLabel.setLayoutData(gd);
	        
	        messageLabel.setText(message);
	    	messageLabel.setForeground(ColorCache.getColor(195, 195, 195));
	    	
	    	return messageLabel;
    	} catch (Exception e) {
    		logger.error("createMessageComponent failed!", e);
    		throw new RuntimeException(e);
    	}
    }
    
    protected int getLabelStyle() {
    	return SWT.WRAP;
    }
    
    public void open() {
    	try {
	    	if (shell == null || shell.isDisposed()) {
	    		createShell();
	    	}
	    	
	    	Boolean playSound = Boolean.valueOf(monitor.settings.getProperty("playSound", "true"));
	    	if (playSound && player != null) {
	    		if (notificationType == NotificationType.SUCCESS) {
	    			player.play(Resource.getStream("good.wav"));
	    		} else {
	    			player.play(Resource.getStream("bad.wav"));
	    		}
	    	}
	
	    	fadeIn(shell);
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    private void fadeIn(final Shell shell) {
        Runnable run = new Runnable() {
            public void run() {
                try {
                    if (shell == null || shell.isDisposed()) { return; }

                    int cur = shell.getAlpha();
                    
                    if (SWTUtils.hasAlphaSupport()) {
	                    cur += FADE_IN_STEP;
	
	                    if (cur > FINAL_ALPHA) {
	                    	cur = FINAL_ALPHA;
	                        startTimer(shell);
	                        return;
	                    }

	                    shell.setAlpha(cur);
	                    shell.getDisplay().timerExec(FADE_TIMER, this);
                    } else {
                    	shell.setAlpha(FINAL_ALPHA);
                        startTimer(shell);
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred!", e);
                }
            }

        };
        
        shell.getDisplay().timerExec(FADE_TIMER, run);
    }

    private void startTimer(final Shell shell) {
        Runnable run = new Runnable() {
            public void run() {
                try {
                    if (shell == null || shell.isDisposed()) { return; }

                    if (SWTUtils.hasAlphaSupport()) {
                    	fadeOut(shell);
                    } else {
                    	close();
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred!", e);
                }
            }

        };
        
        shell.getDisplay().timerExec(monitor.settings.getNotificationDisplayTime() * 1000, run);
    }

    private void fadeOut(final Shell shell) {
        final Runnable run = new Runnable() {

            public void run() {
                try {
                    if (shell == null || shell.isDisposed()) { return; }

                    int cur = shell.getAlpha();
                    cur -= FADE_OUT_STEP;

                    if (cur <= 0) {
                    	close();
                        return;
                    }

                    shell.setAlpha(cur);

                    shell.getDisplay().timerExec(FADE_TIMER, this);

                } catch (Exception e) {
                	logger.error("Exception occurred!", e);
                }
            }
        };
        
        shell.getDisplay().timerExec(FADE_TIMER, run);
    }
    
    public void close() {
    	shell.setAlpha(0);
    	if (oldImage != null) {
    		oldImage.dispose();
    	}
    	
    	if (player != null) {
    		player.close();
    	}
    	
    	shell.dispose();
    	monitor.windowManager.removeNotifier(this);
    }
    
	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Shell getShell() {
		return shell;
	}
}
