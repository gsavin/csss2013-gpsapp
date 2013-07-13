package csss2013;

import csss2013.process.CompareTracesProcess;
import csss2013.view.CompareTracesView;

public class CompareTracesApp extends App {
	public static void main(String... args) {
		App.registerView("compare traces", CompareTracesView.class);
		App.registerProcess("compare traces", CompareTracesProcess.class);
		App.main(args);
	}
}
