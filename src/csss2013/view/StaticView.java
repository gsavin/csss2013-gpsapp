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
import csss2013.process.Reload;

@Default
@Title("Static")
public class StaticView implements TraceView {
	public JComponent build(App app) {
		Graph merged = (Graph) app.getData(Merge.DATA_NAME);
		
		double[] anchorMin = (double[]) app
				.getData(Reload.MIN_ANCHOR_DATA_NAME);
		double[] anchorMax = (double[]) app
				.getData(Reload.MAX_ANCHOR_DATA_NAME);

		if (merged == null) {
			App.error("No merged graph found. Is the Merge process enabled ?");
			return new JLabel("Unavailable");
		}

		Viewer mergedViewer = new Viewer(merged,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View mergedView = mergedViewer.addDefaultView(false);
		
		mergedView.getCamera().setGraphViewport(anchorMin[0], anchorMin[1],
				anchorMax[0], anchorMax[1]);

		return mergedView;
	}
}
