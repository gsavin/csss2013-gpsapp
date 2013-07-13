package csss2013.process;

import java.util.HashMap;
import java.util.LinkedList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.Timeline;

import csss2013.App;
import csss2013.Process;
import csss2013.annotation.Title;
import csss2013.util.Tools;

@Title("CompareTraces")
public class Compare implements Process {
	public static final String COMPARE_DISTANCE_DATA_NAME = "process.compareTraces.distance";
	public static final String COMPARE_DISTANCE_SERIES_DATA_NAME = "process.compareTraces.distance.series";

	public int getPriority() {
		return 10;
	}

	class Comparaison {

		protected String node0Id = "";
		protected String node1Id = "";

		protected double[] node0 = new double[2];
		protected double[] node1 = new double[2];

		double avg = 0;

		int i = 0;

		protected LinkedList<double[]> series = new LinkedList<double[]>();

		public Comparaison(String node0, String node1) {
			this.node0Id = node0;
			this.node1Id = node1;
			series = new LinkedList<double[]>();
		}
	}

	protected int idx = 0;
	protected double firstStep = 0L, currentTime = 0L;

	HashMap<String, Comparaison> comparaison;

	public Compare() {
		comparaison = new HashMap<String, Comparaison>();
	}

	public void process(App app) {
		Timeline timeline = (Timeline) app.getData(Reload.TIMELINE_DATA_NAME);

		if (app.getTraceCount() < 2)
			App.error("We need exactly two traces");

		String[] cs = app.getProperty("settings.process.compare.what").split(
				"\\s*,\\s*");

		for (String c : cs) {
			if (c.length() == 0)
				continue;

			String n0 = app.getProperty("settings.process.compare." + c
					+ ".node0");
			String n1 = app.getProperty("settings.process.compare." + c
					+ ".node1");

			comparaison.put(c, new Comparaison(n0, n1));
		}

		Graph g = new AdjacencyListGraph("tmp");

		timeline.seekStart();
		timeline.addSink(g);

		firstStep = Double.NaN;

		while (timeline.hasNext()) {
			timeline.next();

			if (Double.isNaN(firstStep))
				firstStep = g.getStep();

			currentTime = g.getStep() - firstStep;

			for (Comparaison c : comparaison.values()) {
				Node n0 = g.getNode(c.node0Id);
				Node n1 = g.getNode(c.node1Id);

				if (n0 == null || n1 == null)
					continue;

				double e[] = new double[2];

				e[0] = currentTime;
				e[1] = Tools.distance(n0.getNumber("lat"), n0.getNumber("lon"),
						n1.getNumber("lat"), n1.getNumber("lon"));

				if (!Double.isNaN(e[1])) {
					c.avg += e[1];
					c.series.add(e);
					c.i += 1;
				}
			}
		}

		timeline.removeSink(g);

		for (String cn : comparaison.keySet()) {
			Comparaison c = comparaison.get(cn);

			c.avg /= c.i;

			app.setData(COMPARE_DISTANCE_DATA_NAME + "." + cn, c.avg);
			app.setData(COMPARE_DISTANCE_SERIES_DATA_NAME + "." + cn,
					c.series.toArray());
		}
	}
}