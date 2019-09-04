package com.util.json;

import com.util.Printer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SaveData {

    private static final String SAVE_FILE = "./data/duke.json";

    public static void write(String content) {
        write(SAVE_FILE, content);
    }

    /**
     * Given a path and content, makes directories if don't exist then writes the file.
     *
     * @param path    path to file
     * @param content content to write
     */
    public static void write(String path, String content) {
        File file = new File(path);
        file.getParentFile().mkdirs();

        try {
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static String read() {
        try {
            return read(SAVE_FILE);
        } catch (FileNotFoundException e) {
            return "";
        }
    }

    /**
     * Given a path, attempts to read contents of file and return it as string.
     * @param path  path to file
     * @return      file content string
     * @throws FileNotFoundException    file does not exist
     */
    public static String read(String path) throws FileNotFoundException {
        File file = new File(path);
        if (file.exists()) {
            FileReader fr = new FileReader(file.getAbsoluteFile());
            BufferedReader br = new BufferedReader(fr);

            try {
                StringBuilder sb = new StringBuilder();
                String line;
                boolean empty = true;
                while ((line = br.readLine()) != null) {
                    if (!empty) {
                        sb.append("\n");
                    }
                    sb.append(line);
                    empty = false;
                }
                br.close();
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

        }
        return "";
    }
}