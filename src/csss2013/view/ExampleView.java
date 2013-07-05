package csss2013.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import csss2013.App;
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

		//
		// We get data back :
		//
		distance = (Double) app.getData(ExampleProcess.DISTANCE_DATA_NAME);
		time = (Double) app.getData(ExampleProcess.TIME_DATA_NAME);
		speed = (Double) app.getData(ExampleProcess.SPEED_DATA_NAME);

		//
		// Build the view :
		//
		JPanel globalInfos = new JPanel();
		globalInfos.setLayout(new GridLayout(3, 2));

		globalInfos.add(new JLabel("Distance :"));
		globalInfos.add(new JLabel(String.format("%.2fm", distance)));

		globalInfos.add(new JLabel("Time :"));
		globalInfos.add(new JLabel(String.format("%.0fs", time)));

		globalInfos.add(new JLabel("Average speed :"));
		globalInfos.add(new JLabel(String.format("%.2fkm/h", speed)));

		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		container.add(globalInfos, BorderLayout.NORTH);
		container.add(super.build(app), BorderLayout.CENTER);

		return container;
	}
}
