package com.util.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SaveData {

    private static final String CURRENT_PATH = System.getProperty("user.dir");
    private static final String DIRECTORY = "/data";
    private static final String FILE_NAME = "duke.json";

    /**
     * Write file and create directory and file if necessary.
     * @param str   string to write to save data file
     */
    public static void write(String str) {
        File directory = new File(CURRENT_PATH + DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }
        // make directory

        File file = new File(CURRENT_PATH + DIRECTORY + "/" + FILE_NAME);
        try {
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Read file if exists otherwise empty string.
     * @return  file contents as string or empty string if doesn't exist
     */
    public static String read() {
        File file = new File(CURRENT_PATH + DIRECTORY + "/" + FILE_NAME);
        try {
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
        } catch (FileNotFoundException ignored) {
            System.out.print("");
        }
        return "";
    }
}