package csss2013.process;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.graphstream.stream.Timeline;
import org.graphstream.stream.file.FileSinkDGS;

import csss2013.App;
import csss2013.Process;
import csss2013.PropertyKeys;
import csss2013.annotation.Title;

@Title("DGS Output")
public class OutputDGS implements PropertyKeys, Process {
	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#getPriority()
	 */
	public int getPriority() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.Process#process(csss2013.App)
	 */
	public void process(App app) {
		Timeline timeline = (Timeline) app.getData(Reload.TIMELINE_DATA_NAME);
		PrintStream out = System.out;
		FileSinkDGS dgs = new FileSinkDGS();

		if (app.hasProperty(PROCESS_OUTPUT_DGS_PATH)) {
			File f = new File(app.getProperty(PROCESS_OUTPUT_DGS_PATH));

			try {
				f.getParentFile().mkdirs();
			} catch (Exception e) {

			}

			try {
				out = new PrintStream(f);
			} catch (FileNotFoundException e) {
				App.error(e);
				return;
			}
		}

		try {
			dgs.begin(out);
		} catch (IOException e) {
			App.error(e);
			return;
		}

		timeline.addSink(dgs);
		timeline.seekStart();

		while (timeline.hasNext())
			timeline.next();

		timeline.removeSink(dgs);

		try {
			dgs.end();
		} catch (IOException e) {
			App.error(e);
		}

		out.flush();

		if (out != System.out)
			out.close();
	}
}
