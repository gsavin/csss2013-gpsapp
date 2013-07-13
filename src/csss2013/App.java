/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import csss2013.annotation.Default;
import csss2013.annotation.Title;
import csss2013.process.Merge;
import csss2013.process.OutputTXT;
import csss2013.process.NormalizeXYZ;
import csss2013.process.Reload;
import csss2013.util.Palette;
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

	public static void error(String message, Object... args) {
		if (args != null && args.length > 0)
			message = String.format(message, args);

		final String finalMessage = message;

		Runnable r = new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, finalMessage, "Error",
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

	public static Properties getProperties(String ressourceName)
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

		if (in != null) {
			Properties p = getProperties(in);

			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return p;
		}

		return null;
	}

	public static Properties getProperties(InputStream in) {
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
					try {
						Class<?> substance = Class
								.forName("org.pushingpixels.substance.api.SubstanceLookAndFeel");
						Class<?> skin = Class
								.forName("org.pushingpixels.substance.api.SubstanceSkin");
						Class<?> appSkin = Class
								.forName("org.pushingpixels.substance.api.skin.GraphiteGlassSkin");

						Method setSkin = substance.getMethod("setSkin", skin);
						setSkin.invoke(null, appSkin.newInstance());

						JFrame.setDefaultLookAndFeelDecorated(true);
						JDialog.setDefaultLookAndFeelDecorated(false);
					} catch (Exception e) {
						System.err.printf("Substance is disabled\n");
					}
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
		App.registerProcess("netlogo", OutputTXT.class);

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

	protected final LinkedList<Trace> traces;
	protected final LinkedList<String> views;
	protected final LinkedList<String> process;
	protected final HashMap<String, Object> data;
	protected JFrame progressDialog;
	protected Properties properties;

	public App(String... args) {
		this.traces = new LinkedList<Trace>();
		this.process = new LinkedList<String>();
		this.views = new LinkedList<String>();
		this.data = new HashMap<String, Object>();
		this.properties = defaultProperties;

		this.process.addAll(getDefaultProcessName());
		this.views.addAll(getDefaultViewName());

		processArgs(args);
	}

	public void run() {
		if (getPropertyAsBoolean(APP_WIZARD, true))
			SettingsWizard.launch(this);
		else
			runApp();
	}

	public int getTraceCount() {
		return traces.size();
	}

	public Trace getTrace(int idx) {
		return traces.get(idx);
	}

	public void setData(String name, Object data) {
		this.data.put(name, data);
	}

	public Object getData(String name) {
		return this.data.get(name);
	}

	public void loadProperties(String path) throws FileNotFoundException {
		properties = getProperties(path);
		flushProperties();
	}

	public void flushProperties() {
		String proc = getProperty("settings.process");
		this.process.clear();

		if (proc != null) {
			String[] process = proc.split("\\s*,\\s*");

			for (String p : process) {
				if (p.length() == 0)
					continue;

				String tryClass = getProperty(String.format(
						"settings.process.%s.class", p));

				if (tryClass != null) {
					try {
						@SuppressWarnings("unchecked")
						Class<? extends Process> pClass = (Class<? extends Process>) Class
								.forName(tryClass);

						App.registerProcess(p, pClass);
					} catch (ClassNotFoundException e) {
						App.error(e);
					}
				}

				this.process.add(p);
			}
		} else
			this.process.addAll(App.getDefaultProcessName());

		String vs = getProperty("settings.views");
		this.views.clear();

		if (vs != null) {
			String[] views = vs.split("\\s*,\\s*");

			for (String v : views) {
				if (v.length() == 0)
					continue;

				String tryClass = getProperty(String.format(
						"settings.views.%s.class", v));

				if (tryClass != null) {
					try {
						@SuppressWarnings("unchecked")
						Class<? extends TraceView> pClass = (Class<? extends TraceView>) Class
								.forName(tryClass);

						App.registerView(v, pClass);
					} catch (ClassNotFoundException e) {
						App.error(e);
					}
				}

				this.views.add(v);
			}
		} else
			this.views.addAll(App.getDefaultViewName());
	}

	public Properties getProperties() {
		return properties;
	}

	public boolean hasProperty(String key) {
		return properties.getProperty(key) != null;
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

	public boolean getPropertyAsBoolean(String key) {
		return getPropertyAsBoolean(key, true);
	}

	public boolean getPropertyAsBoolean(String key, boolean def) {
		String v = getProperty(key);

		if (v == null)
			return def;

		try {
			return Boolean.parseBoolean(v);
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

	void runApp() {
		flushProperties();
		loadTraces();
		launchProcess();
	}

	protected void loadTraces() {
		this.traces.clear();
		String[] traces = getProperty("settings.traces", "").split("\\s*,\\s*");

		Palette palette = new Palette();

		for (String trace : traces) {
			if (trace.length() == 0)
				continue;

			String name = getProperty(String.format("settings.%s.name", trace));
			String data = getProperty(String.format("settings.%s.data", trace));
			String color = getProperty(
					String.format("settings.%s.color", trace),
					palette.nextColor());
			String style = getProperty(
					String.format("settings.%s.style", trace), "");

			if (name == null)
				name = trace;

			if (data == null) {
				App.error("No data for trace '" + name + "'");
				continue;
			}

			File traceData = new File(data);

			try {
				Trace t = Trace.load(this, name, traceData);
				t.setColor(color);
				t.setCustomStyle(style);

				this.traces.add(t);
			} catch (IOException e1) {
				error(e1.getMessage());
			}
		}
	}

	protected void processArgs(String... args) {
		if (args == null)
			return;

		for (String arg : args) {
			if (arg.matches("^--settings=.*")) {
				String settings = arg.substring(11);

				if (settings.charAt(0) == '"')
					settings = settings.substring(1, settings.length() - 1);

				try {
					loadProperties(settings);
				} catch (FileNotFoundException e) {
					System.err.printf("Settings file \"%s\" not found\n",
							settings);
					System.exit(1);
				}
			}
		}
	}

	protected void processTerminated() {
		Runnable r = new Runnable() {
			public void run() {
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
		
		if (views.size() == 0) {
			System.exit(0);
		}

		r = new Runnable() {
			public void run() {
				showViews();
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

		try {
			BufferedImage icon = ImageIO.read(App.class
					.getResourceAsStream("data/icon.png"));

			frame.setIconImage(icon);
		} catch (IOException e) {
		}

		frame.setVisible(true);
	}

	class ProcessWorker implements Runnable {
		public void run() {
			LinkedList<Process> process = new LinkedList<Process>();

			for (String p : App.this.process)
				process.add(buildProcess(p));

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
		App app = new App(args);
		SwingUtilities.invokeLater(app);
	}
}
