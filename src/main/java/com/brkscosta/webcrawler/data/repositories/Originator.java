package com.brkscosta.webcrawler.data.repositories;

public interface Originator {
    /**
     * Request of memento for current state.
     *
     * @return the memento state
     */
    Memento save();

    /**
     * Request to change state for this memento.
     *
     * @param savedState the memento state to restore
     */
    void restore(Memento savedState);
}
