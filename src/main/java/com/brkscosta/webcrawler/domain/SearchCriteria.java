package com.brkscosta.webcrawler.domain;

import com.brkscosta.webcrawler.data.entities.WebPage;

import java.util.concurrent.CompletableFuture;

public interface SearchCriteria {
    CompletableFuture<Void> search(WebPage rootPage);
}
