package com.brkscosta.webcrawler.data.errors;

import java.util.concurrent.CompletionException;

public class LinkTitleException extends CompletionException {
    public LinkTitleException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
