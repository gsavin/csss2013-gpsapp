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
}
