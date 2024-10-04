package com.brkscosta.webcrawler.data.errors;

import java.util.concurrent.CompletionException;

public class LinkException extends CompletionException {
    public LinkException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
