package csss2013.process;

import java.util.LinkedList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.Timeline;

import csss2013.App;
import csss2013.Process;
import csss2013.util.Tools;

public class AnalyzeDegree implements Process {
	public static final String DATA_DISTANCES = "process.analyzedegree.distances";
	public static final String SETTINGS_DISTANCES = "settings.process.analyze_degree.distances";

	public static class Distance {
		double value;
		LinkedList<double[]> series;

		public Distance(double d) {
			value = d;
			series = new LinkedList<double[]>();
		}
	}

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
		Timeline timeline = (Timeline) app.getData(Reload.TIMELINE_DATA_NAME);
		Graph g = new AdjacencyListGraph("tmp");

		String[] dstr = app.getProperty(SETTINGS_DISTANCES).split("\\s*,\\s*");
		Distance[] distances = new Distance[dstr.length];

		for (int i = 0; i < dstr.length; i++)
			distances[i] = new Distance(Double.parseDouble(dstr[i]));

		timeline.seekStart();
		timeline.addSink(g);

		while (timeline.hasNext()) {
			timeline.next();

			for (Distance d : distances) {
				double avg = averageDegree(g, d.value);
				d.series.add(new double[] { g.getStep(), avg });
			}
		}

		for (int i = 0; i < dstr.length; i++) {
			Distance d = distances[i];

			app.setData(DATA_DISTANCES + "." + dstr[i], d.series.toArray());
			app.setData(DATA_DISTANCES + "." + dstr[i] + ".title",
					"Distance max " + d.value + "m");
		}
	}

	protected double averageDegree(Graph g, double distance) {
		double[] ds = new double[g.getNodeCount()];
		double avg = 0;

		for (int i = 0; i < ds.length; i++)
			ds[i] = 0;

		for (int idx1 = 0; idx1 < g.getNodeCount() - 1; idx1++)
			for (int idx2 = idx1 + 1; idx2 < g.getNodeCount(); idx2++) {
				double d = Tools.distance(g.getNode(idx1), g.getNode(idx2));

				if (d <= distance) {
					ds[idx1]++;
					ds[idx2]++;
				}
			}

		for (int i = 0; i < ds.length; i++)
			avg += ds[i];

		return avg / ds.length;
	}

}
