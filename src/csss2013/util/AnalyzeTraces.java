package csss2013.util;

import java.io.IOException;
import java.lang.reflect.Array;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.swingViewer.Viewer;

public class AnalyzeTraces extends SinkAdapter {
	public static final String TRACES_DGS = "export/output.dgs";

	boolean viewportSet = false;
	FileSourceDGS dgs;
	Graph g;
	Viewer v;

	public AnalyzeTraces() {
		dgs = new FileSourceDGS();
		g = new AdjacencyListGraph("traces");

		dgs.addSink(g);

		try {
			dgs.begin(TRACES_DGS);
		} catch (IOException e) {
			e.printStackTrace();
		}

		v = g.display(false);
	}

	public void analyze() {
		while (next()) {
			//
			// TODO ....
			//
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	}

	public boolean next() {
		boolean next = false;

		try {
			next = dgs.nextStep();

			if (false && !viewportSet && g.hasAttribute("ui.viewport")) {
				Object[] anchor = g.getAttribute("ui.viewport");
				
				v.getDefaultView()
						.getCamera()
						.setGraphViewport((Double) anchor[0],
								(Double) anchor[1], (Double) anchor[2],
								(Double) anchor[3]);
				
				viewportSet = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return next;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#stepBegins(java.lang.String,
	 * long, double)
	 */
	public void stepBegins(String sourceId, long timeId, double step) {
		analyze();
	}

	public static void main(String... args) {
		AnalyzeTraces a = new AnalyzeTraces();
		a.analyze();
	}
}
