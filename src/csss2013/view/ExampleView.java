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
import csss2013.process.ExampleProcess;

@Title("Speed")
public class ExampleView extends PlotView.PlotSeriesView implements TraceView {
	public ExampleView() {
		super("Average speed", ExampleProcess.SPEED_SERIES_DATA_NAME);

		this.xAxisLabel = "time in seconds";
		this.yAxisLabel = "speed in km / h";
		this.showLegend = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.TraceView#build(csss2013.App)
	 */
	public JComponent build(App app) {
		double distance, time, speed;
		LinkedList<String> series = new LinkedList<String>();

		//
		// Build the view :
		//
		JPanel globalInfos = new JPanel();
		globalInfos.setLayout(new GridLayout(app.getTraceCount() + 1, 4));

		globalInfos.add(new JLabel("Trace"));
		globalInfos.add(new JLabel("Distance"));
		globalInfos.add(new JLabel("Time"));
		globalInfos.add(new JLabel("Average speed"));

		for (int i = 0; i < app.getTraceCount(); i++) {
			Trace t = app.getTrace(i);

			//
			// We get data back :
			//
			distance = (Double) app.getData(ExampleProcess.DISTANCE_DATA_NAME
					+ t.getId());
			time = (Double) app.getData(ExampleProcess.TIME_DATA_NAME
					+ t.getId());
			speed = (Double) app.getData(ExampleProcess.SPEED_DATA_NAME
					+ t.getId());

			series.add(ExampleProcess.SPEED_SERIES_DATA_NAME + t.getId());

			globalInfos.add(new JLabel(t.getId()));
			globalInfos.add(new JLabel(String.format("%.2fm", distance)));
			globalInfos.add(new JLabel(String.format("%.0fs", time)));
			globalInfos.add(new JLabel(String.format("%.2fkm/h", speed)));
		}

		setDataNames(series.toArray(new String[app.getTraceCount()]));

		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(globalInfos, BorderLayout.NORTH);
		container.add(super.build(app), BorderLayout.CENTER);

		return container;
	}
}
