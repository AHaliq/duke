package com.util.json;

import com.util.Printer;
import java.util.HashMap;

public class JsonObject extends HashMap<String,JsonValue> {

    public JsonValue put(String key, int value) {
        return super.put(key, new JsonValue(value));
    }

    public JsonValue put(String key, double value) {
        return super.put(key, new JsonValue(value));
    }

    public JsonValue put(String key, boolean value) {
        return super.put(key, new JsonValue(value));
    }

    public JsonValue put(String key, String value) {
        return super.put(key, new JsonValue(value));
    }

    public JsonValue put(String key, JsonObject value) {
        return super.put(key, new JsonValue(value));
    }

    public JsonValue put(String key, JsonArray value) {
        return super.put(key, new JsonValue(value));
    }

    /**
     * Returns string representation of JsonObject.
     * @return  string representation
     */
    public String toString() {
        StringBuilder formattedString = new StringBuilder("");
        boolean empty = true;
        for (HashMap.Entry<String, JsonValue> entry : entrySet()) {
            if (!empty) {
                formattedString.append(",\n");
            } else {
                empty = false;
            }
            String key = JsonParser.formatStringForJson(entry.getKey());
            String value = entry.getValue().toString();
            formattedString.append(key).append(": ").append(value);
        }
        return "{\n" + Printer.indentString(formattedString.toString()) + "}";
    }
}
