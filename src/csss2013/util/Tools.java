package csss2013.util;

import java.util.Calendar;
import java.util.Locale;

import org.graphstream.graph.Node;

public class Tools {
	/*
	 * From http://www.anddev.org/viewtopic.php?p=20195#20195
	 */
	public static double distance(Node n1, Node n2) {
		double lat1 = n1.getNumber("lat"), lat2 = n2.getNumber("lat"), lon1 = n1
				.getNumber("lon"), lon2 = n2.getNumber("lon");

		return distance(lat1, lon1, lat2, lon2);
	}

	public static double distance(double lat1, double lon1, double lat2,
			double lon2) {
		return 1852.0 * orthodromie(lon1, lat1, lon2, lat2);
	}

	public static double distance__(double lat1, double lon1, double lat2,
			double lon2) {
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

	public static double orthodromie(double lat1, double lon1, double lat2,
			double lon2) {
		lon1 = Math.toRadians(lon1);
		lon2 = Math.toRadians(lon2);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double d = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
				* Math.cos(lat2) * Math.cos(lon2 - lon1);
		d = 60 * Math.toDegrees(Math.acos(d));

		return d;
	}

	public static synchronized Calendar getCalendarTime(Node n) {
		return n.getAttribute("time.calendar");
	}

	public static long getTime(Node n) {
		return n.getAttribute("time.ms");
	}

	public static void main(String... args) {
		// (48°51' N, 2°21' E)--(40°43'N, 74°00'W) : 5830 km.
		double parisLat = 48.856578;
		double parisLon = 2.351828;
		double nyLat = 40.713361;
		double nyLon = -74.005594;
		double d = distance__(parisLat, parisLon, nyLat, nyLon);
		double o = distance(parisLat, parisLon, nyLat, nyLon);
		System.out.printf(Locale.ROOT, "%.2f |%.2f\n", o, d);
	}
}
