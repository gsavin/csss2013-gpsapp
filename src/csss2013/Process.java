/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013;

/**
 * Define something that will act on the traces.
 * 
 * Process can produce data that can be stored with
 * {@link csss2013.App#setData(String, Object)} and retrieve with
 * {@link csss2013.App#getData(String)}.
 * 
 * @author Guilhelm Savin
 * 
 */
public interface Process {
	/**
	 * Define the priority of the process. The order process are executed is
	 * defined by this priority. Process with higher priority will be executed
	 * first. It is important if your process produce some data that will be
	 * used by an other process.
	 * 
	 * Consider that priority is from 0 to 100.
	 * 
	 * @return process priority
	 */
	int getPriority();

	/**
	 * Execute the process.
	 * 
	 * @param app
	 *            the current app from which the process is executed.
	 */
	void process(App app);
}
