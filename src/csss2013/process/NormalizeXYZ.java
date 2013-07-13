/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013.process;

import org.graphstream.graph.Node;

import csss2013.App;
import csss2013.Process;
import csss2013.Trace;
import csss2013.annotation.Default;
import csss2013.annotation.Title;
import csss2013.util.Tools;

@Default
@Title("Normalize XYZ")
public class NormalizeXYZ implements Process {
	public static enum Type {
		NORMALIZE, PROJECTION
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#getPriority()
	 */
	public int getPriority() {
		return 100;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#process(csss2013.App)
	 */
	public void process(App app) {
		Type type = Type.PROJECTION;

		switch (type) {
		case NORMALIZE:
			normalize(app);
			break;
		default:
			projection(app);
			break;
		}
	}

	public void normalize(App app) {
		double x = 0, y = 0, n = 0;

		for (int i = 0; i < app.getTraceCount(); i++) {
			Trace t = app.getTrace(i);
			n += t.getNodeCount();

			for (int j = 0; j < t.getNodeCount(); j++) {
				double[] xyz = t.getNode(j).getAttribute("xyz");
				x += xyz[0];
				y += xyz[1];
			}
		}

		x /= n;
		y /= n;

		for (int i = 0; i < app.getTraceCount(); i++) {
			Trace t = app.getTrace(i);

			for (int j = 0; j < t.getNodeCount(); j++) {
				double[] xyz = t.getNode(j).getAttribute("xyz");

				xyz[0] -= x;
				xyz[1] -= y;

				t.getNode(j).setAttribute("xyz", xyz);
			}
		}
	}

	public void projection(App app) {
		double minLat, maxLat, minLon, maxLon;

		minLat = minLon = Double.MAX_VALUE;
		maxLat = maxLon = Double.MIN_VALUE;

		for (int i = 0; i < app.getTraceCount(); i++) {
			Trace t = app.getTrace(i);

			for (int j = 0; j < t.getNodeCount(); j++) {
				Node n = t.getNode(j);

				double lon, lat;
				lon = n.getNumber("lon");
				lat = n.getNumber("lat");

				minLat = Math.min(minLat, lat);
				maxLat = Math.max(maxLat, lat);
				minLon = Math.min(minLon, lon);
				maxLon = Math.max(maxLon, lon);
			}
		}

		double cLon, cLat;
		cLon = (minLon + maxLon) / 2.0;
		cLat = (minLat + maxLat) / 2.0;

		System.out.printf("Center (%f;%f)\n", cLon, cLat);
		for (int i = 0; i < app.getTraceCount(); i++) {
			Trace t = app.getTrace(i);

			for (int j = 0; j < t.getNodeCount(); j++) {
				Node n = t.getNode(j);

				double lon, lat;
				lon = n.getNumber("lon");
				lat = n.getNumber("lat");

				double x = Tools.distance(cLat, lon, cLat, cLon);
				double y = Tools.distance(lat, cLon, cLat, cLon);

				if (lon < cLon)
					x *= -1;

				if (lat < cLat)
					y *= -1;

				double[] xyz = n.getAttribute("xyz");
				xyz[0] = x;
				xyz[1] = y;
				xyz[2] = 0;

				n.setAttribute("xyz", xyz);
			}
		}
	}
}
