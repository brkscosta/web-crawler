package com.brkscosta.webcrawler.domain;

import com.brkscosta.webcrawler.app.utils.Logger;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.data.repositories.CrawlerRepository;
import com.brkscosta.webcrawler.data.repositories.WebPageRepository;
import com.brkscosta.webcrawler.data.utils.WePageUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BreadthSearch implements SearchCriteria {
    private final int numberOfPages;
    private final Logger logger;
    private final WebPageRepository webPageRepository;
    private final CrawlerRepository crawlerRepository;
    private static final int TIMEOUT = 10;

    private int pagesAddedCount = 0;

    public BreadthSearch(Logger logger, WebPageRepository webPageRepository, CrawlerRepository crawlerRepository, int numberOfPages) {
        this.crawlerRepository = crawlerRepository;
        this.numberOfPages = numberOfPages;
        this.logger = logger;
        this.webPageRepository = webPageRepository;
    }

    @Override
    public CompletableFuture<Void> search(WebPage rootPage) {
        Queue<WebPage> pagesToVisit = new LinkedList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        this.crawlerRepository.clear();
        this.crawlerRepository.insertPage(rootPage);
        pagesToVisit.add(rootPage);
        pagesAddedCount++;

        return CompletableFuture.runAsync(() -> {
            while (!pagesToVisit.isEmpty() && pagesAddedCount < this.numberOfPages) {
                WebPage currentPage = pagesToVisit.poll();

                List<Link> links = this.webPageRepository.fetchIncidentLinks(currentPage, this.numberOfPages).join();
                this.logger.writeToLog("Fetched links for page: " + currentPage.getUrl() + " with " + links.size() + " links");

                this.processIncidentLinks(currentPage, links, pagesToVisit);
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

    private void processIncidentLinks(WebPage currentPage, List<Link> incidentLinks, Queue<WebPage> pagesToVisit) {
        int incidentLinksAdded = 0;

        if (incidentLinks.isEmpty()) {
            return;
        }

        for (Link link : incidentLinks) {
            if (pagesAddedCount >= this.numberOfPages) {
                this.logger.writeToLog("Page limit reached. Stopping search.");
                break;
            }

            WebPage existingPage = this.crawlerRepository.searchForPageByLinkIfExists(link.getUrl());
            if (existingPage != null) {
                this.crawlerRepository.makeLinkBetweenPages(currentPage, existingPage, link);
            } else {
                WebPage newPage = WePageUtils.createWebPageFromAnother(currentPage, link);
                this.addPageToGraph(currentPage, newPage, link, incidentLinksAdded, incidentLinks.size());
                pagesToVisit.add(newPage);
            }
        }
        this.logger.writeToLog("Incident links added: " + incidentLinksAdded);
    }

    private void addPageToGraph(WebPage currentPage, WebPage newPage, Link link, int linksAdded, int totalLinks) {
        if (currentPage.getIsLastOfALevel() && linksAdded == totalLinks - 1) {
            newPage.setIsLastOfALevel(true);
        }

        this.crawlerRepository.insertPage(newPage);
        pagesAddedCount++;
        this.crawlerRepository.makeLinkBetweenPages(currentPage, newPage, link);
    }
}
