package csss2013.util;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import org.graphstream.util.time.ISODateIO;

public class DataGenerator {
	String prefix;
	double[][] bouees;
	Random random;
	int waypoints;
	long stepTime;
	Calendar refDate;

	public DataGenerator(String prefix, int boueesCount, int tracesCount,
			int waypoints, long stepTime) {
		this.prefix = prefix;
		this.waypoints = waypoints;
		this.stepTime = stepTime;

		bouees = new double[boueesCount][2];
		random = new Random();
		refDate = Calendar.getInstance();
	}

	public void generate() throws IOException {
		initBouees();
	}

	protected void initBouees() throws IOException {
		for (int i = 0; i < bouees.length; i++) {
			double angle = i * (2 * Math.PI) / bouees.length;
			bouees[i][0] = 100 * Math.sin(angle);
			bouees[i][1] = 100 * Math.cos(angle);

			shake(bouees[i], 20);

			System.err.printf("bouee#%d @ (%f;%f)\n", i, bouees[i][0],
					bouees[i][1]);

			String filename = String.format("%s_bouee_%d.gpx", prefix, i);
			PrintStream out = new PrintStream(filename);

			traceBouee(i, out);
			out.close();
		}
	}

	protected void traceBouee(int idx, PrintStream out) {
		Calendar date = (Calendar) refDate.clone();

		printGPXHeader("bouee#" + idx, out);

		out.printf("    <trkseg>\n");
		for (int i = 0; i < waypoints; i++) {
			shake(bouees[idx], 2);

			out.printf("      <trkpt ");
			out.printf(Locale.ROOT, "lat=\"%f\" ", bouees[idx][0]);
			out.printf(Locale.ROOT, "lon=\"%f\">\n", bouees[idx][1]);
			out.printf("        <ele>0</ele>\n");
			out.printf("        <time>%s</time>\n", format(date));
			out.printf("      </trkpt>\n");

			date.add(Calendar.MILLISECOND, (int) stepTime);
		}

		out.printf("    </trkseg>\n");
		printGPXFooter(out);
	}

	protected void printGPXHeader(String name, PrintStream out) {
		out.printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		out.printf("<gpx version=\"1.1\" creator=\"GraphStream\">\n");
		out.printf("  <metadata>\n");
		out.printf("    <name><![CDATA[%s]]></name>\n", name);
		out.printf("    <desc><![CDATA[]]></desc>\n");
		out.printf("  </metadata>\n");
		out.printf("  <trk>\n");
		out.printf("    <name><![CDATA[%s]]></name>\n", name);
		out.printf("    <desc><![CDATA[]]></desc>\n");
		out.printf("    <type><![CDATA[]]></type>\n");
	}

	protected void printGPXFooter(PrintStream out) {
		out.printf("  </trk>\n");
		out.printf("</gpx>\n");
	}

	protected String format(Calendar date) {
		ISODateIO dateScanner = null;

		try {
			dateScanner = new ISODateIO("%FT%T.%k%z");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return dateScanner.toString(date);
	}

	protected void shake(double[] xy, double strength) {
		xy[0] += (2 * random.nextDouble() - 1) * strength;
		xy[1] += (2 * random.nextDouble() - 1) * strength;
	}

	public static void main(String... args) {
		DataGenerator gen = new DataGenerator("data/sim_", 4, 7, 10, 1000);

		try {
			gen.generate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
