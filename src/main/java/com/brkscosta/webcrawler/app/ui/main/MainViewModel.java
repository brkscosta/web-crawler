package com.brkscosta.webcrawler.app.ui.main;

import com.brkscosta.webcrawler.data.utils.Logger;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.data.errors.LinkException;
import com.brkscosta.webcrawler.data.errors.LinkTitleException;
import com.brkscosta.webcrawler.data.repositories.CareTakerRepository;
import com.brkscosta.webcrawler.data.repositories.CrawlerRepository;
import com.brkscosta.webcrawler.data.repositories.WebPageRepository;
import com.brkscosta.webcrawler.data.utils.WePageUtils;
import com.brkscosta.webcrawler.domain.BreadthSearch;
import com.brkscosta.webcrawler.domain.DepthSearch;
import com.brkscosta.webcrawler.domain.InteractiveSearch;
import com.brkscosta.webcrawler.domain.SearchCriteria;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MainViewModel {
    private static final String ROOT_PAGE_NAME = "Home";
    private final Logger logger;
    private final WebPageRepository webPageRepository;
    private final CrawlerRepository crawlerRepository;
    private Map<SearchType, String> stopCriteriaStringHashMap = new HashMap<>();
    private final CareTakerRepository careTakerRepository;
    private final Subject<MainViewUiState> searchComplete$ = PublishSubject.create();

    @Inject
    public MainViewModel(Logger logger, CrawlerRepository crawlerRepository, WebPageRepository webPageRepository,
                         CareTakerRepository careTakerRepository) {
        this.crawlerRepository = crawlerRepository;
        this.logger = logger;
        this.webPageRepository = webPageRepository;
        this.careTakerRepository = careTakerRepository;

        initStopCriteria();
    }

    /**
     * Starts the search process for the given web page with the specified search type.
     *
     * @param webPage    - The web page object to start the search from.
     * @param searchType - The type of search to perform.
     */
    public void startSearch(WebPage webPage, SearchType searchType) {
        this.startSearch(webPage, searchType, 0);
    }

    /**
     * Start the search for che given link with the stop criteria.
     *
     * @param webPage    - The webPage object.
     * @param searchType - The stop criteria.
     * @param numOfPages - The num of pages for dept search.
     */
    public void startSearch(WebPage webPage, SearchType searchType, int numOfPages) {

        if (webPage == null) {
            this.logger.writeToLog("Invalid WebPage");
            return;
        }

        SearchCriteria searchCriteria = this.getSearchCriteria(searchType, numOfPages);

        if (searchCriteria instanceof InteractiveSearch) {
            searchCriteria.search(webPage).whenComplete((aVoid, throwable) -> {
                this.careTakerRepository.saveCurrentState();
                this.searchComplete$.onNext(new MainViewUiState(
                        this.crawlerRepository.getGraph().numVertices(),
                        this.crawlerRepository.getGraph().numEdges(),
                        this.webPageRepository.getHttp404Count(),
                        this.webPageRepository.getHttpsProtocolsCount()
                ));
            });
            return;
        }

        this.webPageRepository.resetCounters();
        searchCriteria.search(WePageUtils.createWebPage(webPage.getUrl().toString(), ROOT_PAGE_NAME)).whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof LinkTitleException) {
                    this.logger.writeToLog(throwable.getMessage());
                } else if (throwable instanceof LinkException) {
                    this.logger.writeToLog(throwable.getMessage());
                } else if (throwable instanceof TimeoutException) {
                    this.logger.writeToLog(throwable.getMessage());
                }
                this.searchComplete$.onNext(new MainViewUiState());
                return;
            }

            this.careTakerRepository.saveCurrentState();
            this.searchComplete$.onNext(new MainViewUiState(
                    this.crawlerRepository.getGraph().numVertices(),
                    this.crawlerRepository.getGraph().numEdges(),
                    this.webPageRepository.getHttp404Count(),
                    this.webPageRepository.getHttpsProtocolsCount()
            ));
        });
    }

    /**
     * Starts the search process for the given URL with the specified search type, number of pages, and time constraints.
     *
     * @param url           - The URL to start the search from.
     * @param searchType    - The type of search to perform.
     * @param numberOfPages - The number of pages to search.
     */
    public void startSearch(String url, SearchType searchType, int numberOfPages) {
        this.startSearch(WePageUtils.createWebPage(url, ROOT_PAGE_NAME), searchType, numberOfPages);
    }

    /**
     * Gets the current stop criteria available.
     *
     * @return - An map with the stop criteria.
     */
    public Map<SearchType, String> getStopCriteria() {
        return stopCriteriaStringHashMap;
    }

    /**
     * Gets the search complete observable.
     *
     * @return - An observable with the search complete.
     */
    public Observable<MainViewUiState> onSearchComplete() {
        return this.searchComplete$;
    }

    /**
     * Sets the placement strategy
     *
     * @param placementStrategy - The target strategy placement.
     */
    public void setPlacementStrategy(StrategyPlacement placementStrategy) {
        SmartPlacementStrategy smartPlacementStrategy;
        if (placementStrategy.equals(StrategyPlacement.RANDOM)) {
            smartPlacementStrategy = new SmartRandomPlacementStrategy();
        } else {
            smartPlacementStrategy = new SmartCircularSortedPlacementStrategy();
        }

        this.logger.writeToLog("Selected strategy: " + placementStrategy);
        this.crawlerRepository.setPlacementStrategy(smartPlacementStrategy);
    }

    /**
     * Gets placement strategy
     *
     * @return - The SmartPlacementStrategy
     */
    public SmartPlacementStrategy getPlacementStrategy() {
        return this.crawlerRepository.getPlacementStrategy();
    }

    /**
     * Gets the selected strategy based on the object created.
     *
     * @return - A enum StrategyPlacement
     */
    public StrategyPlacement getSelectedPlacementStrategy() {
        boolean isDefaultPlacementStrategyCircular = this.getPlacementStrategy().getClass().equals(SmartCircularSortedPlacementStrategy.class);
        return isDefaultPlacementStrategyCircular ? StrategyPlacement.CIRCULAR : StrategyPlacement.RANDOM;
    }

    /**
     * Clear the graph model.
     */
    public void clearGraph() {
        this.crawlerRepository.clear();
        this.logger.writeToLog("Clearing graph...");
    }

    /**
     * Get a graph model.
     *
     * @return - An Graph<WebPage, Link>
     */
    public Graph<WebPage, Link> getGraph() {
        return this.crawlerRepository.getGraph();
    }

    /**
     * Gets the root vertex of the graph.
     *
     * @return the root WebPage of the graph
     */
    public WebPage getRootVertex() {
        return this.crawlerRepository.getRootPage();
    }

    public Subject<Boolean> onRestore() {
        return this.careTakerRepository.onRestore();
    }

    @Override
    public String toString() {
        return "MainViewModel";
    }

    private void initStopCriteria() {
        this.stopCriteriaStringHashMap = new HashMap<>() {{
            put(SearchType.BREADTH, "Breadth");
            put(SearchType.DEPTH, "Depth");
        }};
    }

    private SearchCriteria getSearchCriteria(SearchType searchType, int numOfPages) {
        return switch (searchType) {
            case DEPTH -> new DepthSearch(this.logger, this.webPageRepository, this.crawlerRepository, numOfPages);
            case BREADTH -> new BreadthSearch(this.logger, this.webPageRepository, this.crawlerRepository, numOfPages);
            case INTERACTIVE -> new InteractiveSearch(this.logger, this.webPageRepository, this.crawlerRepository);
        };
    }
}
