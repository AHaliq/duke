package com.tasks;

import com.util.datetime.DateTime;

public class Event extends DoableTask {

    /**
     * The task's start range.
     */
    private DateTime startDate;

    /**
     * The task's end range.
     */
    private DateTime endDate;

    /**
     * Create DoableTask of this type with dateRange string.
     *
     * @param taskName  name of task
     * @param start     start of date range
     * @param end       end of date range
     */
    public Event(String taskName, DateTime start, DateTime end) {
        super(taskName);
        startDate = start;
        endDate = end;
    }

    /**
     * get string representation of task.
     *
     * @return string
     */
    public String toString() {
        return "[E]" + super.toString() + "(at: " + startDate.toString() + " to " + endDate.toString() +  ")";
    }
}