/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013.process;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.Timeline;

import csss2013.App;
import csss2013.Process;
import csss2013.PropertyKeys;
import csss2013.Trace;
import csss2013.annotation.Default;
import csss2013.annotation.Title;
import csss2013.util.Tools;

@Default
@Title("Reload")
public class Reload implements Process, PropertyKeys {
	public static final String TIMELINE_DATA_NAME = "process.reload.file";
	public static final String MIN_ANCHOR_DATA_NAME = "process.reload.anchor.min";
	public static final String MAX_ANCHOR_DATA_NAME = "process.reload.anchor.max";

	protected class Entry implements Comparable<Entry> {
		long time;
		Calendar cal;
		Node traceNode;
		Trace trace;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Entry arg0) {
			return cal.compareTo(arg0.cal);
		}
	}

	protected class EntryStack extends LinkedList<Entry> {
		private static final long serialVersionUID = -1805788894770734746L;

		int position;
		Trace trace;

		public Entry current() {
			return get(position);
		}

		public Entry next() {
			return get(position + 1);
		}

		public boolean hasRemaining() {
			return position < size();
		}
	}

	double acceleration = 30;
	String stylesheet;
	final HashMap<Trace, EntryStack> stacks;

	/**
	 * In meters.
	 */
	double minDistance = 5;

	public Reload() {
		stacks = new HashMap<Trace, EntryStack>();
		stylesheet = "node {size:15px;} node.anchor {size:1px;}";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#getPriority()
	 */
	public int getPriority() {
		return 90;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#process(csss2013.App)
	 */
	public synchronized void process(App app) {
		Graph g = new AdjacencyListGraph("reload");
		Timeline timeline = new Timeline();

		minDistance = app.getPropertyAsDouble(PROCESS_RELOAD_MIN_DISTANCE, 5);
		stylesheet = app.getProperty(VIEWS_DYNAMIC_STYLESHEET,
				"node {size:15px;}");

		stacks.clear();

		for (int idx = 0; idx < app.getTraceCount(); idx++)
			load(app.getTrace(idx));

		double minX, minY, maxX, maxY;
		minX = minY = Double.MAX_VALUE;
		maxX = maxY = Double.MIN_VALUE;

		for (EntryStack stack : stacks.values())
			for (int i = 0; i < stack.size(); i++) {
				double[] xyz = stack.get(i).traceNode.getAttribute("xyz");

				minX = Math.min(minX, xyz[0]);
				minY = Math.min(minY, xyz[1]);
				maxX = Math.max(maxX, xyz[0]);
				maxY = Math.max(maxY, xyz[1]);
			}

		app.setData(MIN_ANCHOR_DATA_NAME, new double[] { minX, minY });
		app.setData(MAX_ANCHOR_DATA_NAME, new double[] { maxX, maxY });

		timeline.begin(g);
		reload(g);
		timeline.end();
		timeline.seekStart();

		app.setData(TIMELINE_DATA_NAME, timeline);
	}

	protected void load(Trace trace) {
		EntryStack stack = new EntryStack();
		stack.trace = trace;
		stacks.put(trace, stack);

		stylesheet += String.format(" node#%s {fill-color:%s;%s}",
				trace.getId(), trace.getColor(), trace.getCustomStyle());

		for (int i = 0; i < trace.getNodeCount(); i++) {
			Node n = trace.getNode(i);

			Entry e = new Entry();
			e.time = Tools.getTime(n);
			e.cal = Tools.getCalendarTime(n);
			e.traceNode = n;
			e.trace = trace;

			stack.add(e);
		}

		Collections.sort(stack);
	}

	public void reset() {
		for (EntryStack stack : stacks.values())
			stack.position = 0;
	}

	public long getStartDate() {
		long start = Long.MAX_VALUE;

		for (EntryStack stack : stacks.values())
			start = Math.min(start, stack.get(0).time);

		return start;
	}

	/*
	 * public long getTick() { reset();
	 * 
	 * long current = getNextDate(); long next; long tick = Long.MAX_VALUE;
	 * 
	 * while (hasRemaining()) { next = getNextDate(); tick = Math.min(tick, next
	 * - current); current = next; }
	 * 
	 * return Math.max(1, tick); }
	 */

	protected EntryStack getNextStack() {
		EntryStack winner = null;

		for (EntryStack stack : stacks.values()) {
			if (stack.hasRemaining()
					&& (winner == null || stack.current().time < winner
							.current().time))
				winner = stack;
		}

		return winner;
	}

	public boolean hasRemaining() {
		for (EntryStack stack : stacks.values())
			if (stack.hasRemaining())
				return true;

		return false;
	}

	protected void reload(Graph g) {
		double min, max, avg;
		int count = 0;
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		avg = 0;
		reset();

		g.addAttribute("ui.stylesheet", stylesheet);
		g.addAttribute("ui.quality");
		g.addAttribute("ui.antialias");

		while (hasRemaining()) {
			EntryStack current = getNextStack();
			long date = current.current().time;

			g.setAttribute("time", date);
			g.stepBegins((double) date);

			for (EntryStack stack : stacks.values()) {
				if (!stack.hasRemaining())
					continue;

				if (stack.position == stack.size() - 1
						&& stack.current().time < date) {
					g.removeNode(stack.trace.getId());
					stack.position++;
					continue;
				}

				Node traceNode = g.getNode(stack.trace.getId());

				if (traceNode == null) {
					double[] initXYZ = stack.get(0).traceNode
							.getAttribute("xyz");

					traceNode = g.addNode(stack.trace.getId());
					traceNode.addAttribute("xyz", new double[] { initXYZ[0],
							initXYZ[1] });
				}

				double[] xyz = traceNode.getAttribute("xyz");
				double[] xyz1 = stack.current().traceNode.getAttribute("xyz");
				double lat = stack.current().traceNode.getNumber("lat");
				double lon = stack.current().traceNode.getNumber("lon");

				if (stack.position == stack.size() - 1) {
					xyz[0] = xyz1[0];
					xyz[1] = xyz1[1];
				} else {
					double[] xyz2 = stack.next().traceNode.getAttribute("xyz");
					double ratio = (date - stack.current().time)
							/ (double) (stack.next().time - stack.current().time);

					xyz[0] = xyz1[0] + ratio * (xyz2[0] - xyz1[0]);
					xyz[1] = xyz1[1] + ratio * (xyz2[1] - xyz1[1]);

					lat += ratio
							* (stack.next().traceNode.getNumber("lat") - lat);

					if (Double.isNaN(lat)) {
						System.err
								.printf("NaN lat (ratio=%f;tlat=%f;lat=%f;date=%d;current=%d;next=%d)\n",
										ratio,
										stack.next().traceNode.getNumber("lat"),
										lat, date, stack.current().time,
										stack.next().time);
						System.exit(1);
					}

					lon += ratio
							* (stack.next().traceNode.getNumber("lon") - lon);

					if (Double.isNaN(lon)) {
						System.err.printf("NaN lon (%f;%f;%f)\n", ratio,
								stack.next().traceNode.getNumber("lon"), lon);
						System.exit(1);
					}
				}

				traceNode.setAttribute("xyz", xyz);
				traceNode.setAttribute("lat", lat);
				traceNode.setAttribute("lon", lon);
			}

			double d = checkLinks(g);

			min = Math.min(d, min);
			max = Math.max(d, max);
			avg += d;
			count++;

			current.position++;
		}

		avg /= count;

		System.out.printf("Min average distance     : %f\n", min);
		System.out.printf("Average average distance : %f\n", avg);
		System.out.printf("Min average distance     : %f\n", max);
	}

	protected double checkLinks(Graph g) {
		double avg = 0;
		int k = 0;
		for (int i = 0; i < g.getNodeCount() - 1; i++) {
			Node n1 = g.getNode(i);

			for (int j = i + 1; j < g.getNodeCount(); j++) {
				Node n2 = g.getNode(j);
				Edge e = n1.getEdgeBetween(n2);

				double d = Tools.distance(n1, n2);

				if (Double.isNaN(d)) {
					System.err.printf("NaN distance between %s and %s at %f\n",
							n1.getId(), n2.getId(), g.getStep());

					double lat1 = n1.getNumber("lat");
					double lon1 = n1.getNumber("lon");
					double lat2 = n2.getNumber("lat");
					double lon2 = n2.getNumber("lon");

					System.err.printf("n1 (%f;%f) n2 (%f;%f)\n", lat1, lon1,
							lat2, lon2);
				}

				avg += d;
				k++;
				if (e == null) {
					if (d <= minDistance) {
						e = g.addEdge(n1.getId() + "__" + n2.getId(), n1, n2);
						e.setAttribute("distance", d);
					}
				} else if (d > minDistance)
					g.removeEdge(e);
				else
					e.setAttribute("distance", d);
			}
		}

		return avg / (double) k;
	}

}
