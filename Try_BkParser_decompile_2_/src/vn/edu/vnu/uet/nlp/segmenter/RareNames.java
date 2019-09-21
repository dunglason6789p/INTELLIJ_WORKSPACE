/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class RareNames {
    private static Set<String> list;
    private static String path;

    private static void getInstance() throws IOException {
        list = new HashSet<String>();
        Path p = Paths.get(path, new String[0]);
        BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8);
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.isEmpty()) continue;
            list.add(line.toLowerCase().trim());
        }
    }

    public static boolean isRareName(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        if (list == null) {
            try {
                RareNames.getInstance();
            }
            catch (IOException e) {
                System.err.println("The dictionary of rare names 'dictionary/rare_names.txt' is not found!");
                return false;
            }
        }
        return list.contains(word.trim().toLowerCase());
    }

    public static void setPath(String _path) {
        path = _path;
    }

    static {
        path = "dictionary/rare_names.txt";
    }
}

