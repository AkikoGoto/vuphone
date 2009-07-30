/******************************************************************************
 * Copyright (c) 2007 Jules White.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jules White - initial API and implementation 
 ****************************************************************************/
package org.vuphone.assassins.landmineadd;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.vuphone.assassins.notification.InvalidFormatException;

/**
 * This class is used to convert generic notification objects into
 * LandMineNotification objects.
 * 
 * @author jules
 * 
 */
public class LandMineAddParser {
	private Logger log_ = Logger.getLogger(LandMineAddParser.class.getName());

	public LandMineAddNotification getLandMine(HttpServletRequest request)
			throws InvalidFormatException {

		// Create an AccidentNotification
		LandMineAddNotification lmn = null;
		try {
			log_.log(Level.FINE, "Processing land mine notification");
			lmn = new LandMineAddNotification();
			lmn.setLatitude(Double.parseDouble(request.getParameter("lat")));
			log_.log(Level.FINER, "Latitude: " + lmn.getLatitude());
			lmn.setLongitude(Double.parseDouble(request.getParameter("lon")));
			log_.log(Level.FINER, "Longitude: " + lmn.getLongitude());
			lmn.setRadius(Double.parseDouble(request.getParameter("radius")));
			log_.log(Level.FINER, "Radius: " + lmn.getRadius());
			lmn.setRequest(request);
			log_.log(Level.FINEST, "Request: " + request.toString());
		} catch (Exception e) {
			e.printStackTrace();

			// If one of the parameters is invalid, or is not present
			throw new InvalidFormatException();
		}

		return lmn;

	}
}