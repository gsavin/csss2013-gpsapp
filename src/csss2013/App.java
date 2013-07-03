/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.BusinessBlackSteelSkin;
import org.pushingpixels.substance.api.skin.GraphiteGlassSkin;
import org.pushingpixels.substance.api.skin.GraphiteSkin;
import org.pushingpixels.substance.api.skin.RavenSkin;

import csss2013.util.Palette;

public class App implements Runnable {
	public static final String DEFAULT_STYLESHEET = "node { size:10px;  }";

	public static class Config {
		HashMap<String, String> dataBateau;
		HashMap<String, String> dataBouees;
		HashMap<String, String> dataZodiac;
	}

	public static Graph mergeTrace(Trace... traces) {
		Graph g = new AdjacencyListGraph("merged");
		String stylesheet = "node {size:15px;}";

		for (Trace trace : traces) {
			stylesheet += String.format(" node.%s {fill-color:%s;}",
					trace.getId(), trace.getColor());

			for (int i = 0; i < trace.getNodeCount(); i++) {
				Node o = trace.getNode(i);
				Node n = g.addNode(String.format("%s:%s", trace.getId(),
						o.getId()));
				n.addAttribute("ui.class", trace.getId());

				for (String key : o.getAttributeKeySet())
					n.addAttribute(key, o.getAttribute(key));
			}

			for (int i = 0; i < trace.getEdgeCount(); i++) {
				Edge o = trace.getEdge(i);
				Edge e = g.addEdge(String.format("%s:%s", trace.getId(),
						o.getId()), String.format("%s:%s", trace.getId(), o
						.getSourceNode().getId()), String.format("%s:%s",
						trace.getId(), o.getTargetNode().getId()), o
						.isDirected());

				for (String key : o.getAttributeKeySet())
					e.addAttribute(key, o.getAttribute(key));
			}
		}

		g.addAttribute("ui.stylesheet", stylesheet);
		g.addAttribute("ui.quality");
		g.addAttribute("ui.antialias");

		return g;
	}

	JLabel gmaps;
	Graph merged;
	Reload reload;
	Trace[] traces;

	public App(Trace... traces) {
		this.traces = traces;

		merged = mergeTrace(traces);
		reload = new Reload(traces);
	}

	public void run() {
		Viewer mergedViewer = new Viewer(merged,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View mergedView = mergedViewer.addDefaultView(false);

		Viewer reloadViewer = new Viewer(reload,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View reloadView = reloadViewer.addDefaultView(false);

		JTabbedPane tabs = new JTabbedPane();
		Settings settings = new Settings();

		// gmaps = new GoogleMapsView(traces);

		tabs.addTab("Settings", settings);
		tabs.addTab("Static", mergedView);
		tabs.addTab("Dynamic", reloadView);
		// tabs.addTab("Google Maps", gmaps);

		tabs.setPreferredSize(new Dimension(640, 640));

		JFrame frame = new JFrame("CSSS2013 - GPS App");
		frame.add(tabs);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void play() {
		reload.play();
	}

	public static void main(String... args) throws IOException {
		final LinkedList<Trace> tracesList = new LinkedList<Trace>();
		final Palette palette = new Palette();

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					SubstanceLookAndFeel.setSkin(new GraphiteGlassSkin());
					JFrame.setDefaultLookAndFeelDecorated(true);
					JDialog.setDefaultLookAndFeelDecorated(false);

					int r = JOptionPane.showConfirmDialog(null,
							"Use default GPX files ?", "Traces",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);

					if (r == JOptionPane.YES_OPTION) {
						try {
							Trace t1 = Trace.load("bateau1", App.class
									.getResourceAsStream("data/Encausse1.gpx"));
							t1.setColor(palette.nextColor());

							tracesList.add(t1);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, e.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}

						try {
							Trace t2 = Trace.load("bateau2", App.class
									.getResourceAsStream("data/Encausse2.gpx"));
							t2.setColor(palette.nextColor());

							tracesList.add(t2);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, e.getMessage(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JFileChooser fileChooser = new JFileChooser(".");
						FileNameExtensionFilter filter = new FileNameExtensionFilter(
								"GPX traces", "gpx");

						fileChooser.setFileFilter(filter);
						fileChooser.setMultiSelectionEnabled(true);
						fileChooser.showOpenDialog(null);

						File[] files = fileChooser.getSelectedFiles();

						if (files == null || files.length == 0)
							System.exit(0);

						for (int i = 0; i < files.length; i++) {
							String traceName = files[i].getName()
									.replaceAll(".gpx$", "")
									.replaceAll("\\W", "_");
							System.out.printf("trace name : %s\n", traceName);
							Trace t;
							try {
								t = Trace.load(traceName, files[i]);
								t.setColor(palette.nextColor());

								tracesList.add(t);
							} catch (IOException e) {
								JOptionPane.showMessageDialog(null,
										e.getMessage(), "Error",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}

		Trace[] traces = new Trace[0];
		traces = tracesList.toArray(traces);

		Trace.normalize(traces);

		App app = new App(traces);
		SwingUtilities.invokeLater(app);

		/*
		 * while (true) { app.play();
		 * 
		 * try { Thread.sleep(1000); } catch (InterruptedException e) {
		 * e.printStackTrace(); } }
		 */
	}
}
