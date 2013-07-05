package csss2013.process;

import java.util.Iterator;

import org.graphstream.graph.Node;

import csss2013.App;
import csss2013.Process;
import csss2013.Trace;
import csss2013.annotation.Title;
import csss2013.util.Tools;

@Title("My Crazy Process")
public class ExampleProcess implements Process {
	//
	// Defines data name :
	//
	public static final String DISTANCE_DATA_NAME = "process.example.distance";
	public static final String TIME_DATA_NAME = "process.example.time";
	public static final String SPEED_DATA_NAME = "process.example.speed";

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
				
				distance += Tools.distance(current, next);
				time += (Tools.getTime(next) - Tools.getTime(current)) / 1000.0;

				current = next;
			}

			double speed = (distance / 1000.0) / (time / 3600.0);

			app.setData(DISTANCE_DATA_NAME, distance);
			app.setData(TIME_DATA_NAME, time);
			app.setData(SPEED_DATA_NAME, speed);
		}
	}
}
