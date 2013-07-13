package csss2013.process;

import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Node;

import csss2013.App;
import csss2013.Process;
import csss2013.Trace;
import csss2013.annotation.Title;
import csss2013.util.Tools;

@Title("Speed")
public class ExampleProcess implements Process {
	//
	// Defines data name :
	//
	public static final String DISTANCE_DATA_NAME = "process.example.distance";
	public static final String TIME_DATA_NAME = "process.example.time";
	public static final String SPEED_DATA_NAME = "process.example.speed";
	public static final String SPEED_SERIES_DATA_NAME = "process.example.speed.series";

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#getPriority()
	 */
	public int getPriority() {
		return 50;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#process(csss2013.App)
	 */
	public void process(App app) {
		//
		// We try to compute a basic data, like the average speed, for each
		// trace :
		//
		double distance = 0, time = 0;

		double currentDistance = 0;
		double currentTime = 0;

		LinkedList<double[]> series = new LinkedList<double[]>();

		for (int idx = 0; idx < app.getTraceCount(); idx++) {
			Trace trace = app.getTrace(idx);

			//
			// We start with the initial node
			//
			Iterator<Node> ite = trace.getTracePath();
			Node current = ite.next(), next = null;

			//
			// Then we compute distance and time between each peer of nodes
			//
			while (ite.hasNext()) {
				next = ite.next();

				long t1 = Tools.getTime(current);
				long t2 = Tools.getTime(next);
				double d = Tools.distance(current, next);

				if (Double.isNaN(d)) {
					System.err.printf(
							"[Trace|%s] distance is NaN between %s and %s\n",
							trace.getId(), Tools.getCalendarTime(current)
									.getTime(), Tools.getCalendarTime(next)
									.getTime());
					continue;
				}

				distance += d;
				currentDistance += d;
				time += (t2 - t1) / 1000.0;
				currentTime += (t2 - t1) / 1000.0;

				if (currentDistance > 50) {
					double[] e = { time,
							(currentDistance / 1000.0) / (currentTime / 3600.0) };

					series.add(e);

					currentDistance = 0;
					currentTime = 0;
				}

				current = next;
			}

			double speed = (distance / 1000.0) / (time / 3600.0);

			app.setData(DISTANCE_DATA_NAME + trace.getId(), distance);
			app.setData(TIME_DATA_NAME + trace.getId(), time);
			app.setData(SPEED_DATA_NAME + trace.getId(), speed);
			app.setData(SPEED_SERIES_DATA_NAME + trace.getId(),
					series.toArray());
			app.setData(SPEED_SERIES_DATA_NAME + trace.getId() + ".title",
					trace.getId() + " speed");
		}
	}
}
