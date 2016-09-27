package io.github.kfkelvinng.nhsChoiceSearch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import com.google.common.collect.ImmutableList;

/**
 * Test RobotsTxt get the allowed url from sitemap
 */
public class RobotsTxtTest {
    
	String sitemapUrl = "http://www.nhs.uk/sitemap.xml";
	String s = 
			"User-agent: *\n" +
			"Disallow: /Conditions/*/*/News.aspx\n" +
			"Disallow: /Service-Search/\n"+
			"Sitemap: "+sitemapUrl;
	WebClient webClient = Mockito.mock(WebClient.class);
	String robotsTxtUrl = "http://www.nhs.uk/robots.txt";
	Page page = Mockito.mock(Page.class);
	XmlPage sitemap = Mockito.mock(XmlPage.class);
	WebResponse webResponse = Mockito.mock(WebResponse.class);
	
	@Before
	public void setup() throws Exception {
		Mockito.when(webClient.getPage(robotsTxtUrl)).thenReturn(page);
		Mockito.when(page.getWebResponse()).thenReturn(webResponse);
		Mockito.when(webResponse.getContentAsStream()).thenReturn(new ByteArrayInputStream(s.getBytes()));
		Mockito.when(webClient.getPage(sitemapUrl)).thenReturn(sitemap);
	}
	

	@Test
	public void testPrefix() throws IOException {
		
		setupSitemap("http://www.nhs.uk/Service-Search/disallow.html");

		
		List<String> allowed = new RobotsTxt(webClient, robotsTxtUrl).getAllowedPages();
		Assert.assertTrue(allowed.isEmpty());
		
	}
	
	@Test
	public void testWildcard() throws IOException {
		
		setupSitemap("http://www.nhs.uk/Conditions/disallow/disallow/News.aspx");

		
		List<String> allowed = new RobotsTxt(webClient, robotsTxtUrl).getAllowedPages();
		Assert.assertTrue(allowed.isEmpty());
		
	}
	
	@Test
	public void testAllow() throws IOException {
		
		setupSitemap("http://www.nhs.uk/Conditions/allow.html");
		List<String> allowed = new RobotsTxt(webClient, robotsTxtUrl).getAllowedPages();
		Assert.assertTrue(allowed.size()==1);
		Assert.assertTrue(allowed.get(0).equals("http://www.nhs.uk/Conditions/allow.html"));
		
	}
	
	private void setupSitemap(final String url) {
		Mockito.when(sitemap.getByXPath("//*[name()='url']/*[name()='loc']")).thenAnswer(
				new Answer<List<?>>() {
					@Override
					public List<?> answer(InvocationOnMock invocation) throws Throwable {
						return ImmutableList.of(
								Mockito.when(Mockito.mock(DomElement.class).getTextContent()).thenReturn(url).getMock()
						);
					}
				}
		);
	}
	
}
