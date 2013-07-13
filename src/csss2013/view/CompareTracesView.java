package csss2013.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import csss2013.App;
import csss2013.Trace;
import csss2013.TraceView;
import csss2013.annotation.Title;
import csss2013.process.CompareTracesProcess;

@Title("CompareTraces")
public class CompareTracesView extends PlotView.PlotSeriesView implements TraceView {
	public CompareTracesView() {
		super("Average distance", CompareTracesProcess.COMPARE_DISTANCE_SERIES_DATA_NAME);

		this.xAxisLabel = "Time (seconds)";
		this.yAxisLabel = "Distance (meters)";
		this.showLegend = false;
	}

	public JComponent build(App app) {
		JPanel globalInfos = new JPanel();
		globalInfos.setLayout(new GridLayout(1, 2));
		globalInfos.add(new JLabel("Average distance"));
		globalInfos.add(new JLabel(String.format("%.2f meters", (Double)app.getData(CompareTracesProcess.COMPARE_DISTANCE_DATA_NAME))));

		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(globalInfos, BorderLayout.NORTH);
		container.add(super.build(app), BorderLayout.CENTER);

		return container;		
	}
}