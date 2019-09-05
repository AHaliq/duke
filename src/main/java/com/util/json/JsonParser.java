package com.util.json;

import com.tasks.Deadline;
import com.tasks.DoableTask;
import com.tasks.Event;
import com.tasks.TaskTypes;
import com.tasks.Todo;
import com.util.Printer;
import com.util.datetime.DateTime;
import java.util.ArrayList;

public class JsonParser {

    /**
     * Given save file's input string, generate list. Empty list if erroneous or empty string does
     * not exist.
     *
     * @param input json file string
     * @return list
     */
    public static ArrayList<DoableTask> parseJsonFile(String input) {
        if (input.length() == 0) {
            return new ArrayList<>();
        }
        try {
            try {
                for (JsonValue obj : processDynamicValue(input.toCharArray(), 0).snd.getArray()) {
                    JsonValue attrObj;
                    if ((attrObj = obj.getObject().get(Schema.TASK_TODO)) != null) {

                    } else if ((attrObj = obj.getObject().get(Schema.TASK_EVENT)) != null) {

                    }
                }
            } catch (JsonWrongValueTypeException e) {
                Printer.printError(e.getMessage());
            }
        } catch (JsonFormatException e) {
            Printer.printError("Save File has errors\n" + e.getMessage());
        }
        try {
            ArrayList<DoableTask> arr = new ArrayList<>();
            for (JsonValue o : parseJsonArray(input.toCharArray(), 0).snd) {
                if (o.getType() != ValueTypes.OBJECT) {
                    throw new JsonFormatException(
                            "JSON array immediate elements must be tasks");
                } else {
                    JsonObject obj = o.getObject();
                    JsonValue task;
                    JsonObject attributes;
                    TaskTypes type;
                    if ((task = obj.get(Schema.TASK_TODO)) != null) {
                        type = TaskTypes.TODO;
                    } else if ((task = obj.get(Schema.TASK_EVENT)) != null) {
                        type = TaskTypes.EVENT;
                    } else if ((task = obj.get(Schema.TASK_DEADLINE)) != null) {
                        type = TaskTypes.DEADLINE;
                    } else {
                        throw new JsonFormatException("Unexpected task type");
                    }
                    if (task.getType() != ValueTypes.OBJECT) {
                        throw new JsonFormatException("Invalid task format");
                    }
                    attributes = task.getObject();
                    // parse task object

                    DoableTask t = null;
                    String name;
                    boolean isDone;
                    if (attributes.get(Schema.ATTR_NAME) != null
                            && attributes.get(Schema.ATTR_NAME).getType() == ValueTypes.STRING) {
                        name = attributes.get(Schema.ATTR_NAME).getString();
                    } else {
                        throw new JsonFormatException("No name field in task");
                    }
                    // parse name
                    if (attributes.get(Schema.ATTR_DONE) != null
                            && attributes.get(Schema.ATTR_DONE).getType() == ValueTypes.BOOLEAN) {
                        isDone = attributes.get(Schema.ATTR_DONE).getBoolean();
                    } else {
                        throw new JsonFormatException("No done field in task");
                    }
                    // parse done
                    switch (type) {
                    case TODO:
                        t = new Todo(name);
                        break;
                    case EVENT:
                        if (attributes.get(Schema.ATTR_EVENT_START) != null
                                && attributes.get(Schema.ATTR_EVENT_START).getType()
                                == ValueTypes.STRING
                                && attributes.get(Schema.ATTR_EVENT_END) != null
                                && attributes.get(Schema.ATTR_EVENT_END).getType()
                                == ValueTypes.STRING) {
                            t = new Event(name,
                                    DateTime.parseString(
                                            attributes.get(Schema.ATTR_EVENT_START).getString()),
                                    DateTime.parseString(
                                            attributes.get(Schema.ATTR_EVENT_END).getString()));
                        }
                        break;
                    case DEADLINE:
                        if (attributes.get(Schema.ATTR_DEADLINE_DUE) != null
                                && attributes.get(Schema.ATTR_DEADLINE_DUE).getType()
                                == ValueTypes.STRING) {
                            t = new Deadline(name, DateTime.parseString(
                                    attributes.get(Schema.ATTR_DEADLINE_DUE).getString()));
                        }
                        break;
                    default:
                        throw new JsonFormatException("Unexpected task type");
                    }

                    if (t == null) {
                        throw new JsonFormatException("Invalid task format");
                    } else {
                        if (isDone) {
                            t.markAsDone();
                        }
                        arr.add(t);
                    }
                    // parse task attributes object
                }
            }
            //TODO specify error content in error message
            return arr;
        } catch (JsonFormatException e) {
            Printer.printError(
                    "Your save file is not in the expected format\n error: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Parses from input[i] onwards as a json array.
     *
     * @param input input character array
     * @param i     index to start parsing
     * @return resulting index and array
     * @throws JsonFormatException file format error
     */
    private static Pair<Integer,JsonArray> parseJsonArray(char[] input, int i)
            throws JsonFormatException {
        JsonArray arr = new JsonArray()
        i = skipWhiteSpace(input, i);
        if (input[i] != '[') {
            throw new JsonFormatException("Expecting [ at " + i, 2);
        }
        // find '['

        i = skipWhiteSpace(input, i + 1);
        if (i >= input.length) {
            throw new JsonFormatException("Empty array did not close");
        }
        // search first value

        boolean isFirst = true;
        while (input[i] != ']') {
            if (!isFirst) {
                i = skipWhiteSpace(input, i);
                if (input[i] != ',') {
                    throw new JsonFormatException("Value pairs must be comma separated");
                }
                i = skipWhiteSpace(input, i + 1);
            }
            // advance ',' between value pairs

            Pair<Integer,JsonValue> valuePair = processDynamicValue(input, i);
            i = skipWhiteSpace(input, valuePair.fst);
            arr.add(valuePair.snd);
            // parse value

            isFirst = false;
        }
        // process key value pairs

        i = skipWhiteSpace(input, i);
        if (i >= input.length || input[i] != ']') {
            throw new JsonFormatException("Array did not close");
        }
        // find ']'
        return new Pair<>(i + 1, arr);
    }

    /**
     * Parses from input[i] onwards as a json object.
     *
     * @param input input character array
     * @param i     index to start parsing
     * @return resulting index and object / key value pairs HashMap
     * @throws JsonFormatException file format error
     */
    private static Pair<Integer,JsonObject> parseJsonObject(char[] input, int i)
            throws JsonFormatException {
        JsonObject obj = new JsonObject();
        i = skipWhiteSpace(input, i);
        if (input[i] != '{') {
            throw new JsonFormatException("Expecting { at " + i, 2);
        }
        // find '{'

        i = skipWhiteSpace(input, i + 1);
        if (i >= input.length) {
            throw new JsonFormatException("Empty object did not close");
        }
        // search first key

        boolean isFirst = true;
        while (input[i] != '}') {
            if (!isFirst) {
                i = skipWhiteSpace(input, i);
                if (input[i] != ',') {
                    throw new JsonFormatException("Key value pairs must be comma separated");
                }
                i = skipWhiteSpace(input, i + 1);
            }
            // advance ',' between key value pairs

            String key;
            try {
                Pair<Integer,String> keyPair = parseJsonString(input, i);
                i = keyPair.fst;
                key = keyPair.snd;
            } catch (JsonFormatException ignored) {
                throw new JsonFormatException("Object keys must be strings at " + i);
            }
            // parse key

            skipWhiteSpace(input, i);
            if (input[i] != ':') {
                throw new JsonFormatException("Expected : after key name at " + i);
            }
            i = skipWhiteSpace(input, i + 1);
            // find ':'

            Pair<Integer,JsonValue> valuePair = processDynamicValue(input, i);
            i = skipWhiteSpace(input, valuePair.fst);
            obj.put(key, valuePair.snd);
            // parse value

            isFirst = false;
        }

        i = skipWhiteSpace(input, i);
        if (i >= input.length || input[i] != '}') {
            throw new JsonFormatException("Object did not close");
        }
        // find '}'
        return new Pair<>(i + 1, obj);
    }

    /**
     * Parses from input[i] onwards as a json value, which can be of type specified in ValueTypes
     * enum.
     *
     * @param input input character array
     * @param i     index to start parsing
     * @return resulting index and DynamicValue; algebraic sum type of all possible ValueTypes
     * @throws JsonFormatException file format error
     */
    private static Pair<Integer,JsonValue> processDynamicValue(char[] input, int i)
            throws JsonFormatException {
        Pair<Integer,JsonValue> obj;
        try {
            Pair<Integer,Integer> res1 = parseJsonInt(input, i);
            obj = new Pair<>(res1.fst, new JsonValue(res1.snd));
        } catch (JsonFormatException e1) {
            try {
                Pair<Integer,Double> res2 = parseJsonDouble(input, i);
                obj = new Pair<>(res2.fst, new JsonValue(res2.snd));
            } catch (JsonFormatException e2) {
                try {
                    Pair<Integer,Boolean> res3 = parseJsonBoolean(input, i);
                    obj = new Pair<>(res3.fst, new JsonValue(res3.snd));
                } catch (JsonFormatException e3) {
                    try {
                        Pair<Integer,String> res4 = parseJsonString(input, i);
                        obj = new Pair<>(res4.fst, new JsonValue(res4.snd));
                    } catch (JsonFormatException e4) {
                        try {
                            Pair<Integer,JsonObject> res5 = parseJsonObject(input,
                                    i);
                            obj = new Pair<>(res5.fst, new JsonValue(res5.snd));
                        } catch (JsonFormatException e5) {
                            try {
                                Pair<Integer,JsonArray> res6 = parseJsonArray(input,
                                        i);
                                obj = new Pair<>(res6.fst, new JsonValue(res6.snd));
                            } catch (JsonFormatException e6) {
                                if (e3.getErrorCode() == 2 && e4.getErrorCode() == 2
                                        && e5.getErrorCode() == 2 && e6.getErrorCode() == 2) {
                                    throw new JsonFormatException(
                                            "input at " + i + " is of unknown format");
                                } else if (e4.getErrorCode() == 2 && e5.getErrorCode() == 2
                                        && e6.getErrorCode() == 2) {
                                    throw e3;
                                } else if (e5.getErrorCode() == 2 && e6.getErrorCode() == 2) {
                                    throw e4;
                                } else if (e6.getErrorCode() == 2) {
                                    throw e5;
                                } else {
                                    throw e6;
                                }
                            }
                        }
                    }
                }
            }
        }
        return obj;
    }

    /**
     * Parses from input[i] onwards as an int.
     *
     * @param input input character array
     * @param i     index to start parsing
     * @return resulting index and int
     * @throws JsonFormatException file format error
     */
    private static Pair<Integer,Integer> parseJsonInt(char[] input, int i)
            throws JsonFormatException {
        StringBuilder value = new StringBuilder();
        while (i < input.length && Character.isDigit(input[i]) || input[i] == '-') {
            value.append(input[i]);
            i++;
        }
        if (value.length() > 0 && checkIfLegalAfterValue(input[i])) {
            try {
                return new Pair<>(i, Integer.parseInt(value.toString()));
            } catch (NumberFormatException ignored) {
                throw new JsonFormatException("String ending at " + i + " is not an Integer");
            }
        }
        throw new JsonFormatException(
                "Expected Integer but encountered something else at " + i);
    }

    /**
     * Parses from input[i] onwards as a double.
     *
     * @param input input character array
     * @param i     index to start parsing
     * @return resulting index and double
     * @throws JsonFormatException file format error
     */
    private static Pair<Integer,Double> parseJsonDouble(char[] input, int i)
            throws JsonFormatException {
        StringBuilder value = new StringBuilder();
        while (i < input.length && Character.isDigit(input[i]) || input[i] == 'e' || input[i] == '.'
                || input[i] == '-') {
            value.append(input[i]);
            i++;
        }
        if (value.length() > 0 && checkIfLegalAfterValue(input[i])) {
            try {
                return new Pair<>(i, Double.parseDouble(value.toString()));
            } catch (NumberFormatException ignored) {
                throw new JsonFormatException("String ending at " + i + " is not a Double");
            }
        }
        throw new JsonFormatException("Expected Double but encountered something else at " + i);
    }

    /**
     * Parses from input[i] onwards as a boolean.
     *
     * @param input input character array
     * @param i     index to start parsing
     * @return resulting index and boolean
     * @throws JsonFormatException file format error
     */
    private static Pair<Integer,Boolean> parseJsonBoolean(char[] input, int i)
            throws JsonFormatException {
        boolean value;
        if (input[i] != 't' && input[i] != 'f') {
            throw new JsonFormatException(
                    "Expected Boolean but encountered something else at " + i, 2);
        }
        if (i + 3 < input.length && input[i] == 't' && input[i + 1] == 'r'
                && input[i + 2] == 'u' && input[i + 3] == 'e') {
            value = true;
            i += 4;
        } else if (i + 4 < input.length && input[i] == 'f' && input[i + 1] == 'a'
                && input[i + 2] == 'l' && input[i + 3] == 's' && input[i + 4] == 'e') {
            value = false;
            i += 5;
        } else {
            throw new JsonFormatException(
                    "Expected Boolean but encountered something else at " + i);
        }

        if (!checkIfLegalAfterValue(input[i])) {
            throw new JsonFormatException(
                    "Expected Boolean but encountered something else at " + i);
        }

        return new Pair<>(i, value);
    }

    /**
     * Parses from input[i] onwards as a string with double quotes surround. Escaped double quotes
     * are also replaced with regular double quotes
     *
     * @param input input character array
     * @param i     index to start parsing
     * @return resulting index and string
     * @throws JsonFormatException file format error
     */
    private static Pair<Integer,String> parseJsonString(char[] input, int i)
            throws JsonFormatException {
        StringBuilder value = new StringBuilder();
        boolean escape = false;
        i = skipWhiteSpace(input, i);
        if (input[i] != '"') {
            throw new JsonFormatException(
                    "Expected starting double quotes for string but encountered something else at "
                            + i, 2);
        }
        i++;
        while (i < input.length) {
            if (input[i] == '"' && !escape) {
                i++;
                break;
            } else if (input[i] == '\\' && !escape) {
                escape = true;
            } else if (escape) {
                if (input[i] == '"') {
                    value.append(input[i]);
                } else if (input[i] == 'n') {
                    value.append('\n');
                } else {
                    value.append('\\');
                    value.append(input[i]);
                }
                escape = false;
            } else {
                value.append(input[i]);
            }
            i++;
        }
        if (i >= input.length) {
            throw new JsonFormatException("String did not terminate with double quotes");
        }
        if (!checkIfLegalAfterValue(input[i])) {
            throw new JsonFormatException(
                    "Expected string but encountered something else at " + i);
        }
        return new Pair<>(i, value.toString());
    }

    /**
     * Check if character is a legal possibility after non object, array values.
     *
     * @param c character to test
     * @return True if legal
     */
    private static boolean checkIfLegalAfterValue(char c) {
        return Character.isWhitespace(c) || c == ',' || c == ']' || c == '}' || c == ':';
    }

    /**
     * Skip whitespace input[i] onwards till non whitespace encountered.
     *
     * @param input input character array
     * @param i     index to start parsing
     * @return resulting index
     */
    private static int skipWhiteSpace(char[] input, int i) {
        while (i < input.length && Character.isWhitespace(input[i])) {
            i++;
        }
        return i;
    }

    /**
     * Replace instances of " and n as escape characters for formatting as json string.
     * @param str   string to format
     * @return      formatted string
     */
    public static String formatStringForJson(String str) {
        return str.replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\\n");
    }
}
