package com.core;

import java.util.ArrayList;
import com.tasks.DoableTask;

/**
 * An instance of the State class is a state of the running program.
 */
public class State {

    /**
     * Flag to determine if program should terminate.
     */
    public boolean toExit = false;

    /**
     * List of DoableTask.
     */
    public ArrayList<DoableTask> list = new ArrayList<>();

    /**
     * Initialize with a list of doableTasks.
     * @param initialList   initial list
     */
    public State(ArrayList<DoableTask> initialList) {
        list = initialList;
    }

    /**
     * Default constructor that does not add items to the list.
     */
    public State() {
    }
}
