/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 9, 2010 10:42:18 PM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.cache;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.pmease.quickbuild.monitor.resource.Resource;

public class ImageCache {
	private final static ImageRegistry imageRegistry = new ImageRegistry(Display.getDefault());
	
	public static void dispose() {
		imageRegistry.dispose();
	}
	
	public static Image getImage(String key) {
		Image image = imageRegistry.get(key);
		
		if (image == null) {
			image = createImage(key);
			
			imageRegistry.put(key, image);
		}
		
		return image;
	}
	
	public static ImageDescriptor getImageDescriptor(String key) {
		return ImageDescriptor.createFromImage(getImage(key));
	}
	
	private static Image createImage(String key) {
		InputStream in = Resource.class.getResourceAsStream(key);
		try {
			return new Image(Display.getDefault(), in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
