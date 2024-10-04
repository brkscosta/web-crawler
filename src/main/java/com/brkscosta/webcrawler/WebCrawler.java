package com.brkscosta.webcrawler;

import com.brkscosta.webcrawler.app.di.AppModule;
import com.brkscosta.webcrawler.app.di.DataModule;
import com.brkscosta.webcrawler.app.ui.main.MainView;
import com.brkscosta.webcrawler.webCrawler.BuildConfig;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WebCrawler extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Injector injector = Guice.createInjector(new AppModule(), new DataModule());

        FXMLLoader fxmlLoader = new FXMLLoader(WebCrawler.class.getResource("mainView.fxml"));
        fxmlLoader.setControllerFactory(injector::getInstance);

        MainView mainView = injector.getInstance(MainView.class);

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(BuildConfig.APP_NAME);
        stage.setScene(scene);
        stage.setMinHeight(BuildConfig.STAGE_MIN_HEIGHT);
        stage.setMinWidth(BuildConfig.STAGE_MIN_WIDTH);
        stage.show();
        mainView.getGraphView().init();
    }

    public static void main(String[] args) {
        launch();
    }
}