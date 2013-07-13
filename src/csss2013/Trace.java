/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.file.FileSourceGPX;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.util.time.ISODateIO;

import csss2013.util.Tools;

public class Trace extends AdjacencyListGraph implements PropertyKeys {
	public static final boolean HIGH_QUALITY_RENDERING = true;

	public static Trace load(App app, String name, File file)
			throws IOException {
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);

			Trace t = load(app, name, in);
			in.close();

			return t;
		} else
			return load(app, name, file.getPath());
	}

	public static Trace load(App app, String name, InputStream data)
			throws IOException {
		if (data == null) {
			System.err.printf("Data is null !\n");
			return null;
		}

		boolean timeRound = app.getPropertyAsBoolean(TRACE_TIME_ROUND);

		Trace t = new Trace(name, timeRound);
		FileSourceGPX gpx = new FileSourceGPX();

		gpx.addSink(t);
		gpx.readAll(data);
		gpx.removeSink(t);

		t.check();

		return t;
	}

	public static Trace load(App app, String name, String resource)
			throws IOException {
		File f = new File(resource);

		if (f.exists())
			return load(app, name, f);

		InputStream in = ClassLoader.getSystemResourceAsStream(resource);

		if (in == null)
			in = Trace.class.getResourceAsStream(resource);

		if (in == null)
			return null;

		Trace t = load(app, name, in);
		in.close();

		return t;
	}

	static ISODateIO dateScanner = null, dateScannerNoMS = null;

	static {
		try {
			dateScanner = new ISODateIO("%FT%T.%k%z");
			dateScannerNoMS = new ISODateIO("%FT%T%z");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		String time = "2013-07-11T17:09:15.737Z";
		Calendar c = dateScanner.parse(time);

		if (c == null)
			c = dateScannerNoMS.parse(time);

		int ms = c.get(Calendar.MILLISECOND);

		System.out.println(c.getTime());

		if (ms > 500)
			c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 1);

		c.set(Calendar.MILLISECOND, 0);

		System.out.println(c.getTime());
	}

	protected String color = "#222222";
	protected String customStyle = "";

	protected boolean roundTime;

	protected Trace(String id, boolean roundTime) {
		super(id);

		this.roundTime = roundTime;

		if (HIGH_QUALITY_RENDERING) {
			addAttribute("ui.quality");
			addAttribute("ui.antialias");
		}
	}

	protected void check() {
		Iterator<Node> it = getTracePath();
		Node current, next;
		LinkedList<Node> toDelete = new LinkedList<Node>();

		for (int i = 0; i < getNodeCount(); i++) {
			Node n = getNode(i);

			double lat = n.getNumber("lat");
			double lon = n.getNumber("lon");

			if (Double.isNaN(lat)) {
				System.out.printf("[%s] Latitude is NaN at %d\n", getId(),
						n.getAttribute("time.ms"));
				System.exit(1);
			}

			if (Double.isNaN(lon)) {
				System.out.printf("[%s] Longitude is NaN at %d\n", getId(),
						n.getAttribute("time.ms"));
				System.exit(1);
			}
		}

		if (!it.hasNext())
			return;

		current = it.next();

		while (it.hasNext()) {
			next = it.next();

			if (Tools.getTime(current) == Tools.getTime(next)) {
				double d = Tools.distance(current, next);

				if (d > 1) {
					System.err.printf(
							"[WARNING] there are two points with same "
									+ "timestamp but distance is %fm\n", d);
				}

				toDelete.add(next);
			}

			current = next;
		}

		for (int i = 0; i < toDelete.size(); i++)
			deleteAndMerge(toDelete.get(i));
	}

	protected void deleteAndMerge(Node n) {
		if (n.getDegree() == 2) {
			Node a, b;

			b = n.getLeavingEdge(0).getOpposite(n);
			a = n.getEnteringEdge(0).getOpposite(n);

			addEdge(a.getId() + "__" + b.getId(), a, b, true);
		}

		removeNode(n);
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}

	public void setCustomStyle(String style) {
		if (style != null)
			this.customStyle = style;
	}

	public String getCustomStyle() {
		return customStyle;
	}

	public Iterator<Node> getTracePath() {
		return new Path();
	}

	protected void checkTime(String nodeId) {
		Node n = getNode(nodeId);
		String time = n.getAttribute("time");
		Calendar c = dateScanner.parse(time);

		if (c == null)
			c = dateScannerNoMS.parse(time);

		int ms = c.get(Calendar.MILLISECOND);

		if (ms != 0 && roundTime) {
			if (ms > 500)
				c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 1);

			c.set(Calendar.MILLISECOND, 0);
		}

		n.setAttribute("time.calendar", c);
		n.setAttribute("time.ms", c.getTime().getTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.graph.implementations.AbstractGraph#display()
	 */
	@Override
	public Viewer display() {
		return display(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.graph.implementations.AbstractGraph#nodeAttributeAdded
	 * (java.lang.String, long, java.lang.String, java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		super.nodeAttributeAdded(sourceId, timeId, nodeId, attribute, value);

		if (attribute.equals("time"))
			checkTime(nodeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.graph.implementations.AbstractGraph#nodeAttributeChanged
	 * (java.lang.String, long, java.lang.String, java.lang.String,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		super.nodeAttributeChanged(sourceId, timeId, nodeId, attribute,
				oldValue, newValue);

		if (attribute.equals("time"))
			checkTime(nodeId);
	}

	class Path implements Iterator<Node> {
		Node current;

		public Path() {
			for (Node n : Trace.this) {
				if (n.getInDegree() == 0) {
					current = n;
					break;
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return current != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		public Node next() {
			Node next = current;

			if (current.getOutDegree() > 0)
				current = current.getLeavingEdge(0).getOpposite(current);
			else
				current = null;

			return next;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
		}
	}
}
