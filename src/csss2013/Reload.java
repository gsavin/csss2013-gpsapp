package csss2013;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.util.time.ISODateIO;

public class Reload extends AdjacencyListGraph {

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
	HashMap<Trace, EntryStack> stacks;
	long tick = 50;

	public Reload(Trace... traces) {
		super("reloader");

		stacks = new HashMap<Trace, EntryStack>();
		stylesheet = "node {size:15px;} node.anchor {size:1px;}";

		if (traces != null) {
			for (Trace t : traces)
				load(t);
		}
	}

	protected void load(Trace trace) {
		ISODateIO dateScanner = null, dateScannerNoMS = null;
		EntryStack stack = new EntryStack();
		stack.trace = trace;
		stacks.put(trace, stack);

		try {
			dateScanner = new ISODateIO("%FT%T.%k%z");
			dateScannerNoMS = new ISODateIO("%FT%T%z");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		stylesheet += String.format(" node#%s {fill-color:%s;}", trace.getId(),
				trace.getColor());

		for (int i = 0; i < trace.getNodeCount(); i++) {
			Node n = trace.getNode(i);
			String time = n.getAttribute("time");
			Calendar c = dateScanner.parse(time);
			
			if (c == null)
				c = dateScannerNoMS.parse(time);

			Entry e = new Entry();
			e.time = c.getTime().getTime();
			e.cal = c;
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

	public boolean hasRemaining() {
		for (EntryStack stack : stacks.values())
			if (stack.position < stack.size())
				return true;

		return false;
	}

	public void play() {
		reset();
		clear();

		long date = getStartDate();
		long step = (long) (tick * acceleration);

		addAttribute("ui.stylesheet", stylesheet);
		addAttribute("ui.quality");
		addAttribute("ui.antialias");

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

		Node anchorMin = addNode("anchor-min");
		anchorMin.addAttribute("xyz", new double[] { minX, minY, 0 });
		anchorMin.addAttribute("ui.class", "anchor");

		Node anchorMax = addNode("anchor-max");
		anchorMax.addAttribute("xyz", new double[] { maxX, maxY, 0 });
		anchorMax.addAttribute("ui.class", "anchor");

		while (hasRemaining()) {
			for (EntryStack stack : stacks.values()) {
				if (!stack.hasRemaining())
					continue;

				while (stack.position < stack.size() - 1
						&& stack.get(stack.position + 1).time <= date)
					stack.position++;

				if (stack.position == stack.size() - 1
						&& stack.current().time < date) {
					removeNode(stack.trace.getId());
					stack.position++;
					continue;
				}

				Node traceNode = getNode(stack.trace.getId());

				if (traceNode == null) {
					double[] initXYZ = stack.get(0).traceNode
							.getAttribute("xyz");

					traceNode = addNode(stack.trace.getId());
					traceNode.addAttribute("xyz", new double[] { initXYZ[0],
							initXYZ[1] });
				}

				double[] xyz = traceNode.getAttribute("xyz");
				double[] xyz1 = stack.current().traceNode.getAttribute("xyz");

				if (stack.position == stack.size() - 1) {
					xyz[0] = xyz1[0];
					xyz[1] = xyz1[1];
				} else {
					double[] xyz2 = stack.next().traceNode.getAttribute("xyz");
					double ratio = (date - stack.current().time)
							/ (double) (stack.next().time - stack.current().time);

					xyz[0] = xyz1[0] + ratio * (xyz2[0] - xyz1[0]);
					xyz[1] = xyz1[1] + ratio * (xyz2[1] - xyz1[1]);
				}

				traceNode.setAttribute("xyz", xyz);
			}

			date += step;

			try {
				Thread.sleep(tick);
			} catch (InterruptedException e) {
			}
		}
	}
}
