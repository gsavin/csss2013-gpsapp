package csss2013.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import csss2013.App;
import csss2013.TraceView;
import csss2013.annotation.Title;
import csss2013.process.CompareTracesProcess;

@Title("CompareTraces")
public class CompareView extends PlotView.PlotSeriesView implements TraceView {
	public CompareView() {
		super("Average distance");

		this.xAxisLabel = "Time (seconds)";
		this.yAxisLabel = "Distance (meters)";
	}

	public JComponent build(App app) {
		LinkedList<String> datanames = new LinkedList<String>();
		String[] cs = app.getProperty("settings.process.compare.what").split(
				"\\s*,\\s*");

		JPanel globalInfos = new JPanel();
		globalInfos.setLayout(new GridLayout(2, cs.length + 1));

		globalInfos.add(new JLabel(""));

		for (int i = 0; i < cs.length; i++)
			globalInfos.add(new JLabel(cs[i]));

		globalInfos.add(new JLabel("Average distance"));

		for (int i = 0; i < cs.length; i++) {
			String dn = CompareTracesProcess.COMPARE_DISTANCE_SERIES_DATA_NAME;
			dn += "." + cs[i];

			datanames.add(dn);

			Double avg = (Double) app
					.getData(CompareTracesProcess.COMPARE_DISTANCE_DATA_NAME
							+ "." + cs[i]);
			
			globalInfos.add(new JLabel(String.format("%.2f meters", avg)));
		}

		setDataNames(datanames.toArray(new String[datanames.size()]));
		
		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(globalInfos, BorderLayout.NORTH);
		container.add(super.build(app), BorderLayout.CENTER);

		return container;
	}
}