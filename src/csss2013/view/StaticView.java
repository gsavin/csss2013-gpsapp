package csss2013.view;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.graphstream.graph.Graph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

import csss2013.App;
import csss2013.TraceView;
import csss2013.annotation.Default;
import csss2013.annotation.Title;
import csss2013.process.Merge;

@Default
@Title("Static")
public class StaticView implements TraceView {
	public JComponent build(App app) {
		Graph merged = (Graph) app.getData(Merge.DATA_NAME);

		if (merged == null) {
			App.error("No merged graph found. Is the Merge process enabled ?");
			return new JLabel("Unavailable");
		}

		Viewer mergedViewer = new Viewer(merged,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View mergedView = mergedViewer.addDefaultView(false);

		return mergedView;
	}
}
