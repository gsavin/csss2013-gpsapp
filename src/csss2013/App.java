/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.GraphiteGlassSkin;

import csss2013.annotation.Default;
import csss2013.annotation.Title;
import csss2013.process.Merge;
import csss2013.process.NormalizeXYZ;
import csss2013.process.Reload;
import csss2013.view.DynamicView;
import csss2013.view.GoogleMapsView;
import csss2013.view.StaticView;

public class App implements PropertyKeys, Runnable {
	public static final String DEFAULT_PROPERTIES = "default.xml";

	private final static HashMap<String, Class<? extends TraceView>> registeredViews = new HashMap<String, Class<? extends TraceView>>();
	private final static HashMap<String, Class<? extends Process>> registeredProcess = new HashMap<String, Class<? extends Process>>();
	private static Properties defaultProperties;

	public static void registerView(String name, Class<? extends TraceView> view) {
		registeredViews.put(name, view);
	}

	public static void registerProcess(String name,
			Class<? extends Process> process) {
		registeredProcess.put(name, process);
	}

	public static Iterable<String> getRegisteredViewName() {
		return registeredViews.keySet();
	}

	public static Collection<String> getDefaultViewName() {
		HashSet<String> names = new HashSet<String>();

		for (String name : getRegisteredViewName()) {
			Class<? extends TraceView> clazz = registeredViews.get(name);

			if (clazz.getAnnotation(Default.class) != null)
				names.add(name);
		}

		return names;
	}

	public static String getViewTitle(String name) {
		Class<? extends TraceView> clazz = registeredViews.get(name);
		Title title = clazz.getAnnotation(Title.class);

		if (title != null)
			return title.value();

		return name;
	}

	public static Iterable<String> getRegisteredProcessName() {
		return registeredProcess.keySet();
	}

	public static Collection<String> getDefaultProcessName() {
		HashSet<String> names = new HashSet<String>();

		for (String name : getRegisteredProcessName()) {
			Class<? extends Process> clazz = registeredProcess.get(name);

			if (clazz.getAnnotation(Default.class) != null)
				names.add(name);
		}

		return names;
	}

	public static String getProcessTitle(String name) {
		Class<? extends Process> clazz = registeredProcess.get(name);
		Title title = clazz.getAnnotation(Title.class);

		if (title != null)
			return title.value();

		return name;
	}

	public static void error(Throwable e) {
		StringBuilder buffer = new StringBuilder();
		String message = e.getMessage();
		buffer.append(e.getClass().getName()).append("\n");

		if (message != null && message.length() > 0)
			buffer.append(message);

		if (e.getCause() != null) {
			buffer.append("\nCaused by :\n");
			buffer.append(e.getCause().getClass().getName());
			buffer.append("\n");

			message = e.getCause().getMessage();

			if (message != null && message.length() > 0)
				buffer.append(message);
		}

		error(buffer.toString());

		e.printStackTrace();
	}

	public static void error(final String message) {
		Runnable r = new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, message, "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		};

		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
	}

	public static Properties loadProperties(String ressourceName)
			throws FileNotFoundException {
		File f = new File(ressourceName);
		InputStream in = null;

		if (f.exists())
			in = new FileInputStream(f);
		else {
			in = ClassLoader.getSystemResourceAsStream(ressourceName);

			if (in == null)
				in = App.class.getResourceAsStream(ressourceName);
		}

		if (in != null)
			return loadProperties(in);

		return null;
	}

	public static Properties loadProperties(InputStream in) {
		Properties prop = new Properties(defaultProperties);

		try {
			prop.loadFromXML(in);
			return prop;
		} catch (Exception e) {
			App.error(e);
		}

		return null;
	}

	static {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					SubstanceLookAndFeel.setSkin(new GraphiteGlassSkin());
					JFrame.setDefaultLookAndFeelDecorated(true);
					JDialog.setDefaultLookAndFeelDecorated(false);
				}
			});
		} catch (Exception e1) {
			System.err.printf("Fail to Substance LnF\n");
		}

		App.registerView("static", StaticView.class);
		App.registerView("dynamic", DynamicView.class);
		App.registerView("gmaps", GoogleMapsView.class);

		App.registerProcess("normalize", NormalizeXYZ.class);
		App.registerProcess("reload", Reload.class);
		App.registerProcess("merge", Merge.class);

		defaultProperties = new Properties();

		try {
			InputStream defIn = ClassLoader
					.getSystemResourceAsStream(DEFAULT_PROPERTIES);

			if (defIn == null)
				defIn = App.class.getResourceAsStream(DEFAULT_PROPERTIES);

			if (defIn == null)
				System.out.printf("Default properties file \"%s\" not found\n",
						DEFAULT_PROPERTIES);
			else
				defaultProperties.loadFromXML(defIn);
		} catch (Exception e) {
			App.error(e);
		}
	}

	protected Trace[] traces;
	protected final LinkedList<String> views;
	protected final LinkedList<Process> process;
	protected final HashMap<String, Object> data;
	protected JFrame progressDialog;
	protected Properties properties;

	public App() {
		this.traces = null;
		this.process = new LinkedList<Process>();
		this.views = new LinkedList<String>();
		this.data = new HashMap<String, Object>();
		this.properties = defaultProperties;
	}

	public void run() {
		SettingsWizard.launch(this);
	}

	public int getTraceCount() {
		return traces == null ? 0 : traces.length;
	}

	public Trace getTrace(int idx) {
		return traces[idx];
	}

	/**
	 * Add a new process. Order of process execution will be computed according
	 * to their priority.
	 * 
	 * @param p
	 *            the new process
	 */
	public void enableProcess(Process p) {
		process.add(p);
	}

	public void setData(String name, Object data) {
		this.data.put(name, data);
	}

	public Object getData(String name) {
		return this.data.get(name);
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties prop) {
		this.properties = prop;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getProperty(String key, String def) {
		return properties.getProperty(key, def);
	}

	public int getPropertyAsInt(String key) {
		return getPropertyAsInt(key, 0);
	}

	public int getPropertyAsInt(String key, int def) {
		String v = getProperty(key);

		if (v == null)
			return def;

		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException e) {
			App.error(e);
			return def;
		}
	}

	public double getPropertyAsDouble(String key) {
		return getPropertyAsDouble(key, Double.NaN);
	}

	public double getPropertyAsDouble(String key, double def) {
		String v = getProperty(key);

		if (v == null)
			return def;

		try {
			return Double.parseDouble(v);
		} catch (NumberFormatException e) {
			App.error(e);
			return def;
		}
	}

	protected JComponent buildView(String name) {
		if (!registeredViews.containsKey(name)) {
			App.error("View " + name + " is not registered");
			return null;
		}

		Class<? extends TraceView> clazz = registeredViews.get(name);
		TraceView view;

		try {
			view = clazz.newInstance();
			return view.build(this);
		} catch (Exception e) {
			App.error(e);
		}

		return null;
	}

	protected Process buildProcess(String name) {
		if (!registeredProcess.containsKey(name)) {
			App.error("Process " + name + " is not registered");
			return null;
		}

		Class<? extends Process> clazz = registeredProcess.get(name);
		Process process;

		try {
			process = clazz.newInstance();
			return process;
		} catch (Exception e) {
			App.error(e);
		}

		return null;
	}

	void wizardCompleted(Settings settings) {
		LinkedList<Trace> tracesList = new LinkedList<Trace>();

		for (Settings.TraceEntry e : settings) {
			try {
				Trace t = Trace.load(e.name, e.data);
				t.setColor(e.color);
				t.setCustomStyle(e.style);

				tracesList.add(t);
			} catch (IOException e1) {
				error(e1.getMessage());
			}
		}

		this.traces = tracesList.toArray(new Trace[0]);
		views.addAll(settings.viewTypes);

		process.clear();

		for (String pname : settings.processTypes)
			process.add(buildProcess(pname));

		launchProcess();
	}

	protected void processTerminated() {
		Runnable r = new Runnable() {
			public void run() {
				showViews();
				
				progressDialog.setVisible(false);
				progressDialog = null;
			}
		};

		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
	}

	protected void launchProcess() {
		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		progressDialog = new JFrame("");
		progressDialog.setLayout(new BorderLayout());
		progressDialog.add(new JLabel("Process execution in progress ..."),
				BorderLayout.NORTH);
		progressDialog.add(bar, BorderLayout.CENTER);
		progressDialog.pack();
		progressDialog.setVisible(true);

		Thread t = new Thread(new ProcessWorker(), "process");
		t.setDaemon(true);
		t.start();
	}

	protected void showViews() {
		JTabbedPane tabs = new JTabbedPane();

		tabs.setPreferredSize(new Dimension(640, 640));

		for (String type : views) {
			JComponent comp = buildView(type);
			tabs.addTab(getViewTitle(type), comp);
		}

		JFrame frame = new JFrame(getProperty(APP_TITLE));
		frame.add(tabs);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	class ProcessWorker implements Runnable {
		public void run() {
			Collections.sort(process, new Comparator<Process>() {
				public int compare(Process arg0, Process arg1) {
					if (arg0.getPriority() < arg1.getPriority())
						return 1;
					else if (arg0.getPriority() > arg1.getPriority())
						return -1;

					return 0;
				}
			});

			for (int i = 0; i < process.size(); i++)
				process.get(i).process(App.this);

			processTerminated();
		}
	}

	public static void main(String... args) {
		App app = new App();
		SwingUtilities.invokeLater(app);
	}
}
