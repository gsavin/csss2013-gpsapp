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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.EventObject;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import csss2013.util.Palette;

public class Settings extends JPanel {
	private static final long serialVersionUID = 8775503918062035122L;

	Palette palette = new Palette();
	TraceFileModel model;

	public Settings() {
		model = new TraceFileModel();
		JTable entries = new JTable(model);

		entries.setFillsViewportHeight(true);
		entries.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		entries.setDefaultRenderer(Color.class, new ColorRenderer());
		entries.setDefaultEditor(Color.class, new ColorEditor());
		entries.setDefaultRenderer(File.class, new FileRenderer());
		entries.setDefaultEditor(File.class, new FileEditor());

		JScrollPane tableContainer = new JScrollPane(entries);

		JPanel topButtons = new JPanel();
		topButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
		topButtons.add(new JButton(new AddTraceAction()));
		topButtons.add(new JButton(new LoadAction()));

		setLayout(new BorderLayout());
		add(topButtons, BorderLayout.NORTH);
		add(tableContainer, BorderLayout.CENTER);
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
					Settings.this.model.addEntry(t);
			}
		}
	}

	class LoadAction extends AbstractAction {
		private static final long serialVersionUID = -755694385989411611L;

		public LoadAction() {
			super("Load settings");
		}

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub

		}
	}

	protected static File[] selectGPXFiles() {
		JFileChooser fileChooser = new JFileChooser(".");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"GPX traces", "gpx");

		fileChooser.setFileFilter(filter);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.showOpenDialog(null);

		File[] files = fileChooser.getSelectedFiles();
		return files;
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
			Object[] entry = { defaultName, f, color, style };

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

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class<?> getColumnClass(int c) {
			return columnClass[c];
		}

		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 1) {
				return false;
			} else {
				return true;
			}
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
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
