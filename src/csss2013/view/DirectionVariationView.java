package csss2013.view;

import javax.swing.JComponent;

import csss2013.App;
import csss2013.Trace;
import csss2013.process.DirectionVariation;
import csss2013.view.PlotView.PlotSeriesView;

public class DirectionVariationView extends PlotSeriesView {

	public DirectionVariationView() {
		super("Direction Variation");

		xAxisLabel = "Time in seconds";
		yAxisLabel = "Variation in degrees";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.view.PlotView#build(csss2013.App)
	 */
	@Override
	public JComponent build(App app) {
		String[] datas = new String[app.getTraceCount()];

		for (int idx = 0; idx < app.getTraceCount(); idx++) {
			Trace t = app.getTrace(idx);
			datas[idx] = DirectionVariation.DATA_DV_SERIES + "." + t.getId();
		}

		setDataNames(datas);

		return super.build(app);
	}
}
