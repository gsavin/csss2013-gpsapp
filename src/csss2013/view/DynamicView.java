package csss2013.view;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.Timeline;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

import csss2013.App;
import csss2013.TraceView;
import csss2013.process.Reload;

public class DynamicView implements TraceView {
	public JComponent build(App app) {
		Timeline timeline = (Timeline) app.getData(Reload.TIMELINE_DATA_NAME);

		if (timeline == null) {
			App.error("No reload timeline found. Is the Reload process enabled ?");
			return new JLabel("Unavailable");
		}

		Graph tmp = new AdjacencyListGraph("dynamic");
		timeline.addSink(tmp);

		Viewer reloadViewer = new Viewer(tmp,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View reloadView = reloadViewer.addDefaultView(false);
		reloadView.setMouseManager(null);

		Controller c = new Controller(tmp, timeline);
		reloadView.addComponentListener(c);

		return reloadView;
	}

	static class Controller implements ComponentListener, Runnable {
		Reload reload;
		volatile boolean alive;
		Thread current;
		Timeline timeline;
		Graph g;
		double acceleration;

		public Controller(Graph g, Timeline timeline) {
			this.timeline = timeline;
			this.g = g;
		}

		public void run() {
			while (alive)
				play();

			current = null;
		}

		protected void play() {
			g.clear();
			timeline.seekStart();

			while (timeline.hasNext() && !current.isInterrupted()) {
				timeline.next();

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		protected void kill() {
			Thread t = current;
			alive = false;

			if (t != null)
				try {
					System.out.printf("Join\n");
					t.interrupt();
					t.join();
					System.out.printf("End\n");
				} catch (InterruptedException e) {
				}
		}

		public void componentHidden(ComponentEvent arg0) {
			kill();
		}

		public void componentMoved(ComponentEvent arg0) {
		}

		public void componentResized(ComponentEvent arg0) {
		}

		public void componentShown(ComponentEvent arg0) {
			if (current != null)
				kill();

			alive = true;
			current = new Thread(this);
			current.setDaemon(true);
			current.start();
		}
	}
}
