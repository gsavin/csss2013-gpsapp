package csss2013.util;

import java.util.Calendar;

import org.graphstream.graph.Node;

public class Tools {
	/*
	 * From http://www.anddev.org/viewtopic.php?p=20195#20195
	 */
	public static double distance(Node n1, Node n2) {
		double lat1 = n1.getNumber("lat"), lat2 = n2.getNumber("lat"), lon1 = n1
				.getNumber("lon"), lon2 = n2.getNumber("lon");

		double pk = (double) (180 / Math.PI);

		double a1 = lat1 / pk;
		double a2 = lon1 / pk;
		double b1 = lat2 / pk;
		double b2 = lon2 / pk;

		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);

		return 6366000 * tt;
	}

	public static synchronized Calendar getCalendarTime(Node n) {
		return n.getAttribute("time.calendar");
	}

	public static long getTime(Node n) {
		return n.getAttribute("time.ms");
	}
}
