package kronquist;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.xml.DOMParser;

/**
 * Get redirect to random image from a PicasaWeb RSS feed. 
 */
public class RandomImage extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String url = request.getParameter("url");
		String entryPath = getParameter(request, "entryPath", "/rss/channel/item");
		String linkPath = getParameter(request, "linkPath", "enclosure/@url");
		PrintWriter writer = response.getWriter();
		try {
			String result = getRandomLink(url, entryPath, linkPath);
			response.sendRedirect(result);
		} catch (Exception e) {
			writer.println(url);
			writer.println("Error: " + e.getMessage());
			e.printStackTrace(writer);
		}
	}

	private String getParameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(getRandomLink(Thread.currentThread().getContextClassLoader().getResourceAsStream("picasa.xml"), "/rss/channel/item", "enclosure/@url"));
	}

	private static String getRandomLink(String feedUrl, String entryPath, String linkPath) throws Exception {
		InputStream inputStream = new URL(feedUrl).openStream();
		return getRandomLink(inputStream, entryPath, linkPath);
	}

	private static String getRandomLink(InputStream inputStream, String entryPath, String linkPath) throws IOException {
		try {
			JXPathContext context = JXPathContext.newContext(new DOMParser().parseXML(inputStream));
			Object value = context.getValue("count("+ entryPath + ")");
			int count = (int) Double.parseDouble(value.toString());
			int random = (int) (Math.random()*count) + 1;
			Pointer pointer = context.createPath(entryPath + "[" + random + "]");
			JXPathContext item = JXPathContext.newContext(pointer.getNode());
			return (String) item.getValue(linkPath);
		} finally {
			inputStream.close();
		}
	}
}
