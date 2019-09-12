package com.core;

import com.core.savedata.SaveFile;
import com.util.json.JsonArray;
import com.util.json.ReadWriteFiles;
import com.util.datetime.DateTime;
import java.util.stream.IntStream;

import com.util.Printer;
import com.tasks.Deadline;
import com.tasks.DoableTask;
import com.tasks.Event;
import com.tasks.Todo;
import java.util.stream.Stream;

public enum Response {
    BYE("(?i)^bye\\s*", (i, s) -> {
        Printer.printString("Bye. Hope to see you again soon!");
        s.toExit = true;
        return true;
    }),
    LIST("(?i)^list\\s*", (j, s) -> {
        String finalString = listIndexStreamToString(IntStream.range(0, s.list.size()).boxed(), s);
        Printer.printString(finalString.equalsIgnoreCase("") ? "You have no tasks" : finalString);
        return true;
    }),
    FIND_BLANK("(?i)^find\\s*", (i, s) -> {
        Printer.printString("Did not specify substring to find");
        return true;
    }),
    FIND("(?i)^find .+", (i, s) -> {
        String substr = i.split(" ", 2)[1];
        String finalString = listIndexStreamToString(IntStream.range(0,
                s.list.size()).boxed().filter((ti) -> s.list.get(ti).getName().contains(substr)), s);
        Printer.printString(finalString.equalsIgnoreCase("") ? "No results" : finalString);
        return true;
    }),
    DONE("(?i)^done [0-9]+", (i, s) -> {
        int index = getNumber(i) - 1;
        if (checkValidIndex(index, s)) {
            s.list.get(index).markAsDone();
            Printer.printString("Nice! I've marked this task as done:\n  "
                    + s.list.get(index).toString());
            save(s);
            return true;
        }
        return false;
    }),
    DELETE("(?i)^delete [0-9]+", (i, s) -> {
        int index = getNumber(i) - 1;
        if (checkValidIndex(index, s)) {
            Printer.printString("Noted! I've removed this task:\n  "
                    + s.list.get(index).toString()
                    + "\nNow you have "
                    + (s.list.size() - 1)
                    + " tasks in the list.");
            s.list.remove(index);
            save(s);
            return true;
        }
        return false;
    }),
    TODO_NO_NAME("(?i)^todo\\s*", (i, s) -> {
        Printer.printError("The description of a todo cannot be empty");
        return true;
    }),
    TODO("(?i)^todo .+", (i, s) -> {
        addTask(new Todo(i.split("todo ", 2)[1]), s);
        assert s.list.size() >= 1 : "expecting non empty list";
        return true;
    }),
    EVENT_NO_NAME("(?i)^event\\s*", (i, s) -> {
        Printer.printError("The description of an event cannot be empty");
        return true;
    }),
    EVENT_NO_TIME("^(?i)event (((?!/at).)+$)|(.+ /at\\s*$)", (i, s) -> {
        Printer.printError("The date range of an event cannot be empty");
        return true;
    }),
    EVENT("(?i)^event .+ /at \\d{1,2}/\\d{1,2}/\\d{4} \\d{4} to \\d{1,2}/\\d{1,2}/\\d{4} \\d{4}", (i, s) -> {
        String[] parts = splitTwoDelimiters(i, "(?i)^event ", "(?i)/at ");

        String[] dates = parts[1].split(" to ", 2);
        addTask(new Event(parts[0], DateTime.parseString(dates[0]),
                DateTime.parseString(dates[1])), s);
        assert s.list.size() >= 1 : "expecting non empty list";
        return true;
    }),
    EVENT_WRONG_TIME("(?i)^event .+ /at .+", (i, s) -> {
        Printer.printError(
                "The date range must be in the format 'DD/MM/YYYY HHMM to DD/MM/YYYY HHMM'");
        return true;
    }),
    DEADLINE_NO_NAME("(?i)^deadline\\s*", (i, s) -> {
        Printer.printError("The description of a deadline cannot be empty");
        return true;
    }),
    DEADLINE_NO_TIME("(?i)^deadline (((?!/by).)+$)|(.+ /by\\s*$)", (i, s) -> {
        Printer.printError("The due date of a deadline cannot be empty");
        return true;
    }),
    DEADLINE("(?i)^deadline .+ /by .+", (i, s) -> {
        String[] parts = splitTwoDelimiters(i, "(?i)^deadline ", "(?i)/by ");
        addTask(new Deadline(parts[0], DateTime.parseString(parts[1])), s);
        assert s.list.size() >= 1 : "expecting non empty list";
        return true;
    }),
    DEADLINE_WRONG_TIME("(?i)^deadline .+ /by .+", (i, s) -> {
        Printer.printError("The date must be in the format 'DD/MM/YYYY HHMM'");
        return true;
    }),
    UNKNOWN(".*", (i, s) -> {
        Printer.printError("I'm sorry but I don't know what that means :-(");
        return true;
    });

    private String regex;
    private ResponseFunc func;

    Response(String r, ResponseFunc f) {
        regex = r;
        func = f;
    }

    /**
     * Given a string and program state, if string matches regex this enum will call its response
     * function.
     *
     * @param i input string
     * @param s state object
     * @return boolean if the string has matched
     */
    public boolean call(String i, State s) {
        if (i.matches(regex)) {
            return func.funcCall(i, s);
        }
        return false;
    }

    /**
     * Assuming input is ".+ [0-9]+", it splits at whitespace and returns an Integer of the second
     * part of the string.
     *
     * @param input input string
     * @return integer of second part of string
     */
    private static int getNumber(String input) {
        return Integer.parseInt(input.split(" ", 2)[1]);
    }

    /**
     * Given an index and state object, check if index in bounds of list in state object.
     *
     * @param index index to check
     * @param s     state object
     * @return False if out of bounds
     */
    private static boolean checkValidIndex(int index, State s) {
        if (index < 0 || index > s.list.size() - 1) {
            Printer.printError("That is not a valid task index");
            return false;
        }
        return true;
    }

    /**
     * Assuming input is "(head).*(mid).*" returns the two texts between them.
     *
     * @param input input string
     * @param head  head regex match
     * @param mid   mid regex match
     * @return array of two text parts
     */
    private static String[] splitTwoDelimiters(String input, String head, String mid) {
        String[] parts = input.split(mid, 2);
        parts[0] = parts[0].split(head)[1];
        return parts;
    }

    /**
     * Insert task into list and prints message string.
     *
     * @param t task to be added
     * @param s state object
     */
    private static void addTask(DoableTask t, State s) {
        s.list.add(t);
        Printer.printString("Got it. I've added this task:\n  "
                + t.toString()
                + "\nNow you have " + s.list.size()
                + " tasks in the list.");
        save(s);
    }

    private static String listIndexStreamToString(Stream<Integer> indices, State s) {
        return indices.reduce("", (acc, ti) -> acc
                + ((acc.equalsIgnoreCase("") ? "" : "\n")
                + (ti + 1)
                + "."
                + s.list.get(ti).toString()), String::concat);
    }

    /**
     * Save state to JsonFile.
     * @param s state
     */
    private static void save(State s) {
        SaveFile.write(s.list);
    }
}