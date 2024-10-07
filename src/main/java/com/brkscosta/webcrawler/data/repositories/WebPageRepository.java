package com.brkscosta.webcrawler.data.repositories;

import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.data.errors.LinkException;
import com.brkscosta.webcrawler.data.errors.LinkTitleException;
import com.brkscosta.webcrawler.data.utils.Logger;
import com.brkscosta.webcrawler.webCrawler.BuildConfig;
import com.google.inject.Inject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebPageRepository {

    private final Logger logger;
    private int httpsProtocolsCount = 0;
    private int http404Count = 0;

    @Inject
    public WebPageRepository(Logger logger) {
        this.logger = logger;
    }

    /**
     * Gets the number of https protocols.
     *
     * @return - A number
     */
    public int getHttpsProtocolsCount() {
        return this.httpsProtocolsCount;
    }

    /**
     * Gets the number of http 404 errors.
     *
     * @return - A number
     */
    public int getHttp404Count() {
        return this.http404Count;
    }

    /**
     * Resets the counters.
     */
    public void resetCounters() {
        this.httpsProtocolsCount = 0;
        this.http404Count = 0;
    }

    /**
     * Count number of links not found
     *
     * @param webPage- The WebPage object
     * @return - Counter of pages
     */
    public int searchLinksNotFoundByPage(WebPage webPage) {
        return webPage.getLinksNotFound().size();
    }

    /**
     * Fetches all the incident links from a web page.
     *
     * @param webPage - The WebPage object.
     * @return - A Completable future with List<Link>
     */
    public CompletableFuture<List<Link>> fetchIncidentLinks(WebPage webPage, int maxLinks) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Document doc = Jsoup.connect(webPage.getUrl().toString())
                        .timeout(BuildConfig.CONNECTION_TIMEOUT)
                        .get();
                Elements links = doc.select("a[href]");

                List<Link> incidentLinks = this.storeIncidentLinks(links, doc, webPage.getUrl(), maxLinks);
                Set<Link> uniqueLinks = new HashSet<>(incidentLinks);
                this.countHttpsProtocols(uniqueLinks);
                return new ArrayList<>(uniqueLinks);
            } catch (IOException ex) {
                this.logger.writeToLog(ex.getMessage());
                throw new LinkException("Failed to fetch links for " + webPage.getUrl());
            }
        });
    }

    /**
     * Fetches all the incident links from a web page.
     *
     * @param webPage - The WebPage object.
     * @return - A Completable future with List<Link>
     */
    public CompletableFuture<List<Link>> fetchIncidentLinks(WebPage webPage) {
        return fetchIncidentLinks(webPage, Integer.MAX_VALUE);
    }

    private List<Link> storeIncidentLinks(Elements links, Document doc, URL url, int maxLinks) {
        List<Link> incidentLinks = new ArrayList<>();
        int pageStatusCode = doc.connection().response().statusCode();
        int count = 0;

        for (Element link : links) {

            if (maxLinks == count) break;

            String href = this.processLink(link.attr("abs:href"), url);

            if (href != null) {
                URL linkUrl = this.validateUrl(href);
                if (linkUrl != null) {
                    Link newLink = new Link();
                    newLink.setUrl(linkUrl);
                    newLink.setStatusCode(pageStatusCode);

                    CompletableFuture<String> titleFuture = this.fetchTitleByLink(linkUrl.toString(), newLink);
                    newLink.setTitle(titleFuture.join());
                    incidentLinks.add(newLink);
                    ++count;
                }
            }
        }
        return incidentLinks;
    }

    private URL validateUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            this.logger.writeToLog("Invalid URL: " + url);
            return null;
        }
    }

    private CompletableFuture<String> fetchTitleByLink(String url, Link newLink) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Document doc = Jsoup.connect(url).timeout(BuildConfig.CONNECTION_TIMEOUT).get();
                Elements title = doc.select("title");
                return title.text();
            } catch (IOException ex) {
                Pattern pattern = Pattern.compile("Status=(\\d{3})");
                Matcher matcher = pattern.matcher(ex.getMessage());
                if (matcher.find()) {
                    newLink.setStatusCode(Integer.parseInt(matcher.group(1)));
                }
                throw new LinkTitleException("Failed to fetch title for " + url);
            }
        });
    }

    private String processLink(String link, URL url) {
        if (link == null || link.isEmpty()) {
            return null;
        }

        boolean isNotFileOrImage = !link.endsWith(".pdf") && !link.endsWith(".jpg") &&
                !link.endsWith(".jpeg") &&
                !link.endsWith(".png") &&
                !link.endsWith(".gif") &&
                !link.endsWith(".bmp") &&
                !link.endsWith(".svg") &&
                !link.endsWith(".txt") &&
                !link.endsWith(".exe") &&
                !link.endsWith(".webp");

        String baseUrl = url.getProtocol() + "://" + url.getAuthority() + stripFilename(url.getPath());
        StringBuilder fullLink = new StringBuilder();

        if (link.startsWith("./")) {
            fullLink.append(baseUrl).append(link.substring(2));
        } else if (link.startsWith("#")) {
            fullLink.append(url).append(link);
        } else if (link.startsWith("javascript:")) {
            return null;
        } else if (link.startsWith("../") || (!link.startsWith("http://") && !link.startsWith("https://"))) {
            fullLink.append(baseUrl).append(link);
        } else if (!isNotFileOrImage) {
            return null;
        } else {
            return link;
        }

        return fullLink.toString();
    }

    private String stripFilename(String path) {
        int pos = path.lastIndexOf("/");
        return pos == -1 ? path : path.substring(0, pos + 1);
    }

    private void countHttpsProtocols(Set<Link> links) {
        this.httpsProtocolsCount += (int) links.stream().filter(link -> link.getUrl().getProtocol().equals("https")).count();
        this.http404Count += (int) links.stream().filter(link -> link.getStatusCode() == 404).count();
    }
}
