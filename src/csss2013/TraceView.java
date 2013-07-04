/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

import javax.swing.JComponent;

/**
 * Defines a new view of the app.
 * 
 * The view has to be registered using
 * {@link csss2013.App#registerView(String, Class)}. You can add the annotation
 * {@link csss2013.annotation.Default} to define the view as a default, and the
 * annotation {@link csss2013.annotation.Title} to set the title of this view.
 * 
 * @author Guilhelm Savin
 * 
 */
public interface TraceView {
	/**
	 * Build a new view of this type. Just have to return a JComponent.
	 * 
	 * @param app
	 *            the current app
	 * @return a component defining the view
	 */
	JComponent build(App app);
}
