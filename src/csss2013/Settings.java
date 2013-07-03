/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class Settings extends LinkedList<Settings.TraceEntry> {
	private static final long serialVersionUID = 8775503918062035122L;

	public static class TraceEntry {
		String name;
		File data;
		String color;
		String style;

		public String toString() {
			return String.format("%s [%s:%s:%s]", name, data.getName(), color,
					style);
		}
	}

	HashSet<TraceView.Type> viewTypes;

	public Settings() {
		viewTypes = new HashSet<TraceView.Type>();
		viewTypes.add(TraceView.Type.STATIC);
		viewTypes.add(TraceView.Type.DYNAMIC);
	}

	public void setViews(Collection<TraceView.Type> types) {
		viewTypes.clear();
		viewTypes.addAll(types);
	}
	
	public void addTrace(String name, File data, String color,
			String additionnalStyle) {
		TraceEntry te = new TraceEntry();
		te.name = name;
		te.data = data;
		te.color = color;
		te.style = additionnalStyle;

		add(te);
	}
}
