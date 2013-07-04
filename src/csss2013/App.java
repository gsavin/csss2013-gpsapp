/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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

public class App implements Runnable {
	private final static HashMap<String, Class<? extends TraceView>> registeredViews = new HashMap<String, Class<? extends TraceView>>();
	private final static HashMap<String, Class<? extends Process>> registeredProcess = new HashMap<String, Class<? extends Process>>();

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
		App.registerView("Google Maps", GoogleMapsView.class);

		App.registerProcess("normalize", NormalizeXYZ.class);
		App.registerProcess("reload", Reload.class);
		App.registerProcess("merge", Merge.class);
	}

	Trace[] traces;
	final LinkedList<String> views;
	final LinkedList<Process> process;
	final HashMap<String, Object> data;
	JFrame progressDialog;

	public App() {
		this.traces = null;
		this.process = new LinkedList<Process>();
		this.views = new LinkedList<String>();
		this.data = new HashMap<String, Object>();
	}

	public void run() {
		SettingsWizard.launch(this);
	}

	public void setViews(Collection<String> types) {
		views.addAll(types);
	}

	public JComponent buildView(String name) {
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

	public Process buildProcess(String name) {
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
		setViews(settings.viewTypes);

		process.clear();

		for (String pname : settings.processTypes)
			process.add(buildProcess(pname));

		launchProcess();
	}

	protected void processTerminated() {
		Runnable r = new Runnable() {
			public void run() {
				progressDialog.setVisible(false);
				progressDialog = null;

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

	public void launchProcess() {
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

	public void showViews() {
		JTabbedPane tabs = new JTabbedPane();

		tabs.setPreferredSize(new Dimension(640, 640));

		for (String type : views) {
			JComponent comp = buildView(type);
			tabs.addTab(getViewTitle(type), comp);
		}

		JFrame frame = new JFrame("CSSS2013 : GPS App");
		frame.add(tabs);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public static void error(Throwable e) {
		error(e.getMessage());
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
