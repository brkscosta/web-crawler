package com.brkscosta.webcrawler.domain;

import com.brkscosta.webcrawler.data.utils.Logger;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.data.repositories.CrawlerRepository;
import com.brkscosta.webcrawler.data.repositories.WebPageRepository;
import com.brkscosta.webcrawler.data.utils.WePageUtils;

import java.util.List;
import java.util.Stack;


public class DepthSearch extends NonInteractiveSearch {
    private final int depthLimit;
    Stack<WebPage> pagesToVisit = new Stack<>();

    public DepthSearch(Logger logger, WebPageRepository webPageRepository,
                       CrawlerRepository crawlerRepository, int depthLimit) {
        super(logger, crawlerRepository, webPageRepository);
        this.depthLimit = depthLimit;
    }

    @Override
    protected void startSearch(WebPage rootPage) {
        while (!this.pagesToVisit.isEmpty()) {
            WebPage currentPage = this.pagesToVisit.pop();

            if (currentPage.getDepth() == depthLimit) {
                break;
            }

            this.logger.writeToLog("Visiting page: " + currentPage.getTitle() + " with depth: " + currentPage.getDepth());
            List<Link> links = this.webPageRepository.fetchIncidentLinks(currentPage, this.depthLimit).join();
            this.logger.writeToLog("Fetched links for page: " + currentPage.getTitle() + " with " + links.size() + " links");
            this.processIncidentLinks(currentPage, links);
        }
    }

    @Override
    protected void addRootToBeSearched(WebPage rootPage) {
        this.pagesToVisit.push(rootPage);
        this.crawlerRepository.clear();
        this.crawlerRepository.insertPage(rootPage);
    }

    private void processIncidentLinks(WebPage currentPage, List<Link> links) {
        int incidentLinksAdded = 0;
        for (Link link : links) {
            WebPage existingPage = this.crawlerRepository.searchForPageByLinkIfExists(link.getUrl());
            if (existingPage != null) {
                this.crawlerRepository.makeLinkBetweenPages(currentPage, existingPage, link);
            } else {
                WebPage newPage = WePageUtils.createWebPageFromAnother(currentPage, link);
                this.addPageToGraph(currentPage, newPage, link, incidentLinksAdded, links.size());
                this.pagesToVisit.push(newPage);
                this.logger.writeToLog("New page inserted: " + newPage.getUrl());
            }
        }
    }
}
