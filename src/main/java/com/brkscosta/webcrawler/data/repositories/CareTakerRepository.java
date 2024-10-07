package com.brkscosta.webcrawler.data.repositories;

import com.brkscosta.webcrawler.data.utils.Logger;
import com.google.inject.Inject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.util.Stack;


public class CareTakerRepository {

    private final Originator originator;
    private final Logger logger;


    private final Subject<Boolean> restore$ = PublishSubject.create();
    private final Stack<Memento> mementos = new Stack<>();

    /**
     * Creates a caretaker for a specific originator.
     *
     * @param originator originator to care about
     */
    @Inject
    public CareTakerRepository(Logger logger, Originator originator) {
        this.originator = originator;
        this.logger = logger;
    }

    /**
     * Requests the originator memento state and stores it.
     */
    public void saveCurrentState() {
        Memento save = originator.save();
        this.logger.writeToLog("Memento Saved for: " + save.getDescription());
        mementos.push(save);
    }

    /**
     * Requests the originator a restore of the last saved memento
     */
    public void requestRestore() {
        if (!canUndo()) return;

        Memento save = mementos.pop();
        originator.restore(save);
        this.restore$.onNext(true);
    }

    /**
     * Whether a restore (undo) operation can be provided.
     *
     * @return true if possible
     */
    public boolean canUndo() {
        return !mementos.isEmpty();
    }

    /**
     * Observable for restore operation.
     *
     * @return - Observable for restore operation
     */
    public Subject<Boolean> onRestore() {
        return restore$;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("SAVED STATES: \n");
        for (Memento m : mementos) {
            output.append(m.getDescription()).append("\n");
        }
        return output.toString();
    }

}