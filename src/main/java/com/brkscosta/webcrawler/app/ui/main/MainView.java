package com.brkscosta.webcrawler.app.ui.main;

import com.brkscosta.webcrawler.data.utils.Logger;
import com.brkscosta.webcrawler.data.entities.Link;
import com.brkscosta.webcrawler.data.entities.WebPage;
import com.brkscosta.webcrawler.webCrawler.BuildConfig;
import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.function.UnaryOperator;

public class MainView extends AnchorPane {

    @FXML
    private Label totalPagesLbl = new Label();

    @FXML
    private Label totalLinksLbl = new Label();

    @FXML
    private Label numLinksNotFoundLbl = new Label();

    @FXML
    private Label totalHTTPSProtocolsLbl = new Label();

    @FXML
    private Label rootPageLbl = new Label();

    @FXML
    private CheckMenuItem circularStrategyMenuCheck;

    @FXML
    private CheckMenuItem randomStrategyMenuCheck;

    @FXML
    private CheckMenuItem automaticLayoutMenuItem;

    @FXML
    private ChoiceBox<String> searchCriteriaDpw = new ChoiceBox<>();

    @FXML
    private TextField urlTextField = new TextField();

    @FXML
    private TextField numberOfPagesTextField = new TextField();

    @FXML
    private CheckBox automaticLayoutCbx = new CheckBox();

    @FXML
    private BorderPane graphBorderPane = new BorderPane();

    private final SmartGraphPanel<WebPage, Link> graphView;
    private final MainViewModel viewModel;
    private final ContentZoomScrollPane zoomScrollPane;
    private final Logger logger;

    /**
     * Constructs a new instance of MainView
     *
     * @param viewModel - the main view instance
     */
    @Inject
    public MainView(Logger logger, MainViewModel viewModel) {
        this.viewModel = viewModel;
        this.logger = logger;
        this.graphView = new SmartGraphPanel<>(viewModel.getGraph(), viewModel.getPlacementStrategy());
        this.zoomScrollPane = new ContentZoomScrollPane(graphView, BuildConfig.MAX_SCALE_FACTOR, BuildConfig.DELTA_SCALE_FACTOR);
    }

    @FXML
    private void initialize() {
        this.setupListeners();
        this.setupUI();
        this.setNumOfPagesInputFilterListener();
    }

    private void setupUI() {
        this.checkStrategyMenuCheck();
        this.graphBorderPane.setCenter(this.zoomScrollPane);
        this.graphBorderPane.setRight(this.createSidebar(this.zoomScrollPane));

        this.automaticLayoutCbx.selectedProperty().bindBidirectional(this.graphView.automaticLayoutProperty());
        this.automaticLayoutMenuItem.selectedProperty().bindBidirectional(this.graphView.automaticLayoutProperty());
    }

    @FXML
    public void onStartSearchClicked() {
        SearchType searchType = SearchType.valueOf(this.searchCriteriaDpw.getValue().toUpperCase());
        this.logger.writeToLog("onStartSearchClicked: StopCriteria: " + searchType);

        if (this.numberOfPagesTextField.getText().isEmpty()) {
            this.logger.writeToLog("Number of pages is empty");
            return;
        }

        int numberOfPages = Integer.parseInt(this.numberOfPagesTextField.getText());
        String url = this.urlTextField.getText().trim();

        this.viewModel.startSearch(url, searchType, numberOfPages);
        this.rootPageLbl.setText(url);
    }

    public SmartGraphPanel<WebPage, Link> getGraphView() {
        return graphView;
    }

    @FXML
    private void onClearGraphClicked() {
        this.viewModel.clearGraph();
        this.graphView.update();
    }

    @FXML
    private void onForceUpdateClicked() {
        this.graphView.update();
        this.logger.writeToLog("Updating graph");
    }

    @FXML
    private void onAutomaticLayoutCheck(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.automaticLayoutCbx) {
            this.automaticLayoutMenuItem.setSelected(this.automaticLayoutCbx.isSelected());
        } else {
            this.automaticLayoutCbx.setSelected(this.automaticLayoutMenuItem.isSelected());
        }

        this.logger.writeToLog("Automatic Layout is: " + this.automaticLayoutMenuItem.isSelected());
    }

    @FXML
    private void onExitClicked(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    private void onAboutClicked(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("WebCrawler Application");
        alert.setContentText("This is a simple web crawling tool developed using JavaFX.");
        alert.showAndWait();
    }

    @FXML
    private void onCircularStrategySelected(ActionEvent actionEvent) {
        this.randomStrategyMenuCheck.setSelected(!this.circularStrategyMenuCheck.isSelected());
        this.viewModel.setPlacementStrategy(StrategyPlacement.CIRCULAR);
    }

    @FXML
    private void onRandomStrategySelected(ActionEvent actionEvent) {
        this.circularStrategyMenuCheck.setSelected(!this.randomStrategyMenuCheck.isSelected());
        this.viewModel.setPlacementStrategy(StrategyPlacement.RANDOM);
    }

    private void checkStrategyMenuCheck() {
        this.searchCriteriaDpw.getSelectionModel().select(0);
        if (this.viewModel.getSelectedPlacementStrategy() == StrategyPlacement.RANDOM) {
            this.randomStrategyMenuCheck.selectedProperty().set(true);
        } else {
            this.circularStrategyMenuCheck.setSelected(true);
        }
    }

    private TextFormatter<String> getStringTextFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (newText.isEmpty()) {
                return change;
            }

            if (newText.matches("\\d*")) {
                try {
                    int value = Integer.parseInt(newText);
                    if (value > 0 && value <= 10) {
                        return change;
                    }
                } catch (NumberFormatException e) {
                    this.logger.writeToLog("Input value is not valid");
                }
            }
            return null;
        };

        return new TextFormatter<>(filter);
    }

    private void setNumOfPagesInputFilterListener() {
        TextFormatter<String> textFormatter = this.getStringTextFormatter();
        this.numberOfPagesTextField.setTextFormatter(textFormatter);
    }

    private void setAutomaticLayout() {
        this.automaticLayoutMenuItem.setSelected(true);
        this.automaticLayoutCbx.setSelected(true);
        this.graphView.setAutomaticLayout(true);
    }

    private void setupListeners() {
        this.viewModel.getStopCriteria().forEach((searchType, name) -> {
            this.searchCriteriaDpw.getItems().add(name);
        });

        this.viewModel.onSearchComplete().subscribe(this::accept);

        this.searchCriteriaDpw.setOnAction(event -> {
            this.viewModel.clearGraph();
        });

        this.graphView.setVertexDoubleClickAction(event -> {
            WebPage webPage = event.getUnderlyingVertex().element();
            this.logger.writeToLog("Clicked on page: " + webPage.getTitle());
            this.viewModel.startSearch(webPage, SearchType.INTERACTIVE);
        });
    }

    private void updateUi(MainViewUiState uiState) {
        this.totalPagesLbl.setText(uiState.numOfWebPages().toString());
        this.totalLinksLbl.setText(uiState.numOfLinks().toString());
        this.numLinksNotFoundLbl.setText(uiState.numLinksNotFound().toString());
        this.totalHTTPSProtocolsLbl.setText(uiState.numHTTPSLinks().toString());
    }

    private Node createSidebar(ContentZoomScrollPane zoomPane) {
        VBox paneSlider = new VBox(10);
        paneSlider.setAlignment(Pos.CENTER);
        paneSlider.setPadding(new Insets(10));
        paneSlider.setSpacing(10);

        Slider slider = new Slider(zoomPane.getMinScaleFactor(),
                zoomPane.getMaxScaleFactor(), BuildConfig.MIN_SCALE_FACTOR);

        slider.setOrientation(Orientation.VERTICAL);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(zoomPane.getDeltaScaleFactor());
        slider.setMinorTickCount(BuildConfig.MIN_SCALE_FACTOR.intValue());
        slider.setBlockIncrement(0.125f);
        slider.setSnapToTicks(true);
        slider.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        slider.valueProperty().bind(zoomPane.scaleFactorProperty());

        paneSlider.getChildren().addAll(slider, new Text("Zoom"));

        return paneSlider;
    }

    private void accept(MainViewUiState uiState) {
        this.graphView.updateAndWait();
        this.graphView.getStylableVertex(this.viewModel.getRootVertex()).addStyleClass("myVertex");
        this.setAutomaticLayout();

        Platform.runLater(() -> {
            this.updateUi(uiState);
        });
    }
}