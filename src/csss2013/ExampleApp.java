package csss2013;

import csss2013.process.ExampleProcess;
import csss2013.view.ExampleView;

public class ExampleApp extends App {
	public static void main(String... args) {
		App.registerView("example", ExampleView.class);
		App.registerProcess("example", ExampleProcess.class);

		App.main(args);
	}
}
