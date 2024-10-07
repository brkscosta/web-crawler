module com.brkscosta.webcrawler.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.guice;
    requires smartgraph;
    requires org.jsoup;
    requires java.desktop;
    requires io.reactivex.rxjava3;

    opens com.brkscosta.webcrawler.app.ui.main to javafx.fxml;

    exports com.brkscosta.webcrawler;
    exports com.brkscosta.webcrawler.app.ui.main;
    exports com.brkscosta.webcrawler.app.di;
    exports com.brkscosta.webcrawler.data.entities;
    exports com.brkscosta.webcrawler.data.repositories;
    exports com.brkscosta.webcrawler.data.utils;
}
