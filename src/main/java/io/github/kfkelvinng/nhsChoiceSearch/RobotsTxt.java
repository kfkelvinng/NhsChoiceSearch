package io.github.kfkelvinng.nhsChoiceSearch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class RobotsTxt {
	
	private static Logger logger = LoggerFactory.getLogger(RobotsTxt.class);
	
	private String sitemapUrl;
	private LinkedList<Pattern> disallows = new LinkedList<>();
	
	private LinkedList<String> allowedPages = new LinkedList<>();
	
	
	RobotsTxt(WebClient webClient, String robotsTxtUrl) throws IOException {
		try(Scanner sc = new Scanner(webClient.getPage(robotsTxtUrl).getWebResponse().getContentAsStream())) {
			sc.findWithinHorizon("\\QUser-agent: *\\E", 0);
			Pattern line = Pattern.compile("([^:]+): (.+)");
			while (sc.hasNextLine()) {
				Matcher m = line.matcher(sc.nextLine());
				if(m.matches()) {					
					switch(m.group(1)) {
					case "Disallow":
						String[] disallow = m.group(2).split(Pattern.quote("*"));
						StringBuilder disallowPattern = new StringBuilder("https?://[^/]*");
						for (String urlPart : disallow) {
							disallowPattern.append(Pattern.quote(urlPart)).append(".*");
						}
						disallows.add(Pattern.compile(disallowPattern.toString()));
						logger.debug(disallowPattern.toString());
						break;
					case "Sitemap":
						sitemapUrl = m.group(2);
						break;
					}
				}
				
			}

			XmlPage sitemap = webClient.getPage(sitemapUrl);
			for (Object de : sitemap.getByXPath("//*[name()='url']/*[name()='loc']")) {
				String urlLoc = ((DomElement)de).getTextContent();
				if (isAllow(urlLoc)) {
					allowedPages.add(urlLoc);
				}
			}
			logger.info("Allowed Pages: {}", allowedPages.size());
		}
	}
	
	public String getSitemapUrl() {
		return sitemapUrl;
	}
	
	public LinkedList<String> getAllowedPages() {
		return allowedPages;
	}
	
	private boolean isAllow(String url) {
		for (Pattern disallow : disallows) {
			if (disallow.matcher(url).matches()) {
				return false;
			}
		}
		return true;
	}

}
