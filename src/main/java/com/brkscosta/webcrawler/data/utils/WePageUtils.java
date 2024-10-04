package com.brkscosta.webcrawler.data.utils;

import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;

import java.net.MalformedURLException;
import java.net.URL;

public class WePageUtils {
    public static WebPage createWebPageFromAnother(WebPage currentPage, Link link) {
        WebPage webPage = new WebPage(link.getUrl());
        webPage.setTitle(link.getTitle());
        webPage.setStatusCode(link.getStatusCode());
        webPage.setDepth(currentPage.getDepth() + 1);
        return webPage;
    }

    public static WebPage createWebPage(String baseUrl, String title) {
        try {
            URL url = new URL(baseUrl);
            WebPage webPage = new WebPage(url);
            webPage.setDepth(0);
            webPage.setIsLastOfALevel(true);
            webPage.setTitle(title);
            return webPage;
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
