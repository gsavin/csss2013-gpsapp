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
import java.util.Iterator;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.file.FileSourceGPX;
import org.graphstream.ui.swingViewer.Viewer;

public class Trace extends AdjacencyListGraph {
	public static final boolean HIGH_QUALITY_RENDERING = true;

	public static Trace load(String name, File file) throws IOException {
		if (file.exists()) {
			FileInputStream in = new FileInputStream(file);

			Trace t = load(name, in);
			in.close();

			return t;
		} else
			return load(name, file.getPath());
	}

	public static Trace load(String name, InputStream data) throws IOException {
		if (data == null) {
			System.err.printf("Data is null !\n");
			return null;
		}

		Trace t = new Trace(name);
		FileSourceGPX gpx = new FileSourceGPX();

		gpx.addSink(t);
		gpx.readAll(data);
		gpx.removeSink(t);

		return t;
	}

	public static Trace load(String name, String resource) throws IOException {
		File f = new File(resource);

		if (f.exists())
			return load(name, f);

		InputStream in = ClassLoader.getSystemResourceAsStream(resource);

		if (in == null)
			in = Trace.class.getResourceAsStream(resource);

		if (in == null)
			return null;

		Trace t = load(name, in);
		in.close();

		return t;
	}

	protected String color = "#222222";
	protected String customStyle = "";

	protected Trace(String id) {
		super(id);

		if (HIGH_QUALITY_RENDERING) {
			addAttribute("ui.quality");
			addAttribute("ui.antialias");
		}
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}

	public void setCustomStyle(String style) {
		this.customStyle = style;
	}

	public String getCustomStyle() {
		return customStyle;
	}

	@Override
	public Viewer display() {
		return display(false);
	}

	public Iterator<Node> getTracePath() {
		return new Path();
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
