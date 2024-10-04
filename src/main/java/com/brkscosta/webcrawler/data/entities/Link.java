package com.brkscosta.webcrawler.data.entities;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class Link {

    private URL url;
    private String title;
    private int statusCode;

    public Link(URL url, String title, int statusCode) {
        this.url = url;
        this.title = title;
        this.statusCode = statusCode;
    }

    public Link() {
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getTitle() {
        return title;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(url, link.url);
    }

    @Override
    public String toString() {
        return "Link{" +
                "url=" + url +
                ", title='" + title + '\'' +
                '}';
    }
}

