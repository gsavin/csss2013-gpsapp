/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import csss2013.util.Palette;

public class SettingsWizard extends JPanel {
	private static final long serialVersionUID = 8775503918062035122L;

	public static void launch(App app) {
		if (!SwingUtilities.isEventDispatchThread())
			System.err.printf("Wizard should be launched in swing thread !\n");

		SettingsWizard wizard = new SettingsWizard(app);
		wizard.dialog.setVisible(true);
	}

	JFrame dialog;
	Palette palette;
	TraceFileModel model;
	App app;
	HashSet<String> viewTypes;
	HashSet<String> processTypes;
	Properties properties;
	JPanel checkboxesView, checkboxesProcess;

	public SettingsWizard(App app) {
		this.app = app;
		this.dialog = new JFrame("CSSS2013 - GPS App : Setup");
		this.model = new TraceFileModel();
		this.viewTypes = new HashSet<String>();
		this.processTypes = new HashSet<String>();
		this.palette = new Palette();
		this.properties = null;

		JTable entries = new JTable(model);

		entries.setFillsViewportHeight(true);
		entries.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		entries.setDefaultRenderer(Color.class, new ColorRenderer());
		entries.setDefaultEditor(Color.class, new ColorEditor());
		entries.setDefaultRenderer(File.class, new FileRenderer());
		entries.setDefaultEditor(File.class, new FileEditor());

		JScrollPane tableContainer = new JScrollPane(entries);

		checkboxesView = new JPanel();
		viewTypes.addAll(App.getDefaultViewName());
		loadViewsCheckboxes();

		checkboxesProcess = new JPanel();
		processTypes.addAll(App.getDefaultProcessName());
		loadProcessCheckboxes();

		JPanel checkboxes = new JPanel();
		checkboxes.setLayout(new GridLayout(2, 1));
		checkboxes.add(checkboxesView);
		checkboxes.add(checkboxesProcess);

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		center.add(tableContainer, BorderLayout.CENTER);
		center.add(checkboxes, BorderLayout.SOUTH);

		JPanel topButtons = new JPanel();
		topButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
		topButtons.add(new JButton(new AddTraceAction()));
		topButtons.add(new JButton(new LoadAction()));

		JPanel bottomButtons = new JPanel();
		bottomButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		bottomButtons.add(new JButton(new SetupAction()));
		bottomButtons.add(new JButton(new CancelAction()));

		setLayout(new BorderLayout());
		add(topButtons, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottomButtons, BorderLayout.SOUTH);

		dialog.setLayout(new BorderLayout());
		dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dialog.add(this, BorderLayout.CENTER);
		dialog.pack();
	}

	protected void loadViewsCheckboxes() {
		checkboxesView.removeAll();
		checkboxesView.revalidate();
		checkboxesView.setLayout(new FlowLayout(FlowLayout.LEFT));
		checkboxesView.add(new JLabel("Views:"));

		for (String name : App.getRegisteredViewName()) {
			ViewTypeAction a = new ViewTypeAction(name);
			JCheckBox box = new JCheckBox(a);

			checkboxesView.add(box);

			if (viewTypes.contains(name))
				box.setSelected(true);
		}

		dialog.pack();
	}

	protected void loadProcessCheckboxes() {
		checkboxesProcess.removeAll();
		checkboxesProcess.revalidate();
		checkboxesProcess.setLayout(new FlowLayout(FlowLayout.LEFT));
		checkboxesProcess.add(new JLabel("Process:"));

		for (String name : App.getRegisteredProcessName()) {
			ProcessTypeAction a = new ProcessTypeAction(name);
			JCheckBox box = new JCheckBox(a);

			checkboxesProcess.add(box);

			if (processTypes.contains(name))
				box.setSelected(true);
		}

		dialog.pack();
	}

	class AddTraceAction extends AbstractAction {
		private static final long serialVersionUID = -5390199887054718055L;

		public AddTraceAction() {
			super("Add Trace");
		}

		public void actionPerformed(ActionEvent arg0) {
			File[] traces = selectGPXFiles();

			if (traces != null) {
				for (File t : traces)
					SettingsWizard.this.model.addEntry(t);
			}
		}
	}

	class LoadAction extends AbstractAction {
		private static final long serialVersionUID = -755694385989411611L;

		public LoadAction() {
			super("Load settings");
		}

		public void actionPerformed(ActionEvent arg0) {
			File[] prop = selectFiles("XML Configuration", "xml", false);

			if (prop == null)
				return;

			Properties settings = null;

			try {
				settings = App.loadProperties(prop[0].getPath());
			} catch (FileNotFoundException e) {
				App.error(e);
				return;
			}

			String proc = settings.getProperty("settings.process");
			processTypes.clear();

			if (proc != null) {
				String[] process = proc.split("\\s*,\\s*");

				for (String p : process) {
					String tryClass = settings.getProperty(String.format(
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

					processTypes.add(p);
				}
			} else
				processTypes.addAll(App.getDefaultProcessName());

			loadProcessCheckboxes();

			String vs = settings.getProperty("settings.views");
			viewTypes.clear();

			if (vs != null) {
				String[] views = vs.split("\\s*,\\s*");

				for (String v : views) {
					String tryClass = settings.getProperty(String.format(
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

					viewTypes.add(v);
				}
			} else
				viewTypes.addAll(App.getDefaultViewName());

			loadViewsCheckboxes();

			String[] traces = settings.getProperty("settings.traces", "")
					.split("\\s*,\\s*");

			for (String trace : traces) {
				String name = settings.getProperty(String.format(
						"settings.%s.name", trace));
				String data = settings.getProperty(String.format(
						"settings.%s.data", trace));
				String color = settings.getProperty(
						String.format("settings.%s.color", trace),
						palette.nextColor());
				String style = settings.getProperty(
						String.format("settings.%s.style", trace), "");

				if (name == null)
					name = trace;

				if (data == null) {
					App.error("No data for trace " + name);
					continue;
				}

				File path = new File(data);
				Color theColor = Color.decode(color);

				model.addEntry(name, path, theColor, style);
			}

			properties = settings;
		}
	}

	class SetupAction extends AbstractAction {
		private static final long serialVersionUID = 7346203583950182224L;

		public SetupAction() {
			super("Launch");
		}

		public void actionPerformed(ActionEvent arg0) {
			if (model.getRowCount() == 0) {
				App.error("No GPX trace selected");
				return;
			}

			Settings settings = model.getSettings();
			dialog.setVisible(false);
			app.wizardCompleted(settings);
		}
	}

	class CancelAction extends AbstractAction {
		private static final long serialVersionUID = -207059075603902183L;

		public CancelAction() {
			super("Cancel");
		}

		public void actionPerformed(ActionEvent arg0) {
			SettingsWizard.this.dialog.setVisible(false);
			System.exit(0);
		}
	}

	class ViewTypeAction extends AbstractAction {
		private static final long serialVersionUID = 8253837349628000598L;

		String type;

		public ViewTypeAction(String type) {
			super(App.getViewTitle(type));
			this.type = type;
		}

		public void actionPerformed(ActionEvent arg0) {
			Object src = arg0.getSource();

			if (src instanceof JCheckBox) {
				JCheckBox box = (JCheckBox) src;

				if (box.isSelected() && !viewTypes.contains(type))
					viewTypes.add(type);
				else if (!box.isSelected() && viewTypes.contains(type))
					viewTypes.remove(type);
			} else {
				System.err.printf("Not a checkbox !\n");
			}
		}
	}

	class ProcessTypeAction extends AbstractAction {
		private static final long serialVersionUID = 8253837349628000598L;

		String type;

		public ProcessTypeAction(String type) {
			super(App.getProcessTitle(type));
			this.type = type;
		}

		public void actionPerformed(ActionEvent arg0) {
			Object src = arg0.getSource();

			if (src instanceof JCheckBox) {
				JCheckBox box = (JCheckBox) src;

				if (box.isSelected() && !processTypes.contains(type))
					processTypes.add(type);
				else if (!box.isSelected() && processTypes.contains(type))
					processTypes.remove(type);
			} else {
				System.err.printf("Not a checkbox !\n");
			}
		}
	}

	protected static File[] selectGPXFiles() {
		return selectFiles("GPX files", "gpx", true);
	}

	protected static File[] selectFiles(String desc, String ext,
			boolean multiple) {
		JFileChooser fileChooser = new JFileChooser(".");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(desc, ext);

		fileChooser.setFileFilter(filter);
		fileChooser.setMultiSelectionEnabled(multiple);
		int r = fileChooser.showOpenDialog(null);

		if (r == JFileChooser.APPROVE_OPTION) {
			File[] files;

			if (multiple)
				files = fileChooser.getSelectedFiles();
			else
				files = new File[] { fileChooser.getSelectedFile() };

			return files;
		}

		return null;
	}

	class TraceFileModel extends AbstractTableModel {
		private static final long serialVersionUID = -361891542238032350L;

		private String[] columnNames = { "Name", "File", "Color", "Style" };
		private Class<?>[] columnClass = { String.class, File.class,
				Color.class, String.class };

		private Object[][] data = {};

		public void addEntry(File f) {
			String defaultName = f.getName().replaceAll("[.]gpx$", "")
					.replaceAll("\\W", "_");

			Color color = Color.decode(palette.nextColor());
			String style = "";

			addEntry(defaultName, f, color, style);
		}

		public void addEntry(String name, File f, Color color, String style) {
			Object[] entry = { name, f, color, style };

			data = Arrays.copyOf(data, data.length + 1);
			data[data.length - 1] = entry;

			fireTableRowsInserted(data.length - 1, data.length - 1);
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			if (data.length > 0)
				return data[row][col];
			else
				return null;
		}

		public Class<?> getColumnClass(int c) {
			return columnClass[c];
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		public Settings getSettings() {
			Settings settings = new Settings();
			settings.setViews(viewTypes);
			settings.setProcess(processTypes);

			if (properties != null)
				settings.setProperties(properties);

			for (int i = 0; i < data.length; i++) {
				String name = (String) data[i][0];
				File traceData = (File) data[i][1];
				Color color = (Color) data[i][2];
				String style = (String) data[i][3];

				settings.addTrace(
						name,
						traceData,
						String.format("#%02X%02X%02X", color.getRed(),
								color.getGreen(), color.getBlue()), style);
			}

			return settings;
		}
	}

	class ColorRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 2463677655689322349L;

		public ColorRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Color newColor = (Color) color;
			setBackground(newColor);

			setToolTipText("RGB value: " + newColor.getRed() + ", "
					+ newColor.getGreen() + ", " + newColor.getBlue());

			return this;
		}
	}

	class FileRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 2463677655689322349L;

		public FileRenderer() {
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			File file = (File) value;

			setText(file.getName());
			setToolTipText(file.getAbsolutePath());

			return this;
		}
	}

	class ColorEditor extends AbstractCellEditor implements TableCellEditor,
			ActionListener {
		private static final long serialVersionUID = 1720601469372803349L;

		Color currentColor;
		JButton button;
		JColorChooser colorChooser;
		JDialog dialog;
		protected static final String EDIT = "edit";

		public ColorEditor() {
			button = new JButton();
			button.setActionCommand(EDIT);
			button.addActionListener(this);
			button.setBorderPainted(false);

			colorChooser = new JColorChooser();
			dialog = JColorChooser.createDialog(button, "Pick a Color", true,
					colorChooser, this, null);
		}

		/**
		 * Handles events from the editor button and from the dialog's OK
		 * button.
		 */
		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				button.setBackground(currentColor);
				colorChooser.setColor(currentColor);
				dialog.setVisible(true);

				fireEditingStopped();
			} else {
				currentColor = colorChooser.getColor();
			}
		}

		public Object getCellEditorValue() {
			return currentColor;
		}

		// Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			currentColor = (Color) value;
			return button;
		}
	}

	class FileEditor extends AbstractCellEditor implements TableCellEditor,
			ActionListener {
		private static final long serialVersionUID = 1720601469372803349L;

		File currentFile;
		JButton button;
		JFileChooser chooser;
		protected static final String EDIT = "edit";

		public FileEditor() {
			button = new JButton();
			button.setActionCommand(EDIT);
			button.addActionListener(this);
			button.setBorderPainted(false);

			chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"GPX traces", "gpx");
			chooser.setFileFilter(filter);
		}

		/**
		 * Handles events from the editor button and from the dialog's OK
		 * button.
		 */
		public void actionPerformed(ActionEvent e) {
			button.setText(currentFile.getName());
			chooser.setCurrentDirectory(currentFile.getParentFile());

			int r = chooser.showOpenDialog(null);

			if (r == JFileChooser.APPROVE_OPTION)
				currentFile = chooser.getSelectedFile();
			fireEditingStopped();
		}

		public Object getCellEditorValue() {
			return currentFile;
		}

		// Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			currentFile = (File) value;
			return button;
		}
	}
}
