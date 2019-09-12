package com.core;

import com.core.savedata.SaveFile;
import com.util.Printer;
import java.util.Arrays;

public class DukeResponder {
    private State state;

    public DukeResponder() {
        state = new State(SaveFile.loadTasks());
    }

    public String getResponse(String input) {
        Arrays.stream(Response.values()).reduce(false, (acc, val) -> acc || val.call(input, state), Boolean::logicalAnd);
        return Printer.flush();
    }
}
