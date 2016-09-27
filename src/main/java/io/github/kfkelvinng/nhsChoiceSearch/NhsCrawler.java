package io.github.kfkelvinng.nhsChoiceSearch;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.*;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * NHS Crawler for http://www.nhs.uk/Conditions/* pages
 */
@Component
public class NhsCrawler {
	
	private static Logger logger = LoggerFactory.getLogger(NhsCrawler.class);
	
	private static long BEING_NICE_INTERVAL = 2000;
	
	private NhsEs nhsEs;
	
	private WebClient webClient;
	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	
	
	@Inject
	public NhsCrawler(NhsEs nhsEs) {
		this.nhsEs = nhsEs;
	}
	
	public void crawl() {
		try {
			RobotsTxt robot = new RobotsTxt(webClient, "http://www.nhs.uk/robots.txt");
			
			for (String page : robot.getAllowedPages()) {
				if (page.startsWith("http://www.nhs.uk/Conditions")) {
					HtmlPage conditionPage = webClient.getPage(page);
					nhsEs.upsert(new NhsCondition.Builder()
							.setUrl(page)
							.setTitle(conditionPage.getTitleText())
							.setContent(conditionPage.asText())
							.build()
							);
					Thread.sleep(BEING_NICE_INTERVAL);
				}
			}
			
			
		} catch (Exception e) {
			logger.error("Fail to crawl", e);
		}
	}

	@PostConstruct
	public void start() {
		webClient = new WebClient();
	    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
	    webClient.getOptions().setThrowExceptionOnScriptError(false);
		ses.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				crawl();
			}
		}, 0, 1, TimeUnit.DAYS);
	}
	
	@PreDestroy
	public void stop() {
		ses.shutdown();
		webClient.close();
	}
	
}
