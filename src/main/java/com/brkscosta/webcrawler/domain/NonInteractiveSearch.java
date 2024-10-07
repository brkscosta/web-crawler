package com.brkscosta.webcrawler.domain;

import com.brkscosta.webcrawler.data.utils.Logger;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.data.repositories.CrawlerRepository;
import com.brkscosta.webcrawler.data.repositories.WebPageRepository;

import java.util.concurrent.CompletableFuture;

public abstract class NonInteractiveSearch implements SearchCriteria {
    protected WebPageRepository webPageRepository;
    protected CrawlerRepository crawlerRepository;
    protected Logger logger;

    public NonInteractiveSearch(Logger logger, CrawlerRepository crawlerRepository,
                                WebPageRepository webPageRepository) {
        this.crawlerRepository = crawlerRepository;
        this.webPageRepository = webPageRepository;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<Void> search(WebPage rootPage) {
        this.addRootToBeSearched(rootPage);
        return CompletableFuture.runAsync(() -> {
            this.startSearch(rootPage);
        }).thenAccept((aVoid) -> {
            this.logger.writeToLog("Search Finished");
        });
    }

    protected void addPageToGraph(WebPage currentPage, WebPage newPage, Link link, int linksAdded, int totalLinks) {
        if (currentPage.getIsLastOfALevel() && linksAdded == totalLinks - 1) {
            newPage.setIsLastOfALevel(true);
        }

        this.crawlerRepository.insertPage(newPage);
        this.crawlerRepository.makeLinkBetweenPages(currentPage, newPage, link);
    }

    abstract void startSearch(WebPage rootPage);

    abstract void addRootToBeSearched(WebPage rootPage);
}
