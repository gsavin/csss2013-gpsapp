package csss2013.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.stream.Timeline;

import csss2013.App;
import csss2013.Process;
import csss2013.PropertyKeys;
import csss2013.annotation.Title;

@Title("TXT Output")
public class OutputTXT implements PropertyKeys, Process {
	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#getPriority()
	 */
	public int getPriority() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#process(csss2013.App)
	 */
	public void process(App app) {
		Timeline timeline = (Timeline) app.getData(Reload.TIMELINE_DATA_NAME);
		Graph g = new AdjacencyListGraph("g");
		PrintStream out = System.out;

		if (app.hasProperty(PROCESS_OUTPUT_TXT_PATH)) {
			File f = new File(app.getProperty(PROCESS_OUTPUT_TXT_PATH));

			try {
				f.getParentFile().mkdirs();
			} catch (Exception e) {

			}

			try {
				out = new PrintStream(f);
			} catch (FileNotFoundException e) {
				App.error(e);
				return;
			}
		}

		Plotter p = new Plotter(g, out);

		timeline.addSink(g);
		g.addSink(p);

		timeline.seekStart();

		while (timeline.hasNext())
			timeline.next();

		timeline.removeSink(g);

		out.flush();

		if (out != System.out)
			out.close();
	}

	static class Plotter extends SinkAdapter {
		Graph g;
		PrintStream out;
		boolean first;

		public Plotter(Graph g, PrintStream out) {
			this.g = g;
			this.out = out;
			first = true;
		}

		public void plot() {
			if (g.getNodeCount() == 0)
				return;

			LinkedList<String> ids = new LinkedList<String>();
			for (int i = 0; i < g.getNodeCount(); i++)
				ids.add(g.getNode(i).getId());

			Collections.sort(ids);

			if (first) {
				out.printf("# ");
				for (int i = 0; i < ids.size(); i++)
					out.printf("%-20s\t\t", ids.get(i));
				out.printf("\n");

				first = false;
			}

			for (int i = 0; i < ids.size(); i++) {
				Double[] xyz = g.getNode(ids.get(i)).getAttribute("xyz");

				if (i > 0)
					out.printf("\t\t");

				out.printf(Locale.ROOT, "%.5f\t%.5f", xyz[0], xyz[1]);
			}

			out.printf("\n");
		}

		public void stepBegins(String sourceId, long timeId, double step) {
			plot();
		}
	}
}
