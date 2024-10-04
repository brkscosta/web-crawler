package com.brkscosta.webcrawler.data.entities;

import com.brkscosta.webcrawler.data.repositories.Memento;
import com.brunomnsilva.smartgraph.graph.Graph;

import java.util.Date;

public record GraphCopyMemento(Date createdAt, Graph<WebPage, Link> graph) implements Memento {
    @Override
    public String getDescription() {
        return String.format("GraphCopyMemento at %s", createdAt.toString());
    }
}
