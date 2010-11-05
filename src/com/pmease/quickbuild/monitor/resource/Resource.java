/*
 * Copyright PMEase (c) 2005 - 2010,
 * Date: Jan 13, 2010 2:05:09 AM
 *
 * All rights reserved.
 * 
 * Revision: $Id$
 */
package com.pmease.quickbuild.monitor.resource;

import java.io.InputStream;

public final class Resource {
	public static InputStream getStream(String resourceName) {
		return Resource.class.getResourceAsStream(resourceName);
	}
}
