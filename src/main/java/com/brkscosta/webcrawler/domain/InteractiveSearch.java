package com.brkscosta.webcrawler.domain;

import com.brkscosta.webcrawler.data.utils.Logger;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.data.repositories.CrawlerRepository;
import com.brkscosta.webcrawler.data.repositories.WebPageRepository;
import com.brkscosta.webcrawler.data.utils.WePageUtils;

import java.util.concurrent.CompletableFuture;

public class InteractiveSearch implements SearchCriteria {
    private final Logger logger;
    private final WebPageRepository webPageRepository;
    private final CrawlerRepository crawlerRepository;

    public InteractiveSearch(Logger logger, WebPageRepository webPageRepository,
                             CrawlerRepository crawlerRepository) {
        this.logger = logger;
        this.webPageRepository = webPageRepository;
        this.crawlerRepository = crawlerRepository;
    }

    @Override
    public CompletableFuture<Void> search(WebPage rootPage) {
        if (rootPage == null) {
            return CompletableFuture.completedFuture(null);
        }

        if (this.crawlerRepository.getAllWebPages().isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return this.webPageRepository.fetchIncidentLinks(rootPage).whenComplete((links, throwable) -> {

            if (throwable != null) {
                this.logger.writeToLog("Error fetching links for page: " + rootPage.getUrl());
                return;
            }

            for (Link link : links) {
                WebPage existingPage = this.crawlerRepository.searchForPageByLinkIfExists(link.getUrl());
                if (existingPage != null) {
                    this.logger.writeToLog("Linking existing pages: " + rootPage.getUrl() + " -> " + existingPage.getUrl());
                    this.crawlerRepository.makeLinkBetweenPages(rootPage, existingPage, link);
                } else {
                    WebPage newPage = WePageUtils.createWebPageFromAnother(rootPage, link);
                    this.crawlerRepository.insertPage(newPage);
                    this.crawlerRepository.makeLinkBetweenPages(rootPage, newPage, link);
                    this.logger.writeToLog("Inserted new page and linked: " + rootPage.getUrl() + " -> " + newPage.getUrl());
                }
            }
        }).thenAccept((param) -> {
            this.logger.writeToLog("Search completed with " + param.size() + " new pages.");
            this.logger.writeToLog("Search Finished");
        });
    }
}
