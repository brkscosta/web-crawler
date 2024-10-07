package com.brkscosta.webcrawler.app.ui.main;

public record MainViewUiState(
        Integer numOfWebPages,
        Integer numOfLinks,
        Integer numLinksNotFound,
        Integer numHTTPSLinks
) {
    public MainViewUiState() {
        this(0, 0, 0, 0);
    }
}
