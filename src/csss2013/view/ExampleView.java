package csss2013.view;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import csss2013.App;
import csss2013.TraceView;
import csss2013.annotation.Title;
import csss2013.process.ExampleProcess;

@Title("My Crazy View")
public class ExampleView implements TraceView {
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
		JPanel container = new JPanel();
		container.setLayout(new GridLayout(3, 2));

		container.add(new JLabel("Distance :"));
		container.add(new JLabel(String.format("%.2fm", distance)));

		container.add(new JLabel("Time :"));
		container.add(new JLabel(String.format("%.0fs", time)));

		container.add(new JLabel("Speed :"));
		container.add(new JLabel(String.format("%.2fkm/h", speed)));

		return container;
	}
}
