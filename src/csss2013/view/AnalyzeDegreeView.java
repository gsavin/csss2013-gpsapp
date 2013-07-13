package csss2013.view;

import javax.swing.JComponent;

import csss2013.App;
import csss2013.process.AnalyzeDegree;
import csss2013.view.PlotView.PlotSeriesView;

public class AnalyzeDegreeView extends PlotSeriesView {

	public AnalyzeDegreeView() {
		super("Average degree");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.view.PlotView#build(csss2013.App)
	 */
	@Override
	public JComponent build(App app) {
		String[] dstr = app.getProperty(AnalyzeDegree.SETTINGS_DISTANCES)
				.split("\\s*,\\s*");

		for (int i = 0; i < dstr.length; i++)
			dstr[i] = AnalyzeDegree.DATA_DISTANCES + "." + dstr[i];

		setDataNames(dstr);

		return super.build(app);
	}
}
