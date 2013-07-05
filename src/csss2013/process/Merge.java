package csss2013.process;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;

import csss2013.App;
import csss2013.Process;
import csss2013.PropertyKeys;
import csss2013.Trace;
import csss2013.annotation.Default;
import csss2013.annotation.Title;

@Default
@Title("Merge")
public class Merge implements Process, PropertyKeys {
	public static final String DATA_NAME = "process.merge.graph";

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
	public void process(App app) {
		Graph g = new AdjacencyListGraph("merged");
		String stylesheet = app.getProperty(VIEWS_STATIC_STYLESHEET,
				"node {size:10px;}");

		for (int idx = 0; idx < app.getTraceCount(); idx++) {
			Trace trace = app.getTrace(idx);
			String traceStyle = String.format("fill-color:%s;%s",
					trace.getColor(), trace.getCustomStyle());

			stylesheet += String.format(" node.%s {%s} edge.%s {%s}",
					trace.getId(), traceStyle, trace.getId(), traceStyle);

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

				e.addAttribute("ui.class", trace.getId());

				for (String key : o.getAttributeKeySet())
					e.addAttribute(key, o.getAttribute(key));
			}
		}

		g.addAttribute("ui.stylesheet", stylesheet);
		g.addAttribute("ui.quality");
		g.addAttribute("ui.antialias");

		app.setData(DATA_NAME, g);
	}

}
