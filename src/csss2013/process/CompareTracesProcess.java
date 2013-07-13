package csss2013.process;

import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Node;
import org.graphstream.stream.Sink;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.stream.Timeline;

import csss2013.App;
import csss2013.Process;
import csss2013.Trace;
import csss2013.annotation.Title;
import csss2013.util.Tools;

@Title("CompareTraces")
public class CompareTracesProcess extends SinkAdapter implements Process {
	public static final String COMPARE_DISTANCE_DATA_NAME = "process.compareTraces.distance";
	public static final String COMPARE_DISTANCE_SERIES_DATA_NAME = "process.compareTraces.distance.series";

	public int getPriority() {
		return 10;
	}

	protected int idx = 0;

	protected long firstStep = 0L;

	protected String node0Id = "";

	protected String node1Id = "";

	protected double[] node0 = new double[2];

	protected double[] node1 = new double[2];

	protected long currentTime = 0L;

	protected LinkedList<double[]> series = new LinkedList<double[]>();

	public void process(App app) {
		Timeline timeline = (Timeline) app.getData(Reload.TIMELINE_DATA_NAME);

		if(app.getTraceCount() < 2)
		 	App.error("We need exactly two traces");

		timeline.seekStart();
		timeline.addSink(this);

		int i = 0;
		double avg = 0;

		while(timeline.hasNext()) {
			timeline.next();
			double e[] = new double[2];
			e[0] = currentTime;
			e[1] = distance(node0, node1);
			avg += e[1];
			series.add(e);

			i += 1;
		}

		avg /= i;

		timeline.removeSink(this);

		app.setData(COMPARE_DISTANCE_DATA_NAME, avg);
		app.setData(COMPARE_DISTANCE_SERIES_DATA_NAME, series.toArray());
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		if(idx == 0) {
			node0Id = nodeId;
			idx += 1;
		} else if(idx == 1) {
			node1Id = nodeId;
			idx += 1;
		} else {
			App.error("WTF ?");
		}
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		handleLatLon(nodeId, attribute, value);
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		handleLatLon(nodeId, attribute, newValue);
	}

	public void stepBegins(String sourceId, long timeId, double step) {
		if(firstStep == 0L) firstStep = ((long) (step/1000.0));
		currentTime = ((long) (step/1000.0)) - firstStep;
	}

	protected void handleLatLon(String nodeId, String attribute, Object value) {
		if(attribute.equals("lat")) {
			node(nodeId)[0] = (Double)value;
		} else if(attribute.equals("lon")) {
			node(nodeId)[1] = (Double)value;
		}
	}

	protected double[] node(String nodeId) {
		if(nodeId.equals(node0Id)) return node0;
		return node1;
	}

	public static double distance(double[] n1, double[] n2) {
		double pk = (double) (180 / Math.PI);

		double a1 = n1[0] / pk;
		double a2 = n1[1] / pk;
		double b1 = n2[0] / pk;
		double b2 = n2[1] / pk;

		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);

		return 6366000 * tt;
	}

}