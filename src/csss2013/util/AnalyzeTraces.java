package csss2013.util;

import org.graphstream.stream.SinkAdapter;

public class AnalyzeTraces extends SinkAdapter {
	public static final String TRACES_DGS = "export/output.dgs";

	public AnalyzeTraces() {

	}

	public void analyze() {
		//
		// TODO ....
		//
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#stepBegins(java.lang.String,
	 * long, double)
	 */
	public void stepBegins(String sourceId, long timeId, double step) {
		analyze();
	}
}
