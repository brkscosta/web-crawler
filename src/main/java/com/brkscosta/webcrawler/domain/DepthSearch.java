package com.brkscosta.webcrawler.domain;

import com.brkscosta.webcrawler.app.utils.Logger;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.data.repositories.CrawlerRepository;
import com.brkscosta.webcrawler.data.repositories.WebPageRepository;
import com.brkscosta.webcrawler.data.utils.WePageUtils;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DepthSearch implements SearchCriteria {
    private final Logger logger;
    private final WebPageRepository webPageRepository;
    private final CrawlerRepository crawlerRepository;
    private final int depthLimit;
    private static final int TIMEOUT = 10;

    public DepthSearch(Logger logger, WebPageRepository webPageRepository,
                       CrawlerRepository crawlerRepository, int depthLimit) {
        this.logger = logger;
        this.webPageRepository = webPageRepository;
        this.crawlerRepository = crawlerRepository;
        this.depthLimit = depthLimit;
    }

    @Override
    public CompletableFuture<Void> search(WebPage rootPage) {
        Stack<WebPage> pagesToVisit = new Stack<>();
        pagesToVisit.push(rootPage);
        this.crawlerRepository.clear();
        this.crawlerRepository.insertPage(rootPage);

        return CompletableFuture.runAsync(() -> {
            while (!pagesToVisit.isEmpty()) {
                WebPage currentPage = pagesToVisit.pop();

                if (currentPage.getDepth() > depthLimit) {
                    continue;
                }

                this.logger.writeToLog("Visiting page: " + currentPage.getUrl());

                List<Link> links = this.webPageRepository.fetchIncidentLinks(currentPage, this.depthLimit).join();
                this.logger.writeToLog("Fetched links for page: " + currentPage.getUrl() + " with " + links.size() + " links");

                this.processLinks(currentPage, links, pagesToVisit);
            }
        }).orTimeout(TIMEOUT, TimeUnit.SECONDS).exceptionally(ex -> {
            if (ex instanceof TimeoutException) {
                this.logger.writeToLog("Search timed out");
                return null;
            }
            this.logger.writeToLog(ex.getMessage());
            return null;
        }).thenAccept((param) -> {
            this.logger.writeToLog("Search Finished");
        });
    }

    private void processLinks(WebPage currentPage, List<Link> links, Stack<WebPage> pagesToVisit) {
        int incidentLinksAdded = 0;
        for (Link link : links) {
            WebPage existingPage = this.crawlerRepository.searchForPageByLinkIfExists(link.getUrl());
            if (existingPage != null) {
                this.crawlerRepository.makeLinkBetweenPages(currentPage, existingPage, link);
            } else {
                WebPage newPage = WePageUtils.createWebPageFromAnother(currentPage, link);
                if (currentPage.getIsLastOfALevel() && incidentLinksAdded == links.size() - 1) {
                    newPage.setIsLastOfALevel(true);
                }
                this.crawlerRepository.insertPage(newPage);
                this.crawlerRepository.makeLinkBetweenPages(currentPage, newPage, link);
                pagesToVisit.push(newPage);
                this.logger.writeToLog("New page inserted: " + newPage.getUrl());
            }
        }
    }
}
