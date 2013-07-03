package csss2013;

import javax.swing.JComponent;

import csss2013.view.DynamicView;
import csss2013.view.GoogleMapsView;
import csss2013.view.StaticView;

public interface TraceView {
	public static enum Type {
		STATIC("Static", StaticView.class, 90), DYNAMIC("Dynamic",
				DynamicView.class, 100), GOOGLE_MAPS("Google Maps",
				GoogleMapsView.class, 10);

		int priority;
		String name;
		Class<? extends TraceView> clazz;

		Type(String name, Class<? extends TraceView> clazz, int priority) {
			this.name = name;
			this.clazz = clazz;
			this.priority = priority;
		}

		public JComponent getTraceView(App app)
				throws InstantiationException, IllegalAccessException {
			System.out.printf("Create %s view\n", name);
			TraceView view = clazz.newInstance();
			return view.build(app);
		}
	}

	JComponent build(App app);
}
