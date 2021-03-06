/*
 * This file is a part of a project under the terms of the GPL3.
 * You can find these terms in the COPYING file distributed with the project.
 * 
 *  Copyright 2013 Guilhelm Savin
 */
package csss2013.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.graphstream.graph.Node;

import csss2013.App;
import csss2013.Trace;
import csss2013.TraceView;
import csss2013.annotation.Title;

@Title("Google Maps")
public class GoogleMapsView implements TraceView {
	/*
	 * (non-Javadoc)
	 * 
	 * @see csss2013.TraceView#build(csss2013.App)
	 */
	public JComponent build(App app) {
		return new Widget(app);
	}

	public static BufferedImage get(String targetURL, String urlParameters) {
		URL url;
		HttpURLConnection connection = null;
		BufferedImage gmapsImg = null;

		try {
			url = new URL(String.format("%s?%s", targetURL, urlParameters));
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			InputStream is = connection.getInputStream();
			gmapsImg = ImageIO.read(is);
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}

		return gmapsImg;
	}

	public static BufferedImage post(String targetURL, String urlParameters) {
		URL url;
		HttpURLConnection connection = null;
		BufferedImage gmapsImg = null;

		try {
			byte[] body = urlParameters.getBytes("UTF-8");

			url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length",
					Integer.toString(body.length));

			// connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			OutputStream out = connection.getOutputStream();
			out.write(body);
			out.flush();
			out.close();

			InputStream is = connection.getInputStream();
			gmapsImg = ImageIO.read(is);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}

		return gmapsImg;
	}

	public static String getGoogleMapsPath(Trace trace) {
		StringBuilder buffer = new StringBuilder("color:0x");
		buffer.append(trace.getColor().substring(1)).append("EE|weight:2");

		Node start = null;
		for (Node n : trace) {
			if (n.getOutDegree() == 0) {
				start = n;
				break;
			}
		}

		do {
			buffer.append("|");
			buffer.append(start.getNumber("lat"));
			buffer.append(",");
			buffer.append(start.getNumber("lon"));

			if (start.getInDegree() > 0)
				start = start.getEnteringEdge(0).getOpposite(start);
			else
				start = null;
		} while (start != null);

		try {
			return "path=" + URLEncoder.encode(buffer.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return "path=" + buffer.toString();
		}
	}

	public class Widget extends JLabel {
		private static final long serialVersionUID = 3204960321557116362L;

		public Widget(App app) {
			this(512, 512, app);
		}

		public Widget(int width, int height, App app) {
			String url = "https://maps.googleapis.com/maps/api/staticmap";
			StringBuilder buffer = new StringBuilder();

			buffer.append("size=");
			buffer.append(width).append("x").append(height);
			buffer.append("&sensor=false&maptype=hybrid");

			for (int idx = 0; idx < app.getTraceCount(); idx++)
				buffer.append("&").append(getGoogleMapsPath(app.getTrace(idx)));

			BufferedImage img;

			if (url.length() + buffer.length() < 2048)
				img = get(url, buffer.toString());
			else {

				// POST is sadly not support by Google Maps :(
				// img = post(url, buffer.toString());

				img = null;
				App.error("Too many points for Google Maps ...");
			}

			if (img != null) {
				ImageIcon icon = new ImageIcon(img);
				setIcon(icon);
			} else {
				setText("Unavailable");
			}

			setVerticalAlignment(JLabel.CENTER);
			setHorizontalAlignment(JLabel.CENTER);
		}
	}
}
