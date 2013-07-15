package csss2013.process;

import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Node;

import csss2013.App;
import csss2013.Process;
import csss2013.Trace;
import csss2013.util.Tools;

public class DirectionVariation implements Process {
	public static final String DATA_DV_SERIES = "process.directionvariation.series";

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
		for (int idx = 0; idx < app.getTraceCount(); idx++) {
			Trace t = app.getTrace(idx);
			Iterator<Node> it = t.getTracePath();
			Node current;
			LinkedList<double[]> data = new LinkedList<double[]>();
			double initialTime = Double.NaN;

			while (it.hasNext()) {
				current = it.next();

				if (Double.isNaN(initialTime))
					initialTime = Tools.getTime(current) / 1000;

				data.add(new double[] {
						Tools.getTime(current) / 1000 - initialTime,
						getVariation(t, current, 10000) });
			}

			findOptimum(data);

			app.setData(DATA_DV_SERIES + "." + t.getId(), data.toArray());
			app.setData(DATA_DV_SERIES + "." + t.getId() + ".title", t.getId());
		}
	}

	protected double[] findOptimum(LinkedList<double[]> data) {
		LinkedList<Double> optimum = new LinkedList<Double>();
		double threhold = 0.15;

		for (int i = 2; i < data.size() - 2; i++) {
			double[] e = data.get(i);
			double d = (e[1] / 360) * (e[1] / 360);

			if (d < threhold)
				continue;

			double d0 = data.get(i - 2)[1] / 360.0;
			d0 = d0 * d0;
			double d1 = data.get(i - 1)[1] / 360.0;
			d1 = d1 * d1;
			double d2 = data.get(i + 1)[1] / 360.0;
			d2 = d2 * d2;
			double d3 = data.get(i + 2)[1] / 360.0;
			d3 = d3 * d3;

			if (d0 < d && d1 < d && d2 < d && d3 < d) {
				System.out.printf("Optimum @ %f\n", e[0]);
				optimum.add(e[0]);
			}
		}

		double[] r = new double[optimum.size()];
		for (int i = 0; i < optimum.size(); i++)
			r[i] = optimum.get(i);

		return r;
	}

	protected double getVariation(Trace t, Node n, double delta) {
		double variation = 0;
		double prev = 0;
		Node c = n;

		LinkedList<Node> nodes = new LinkedList<Node>();
		nodes.add(n);

		while (c.getInDegree() > 0
				&& Tools.getTime(n) - Tools.getTime(c) < delta) {
			c = c.getEnteringEdge(0).getOpposite(c);
			nodes.addFirst(c);
		}

		c = n;

		while (c.getOutDegree() > 0
				&& Tools.getTime(c) - Tools.getTime(n) < delta) {
			c = c.getLeavingEdge(0).getOpposite(c);
			nodes.addLast(c);
		}

		for (int i = 0; i < nodes.size() - 1; i++) {
			double[] xy1 = nodes.get(i).getAttribute("xyz");
			double[] xy2 = nodes.get(i + 1).getAttribute("xyz");

			double angle = Math.toDegrees(Math.atan2(xy2[1] - xy1[1], xy2[0]
					- xy1[0]));

			if (i > 0)
				variation += angle - prev;

			prev = angle;
		}

		return variation;
	}
}
