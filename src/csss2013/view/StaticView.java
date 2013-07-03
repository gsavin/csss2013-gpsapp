package csss2013.view;

import javax.swing.JComponent;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

import csss2013.App;
import csss2013.Trace;
import csss2013.TraceView;

public class StaticView implements TraceView {
	public static Graph mergeTrace(App app) {
		Graph g = new AdjacencyListGraph("merged");
		String stylesheet = "node {size:15px;}";

		for (int idx = 0; idx < app.getTraceCount(); idx++) {
			Trace trace = app.getTrace(idx);

			stylesheet += String.format(" node.%s {fill-color:%s;%s}",
					trace.getId(), trace.getColor(), trace.getCustomStyle());

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

	public JComponent build(App app) {
		Graph merged = mergeTrace(app);
		Viewer mergedViewer = new Viewer(merged,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View mergedView = mergedViewer.addDefaultView(false);

		return mergedView;
	}
}
