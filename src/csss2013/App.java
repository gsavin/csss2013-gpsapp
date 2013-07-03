/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.GraphiteGlassSkin;

import csss2013.TraceView.Type;
import csss2013.process.NormalizeXYZ;
import csss2013.process.Reload;

public class App implements Runnable {
	public static final String DEFAULT_STYLESHEET = "node { size:10px;  }";

	public static class Config {
		HashMap<String, String> dataBateau;
		HashMap<String, String> dataBouees;
		HashMap<String, String> dataZodiac;
	}

	Trace[] traces;
	final LinkedList<TraceView.Type> views;
	final LinkedList<Process> process;
	final HashMap<String, Object> data;

	public App() {
		this.traces = null;
		this.process = new LinkedList<Process>();
		this.views = new LinkedList<TraceView.Type>();
		this.data = new HashMap<String, Object>();
	}

	public void run() {
		SettingsWizard.launch(this);
	}

	public void setViews(Collection<? extends TraceView.Type> types) {
		views.addAll(types);

		Collections.sort(views, new Comparator<TraceView.Type>() {
			public int compare(Type arg0, Type arg1) {
				if (arg0.priority < arg1.priority)
					return 1;
				else if (arg0.priority > arg1.priority)
					return -1;

				return 0;
			}
		});
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

		launchProcess();
	}

	protected void processTerminated() {
		Runnable r = new Runnable() {
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

	public void launchProcess() {
		Thread t = new Thread(new ProcessWorker(), "process");
		t.setDaemon(true);
		t.start();
	}

	public void showViews() {
		JTabbedPane tabs = new JTabbedPane();

		tabs.setPreferredSize(new Dimension(640, 640));

		for (TraceView.Type type : views) {
			try {
				JComponent comp = type.getTraceView(this);
				tabs.addTab(type.name, comp);
			} catch (Exception e) {
				error(e);
			}
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

	public static void main(String... args) throws IOException {
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

		App app = new App();
		app.enableProcess(new NormalizeXYZ());
		app.enableProcess(new Reload());

		SwingUtilities.invokeLater(app);
	}
}
