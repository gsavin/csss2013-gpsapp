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

			while (it.hasNext()) {
				current = it.next();

				data.add(new double[] { Tools.getTime(current),
						getVariation(t, current, 10000) });
			}

			app.setData(DATA_DV_SERIES + "." + t.getId(), data.toArray());
			app.setData(DATA_DV_SERIES + "." + t.getId() + ".title", t.getId());
		}
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
