package com.brkscosta.webcrawler.data.repositories;

import com.brkscosta.webcrawler.data.entities.GraphCopyMemento;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class CrawlerRepository implements Originator {
    private Graph<WebPage, Link> graph;
    private SmartPlacementStrategy placementStrategy;


    /**
     * Constructs a GraphRepository with the specified graph and placement strategy.
     *
     * @param graph             - The graph to be used in the repository
     * @param placementStrategy - The strategy for placing elements in the graph
     */
    public CrawlerRepository(Graph<WebPage, Link> graph, SmartPlacementStrategy placementStrategy) {
        this.graph = graph;
        this.placementStrategy = placementStrategy;
    }

    /**
     * Constructs a GraphRepository with the default graph and placement strategy.
     */
    public CrawlerRepository() {
        this(new GraphEdgeList<>(), new SmartCircularSortedPlacementStrategy());
    }

    /**
     * Returns the graph.
     *
     * @return - The graph
     */
    public Graph<WebPage, Link> getGraph() {
        return this.graph;
    }

    /**
     * Returns the root page.
     *
     * @return - The root page
     */
    public WebPage getRootPage() {
        Vertex<WebPage> pageVertex = this.graph.vertices()
                .stream().filter(vertex -> vertex.element().getDepth() == 0).findFirst().orElse(null);

        return pageVertex == null ? null : pageVertex.element();
    }

    /**
     * Searches for a page by link.
     *
     * @param url - The URL to search for
     * @return - The WebPage if found, null otherwise
     */
    public WebPage searchForPageByLinkIfExists(URL url) {
        Vertex<WebPage> pageVertex = this.graph.vertices().stream().filter(v -> v.element()
                        .getUrl().toString()
                        .equals(url.toString()))
                .findFirst()
                .orElse(null);
        return pageVertex == null ? null : pageVertex.element();
    }

    public void makeLinkBetweenPages(WebPage currentPage, WebPage existingPage, Link link) {
        if (!this.containsLink(link)) {
            this.graph.insertEdge(currentPage, existingPage, link);
        }
    }

    /**
     * Inserts a page into the graph.
     *
     * @param webPage - The WebPage to insert
     */
    public void insertPage(WebPage webPage) {
        if (!this.containsWebPage(webPage)) {
            this.graph.insertVertex(webPage);
        }
    }

    /**
     * Returns all the web pages in the graph.
     *
     * @return - A list of WebPage
     */
    public List<WebPage> getAllWebPages() {
        Collection<Vertex<WebPage>> verticesCollection = this.graph.vertices();
        List<WebPage> webPages = new ArrayList<>();

        for (Vertex<WebPage> vertex : verticesCollection) {
            webPages.add(vertex.element());
        }

        return webPages;
    }

    /**
     * Remove all edges and vertices from the graph.
     */
    public void clear() {
        this.graph.edges().forEach(this.graph::removeEdge);
        this.graph.vertices().forEach(this.graph::removeVertex);
    }

    /**
     * Sets the placement strategy.
     *
     * @param placementStrategy - The target strategy placement.
     */
    public void setPlacementStrategy(SmartPlacementStrategy placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    /**
     * Returns the placement strategy.
     *
     * @return - The SmartPlacementStrategy
     */
    @Override
    public Memento save() {
        return new GraphCopyMemento(new Date(), this.graph);
    }

    /**
     * Restores the graph to a previous state.
     *
     * @param savedState - The state to restore
     */
    @Override
    public void restore(Memento savedState) {
        GraphCopyMemento savedGraphRepository = (GraphCopyMemento) savedState;
        this.graph = savedGraphRepository.graph();
    }

    /**
     * Returns the placement strategy.
     *
     * @return - The SmartPlacementStrategy
     */
    public SmartPlacementStrategy getPlacementStrategy() {
        return placementStrategy;
    }

    private boolean containsWebPage(WebPage webPage) {
        return this.graph.vertices().stream().anyMatch(v -> v.element().equals(webPage));
    }

    private boolean containsLink(Link link) {
        return graph.edges().stream().anyMatch(v -> v.element().equals(link));
    }
}
