package com.brkscosta.webcrawler.data.entities;

import java.net.URL;
import java.util.*;

public class WebPage {

    private int depth;
    private boolean isLastOfALevel;
    private String title;
    private URL url;
    private final List<Link> linksNotFound;
    private int statusCode;

    /**
     * Constructs a new instance of WebPage
     *
     * @param url - the url
     */
    public WebPage(URL url) {
        this.url = url;
        this.linksNotFound = new ArrayList<>();
    }

    public int getDepth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean getIsLastOfALevel() {
        return this.isLastOfALevel;
    }

    public void setIsLastOfALevel(boolean isLastOfALevel) {
        this.isLastOfALevel = isLastOfALevel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void addNotFoundLink(URL url, String title, int statusCode) {
        Link link = new Link(url, title, statusCode);
        this.linksNotFound.add(link);
    }

    public List<Link> getLinksNotFound() {
        return this.linksNotFound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebPage webPage = (WebPage) o;
        return depth == webPage.depth && isLastOfALevel == webPage.isLastOfALevel && statusCode == webPage.statusCode && Objects.equals(title, webPage.title) && Objects.equals(url, webPage.url) && Objects.equals(linksNotFound, webPage.linksNotFound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depth, isLastOfALevel, title, url, linksNotFound, statusCode);
    }

    @Override
    public String toString() {
        return "Title: " + title + " HTTP Code: " + this.statusCode + "\n";
    }
}
