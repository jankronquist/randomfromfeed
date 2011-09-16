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
		PrintWriter writer = response.getWriter();
		
		if (request.getParameterMap().isEmpty()) {
			writer.println("Parameters:");
			writer.println(" url = the URL of the feed");
			writer.println(" [entryPath] = xpath of the entries to randomly select between");
			writer.println(" [linkPath] = xpath of the link of the entry that will be redirected to");
			writer.println(" [nlast] = limit the selection to this number of entries");
			writer.println(" [prefernew] = 0.0 to evenly select between all entries, 1.0 to only select the latest entry");
			return;
		}

		String url = request.getParameter("url");
		String entryPath = getParameter(request, "entryPath", "/rss/channel/item");
		String linkPath = getParameter(request, "linkPath", "enclosure/@url");
		
		int nlatest = getIntParameter(request, "nlast", Integer.MAX_VALUE);
		double preferNew = getDoubleParameter(request, "prefernew", 0);
		if (preferNew < 0.0 || preferNew > 1.0) {
			writer.println("prefernew needs to be greater than or equal to zero (equal preference) up to a maximum of 1.0 (always pick the last picture).");
			return;
		}

		try {
			String result = getRandomLink(url, entryPath, linkPath, nlatest, preferNew);
			response.sendRedirect(result);
		} catch (Exception e) {
			writer.println(url);
			writer.println("Error: " + e.getMessage());
			e.printStackTrace(writer);
		}
	}

	private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
		return Integer.parseInt(getParameter(request, name, "" + defaultValue));
	}

	private double getDoubleParameter(HttpServletRequest request, String name, double defaultValue) {
		return Double.parseDouble(getParameter(request, name, "" + defaultValue));
	}

	private String getParameter(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public static void main(String[] args) throws Exception {
		do {
			System.out.println(getRandomLink(Thread.currentThread().getContextClassLoader().getResourceAsStream("picasa.xml"), "/rss/channel/item", "enclosure/@url", Integer.MAX_VALUE, 0.9));
		} while (true);
	}

	private static String getRandomLink(String feedUrl, String entryPath, String linkPath, int nlatest, double preferNew) throws Exception {
		InputStream inputStream = new URL(feedUrl).openStream();
		return getRandomLink(inputStream, entryPath, linkPath, nlatest, preferNew);
	}

	private static String getRandomLink(InputStream inputStream, String entryPath, String linkPath, int nlatest, double preferNew) throws IOException {
		try {
			JXPathContext context = JXPathContext.newContext(new DOMParser().parseXML(inputStream));
			Object value = context.getValue("count("+ entryPath + ")");
			int count = (int) Double.parseDouble(value.toString());
			int random = selectRandom(count, nlatest, preferNew) + 1;
			Pointer pointer = context.createPath(entryPath + "[" + random + "]");
			JXPathContext item = JXPathContext.newContext(pointer.getNode());
			return (String) item.getValue(linkPath);
		} finally {
			inputStream.close();
		}
	}

	private static int selectRandom(int count, int nlatest, double preferNew) {
		int nrEntriesSelection = Math.min(nlatest, count);
		double dblSelection = Math.pow(Math.random(), 1.0 - preferNew);
		int i = (int) Math.floor(nrEntriesSelection * dblSelection);
		if (i == nrEntriesSelection) {	// really just in case preferNew = 1.0
			i = nrEntriesSelection-1;
		}
		return (count-nrEntriesSelection) + i;
	}
}
