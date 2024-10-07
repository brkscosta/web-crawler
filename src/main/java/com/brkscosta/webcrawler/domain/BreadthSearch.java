package com.brkscosta.webcrawler.domain;

import com.brkscosta.webcrawler.data.utils.Logger;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.data.repositories.CrawlerRepository;
import com.brkscosta.webcrawler.data.repositories.WebPageRepository;
import com.brkscosta.webcrawler.data.utils.WePageUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BreadthSearch extends NonInteractiveSearch {
    private final int numberOfPages;
    Queue<WebPage> pagesToVisit = new LinkedList<>();
    private int pagesAddedCount = 0;

    public BreadthSearch(Logger logger, WebPageRepository webPageRepository,
                         CrawlerRepository crawlerRepository, int numberOfPages) {
        super(logger, crawlerRepository, webPageRepository);
        this.numberOfPages = numberOfPages;
    }

    @Override
    protected void startSearch(WebPage rootPage) {
        while (!this.pagesToVisit.isEmpty() && this.pagesAddedCount < this.numberOfPages) {
            WebPage currentPage = this.pagesToVisit.poll();
            List<Link> links = this.webPageRepository.fetchIncidentLinks(currentPage, this.numberOfPages).join();
            this.logger.writeToLog("Fetched links for page: " + currentPage.getUrl() + " with " + links.size() + " links");
            this.processIncidentLinks(currentPage, links);
        }
    }

    @Override
    protected void addRootToBeSearched(WebPage rootPage) {
        this.crawlerRepository.clear();
        this.crawlerRepository.insertPage(rootPage);
        this.pagesToVisit.add(rootPage);
        this.pagesAddedCount++;
    }

    private void processIncidentLinks(WebPage currentPage, List<Link> incidentLinks) {
        for (Link link : incidentLinks) {
            if (this.pagesAddedCount >= this.numberOfPages) {
                this.logger.writeToLog("Page limit reached. Stopping search.");
                break;
            }

            WebPage existingPage = this.crawlerRepository.searchForPageByLinkIfExists(link.getUrl());
            if (existingPage != null) {
                this.crawlerRepository.makeLinkBetweenPages(currentPage, existingPage, link);
            } else {
                WebPage newPage = WePageUtils.createWebPageFromAnother(currentPage, link);
                this.addPageToGraph(currentPage, newPage, link, this.pagesAddedCount, incidentLinks.size());
                ++this.pagesAddedCount;
                this.pagesToVisit.add(newPage);
            }
        }
    }
}
