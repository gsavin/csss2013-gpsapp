package csss2013.view;

import java.lang.reflect.Array;

import javax.swing.JComponent;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import csss2013.App;
import csss2013.TraceView;

public abstract class PlotView implements TraceView {
	public static enum PlotType {
		/**
		 * Points connected with lines.
		 */
		LINE,
		/**
		 * 
		 */
		BAR,
		/**
		 * Cloud of points.
		 */
		SCATTER
	}

	public JComponent build(App app) {
		ChartPanel panel = new ChartPanel(getChart(app), true, true, true,
				true, true);

		return panel;
	}

	protected abstract JFreeChart getChart(App app);

	public static class PlotSeriesView extends PlotView {
		String[] dataNames;
		PlotType type;
		PlotOrientation orientation;
		boolean showLegend;
		String xAxisLabel, yAxisLabel;
		String title;

		public PlotSeriesView(String title, String... dataNames) {
			this.dataNames = dataNames;
			this.orientation = PlotOrientation.VERTICAL;
			this.showLegend = true;
			this.xAxisLabel = "x";
			this.yAxisLabel = "y";
			this.title = title;
			this.type = PlotType.LINE;
		}

		public void setDataNames(String... dataNames) {
			this.dataNames = dataNames;
		}

		protected JFreeChart getChart(App app) {
			JFreeChart chart;
			XYSeriesCollection dataset = new XYSeriesCollection();

			for (String name : dataNames)
				dataset.addSeries(getSeries(app, name));

			switch (type) {
			default:
				chart = ChartFactory.createXYLineChart(title, xAxisLabel,
						yAxisLabel, dataset, orientation, showLegend, false,
						false);
				break;
			case BAR:
				chart = ChartFactory.createXYBarChart(title, xAxisLabel, false,
						yAxisLabel, dataset, orientation, showLegend, false,
						false);
				break;
			case SCATTER:
				chart = ChartFactory.createScatterPlot(title, xAxisLabel,
						yAxisLabel, dataset, orientation, showLegend, false,
						false);
				break;
			}

			return chart;
		}

		protected XYSeries getSeries(App app, String name) {
			String title = name;
			Object data = app.getData(name);

			if (app.getData(name + ".title") != null)
				title = (String) app.getData(name + ".title");

			XYSeries series = new XYSeries(title);

			if (data != null) {
				for (int i = 0; i < Array.getLength(data); i++) {
					Object e = Array.get(data, i);

					if (!e.getClass().isArray() || Array.getLength(e) < 2) {
						App.error("Invalid entry \"%s\" in series %s", e, name);
						break;
					}

					double x = Array.getDouble(e, 0);
					double y = Array.getDouble(e, 1);

					series.add(x, y);
				}
			}

			return series;
		}
	}
}
