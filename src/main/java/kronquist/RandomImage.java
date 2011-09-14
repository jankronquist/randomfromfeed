package kronquist;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Get redirect to random image from a PicasaWeb RSS feed. 
 */
public class RandomImage extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String url = request.getParameter("url");
		PrintWriter writer = response.getWriter();
		try {
			String result = getRandomImage(url);
			response.sendRedirect(result);
		} catch (Exception e) {
			writer.println(url);
			writer.println("Error: " + e.getMessage());
			e.printStackTrace(writer);
		}
	}

	public static void main(String[] args) throws Exception {
		String randomImage = getRandomImage(args[0]);
		System.out.println(randomImage);
	}

	private static String getRandomImage(String feedUrl) throws Exception {
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));

		List<SyndEntry> entries = feed.getEntries();
		int i = (int) (entries.size() * Math.random());
		SyndEntry entry = entries.get(i);
		SyndEnclosure enclosure = (SyndEnclosure) entry.getEnclosures().get(0);
		return enclosure.getUrl();
	}
}
