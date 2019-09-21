/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BuildSyllableSet {
    private static Map<String, String> normalizationMap = new HashMap<String, String>();
    private static Set<String> normalizationSet;

    public static void main(String[] args) throws IOException {
        String line;
        Path file = Paths.get("/home/tuanphong94/workspace/dictionary/VNsyl.txt", new String[0]);
        BufferedReader br = Files.newBufferedReader(file, Charset.forName("utf-8"));
        HashSet<String> set = new HashSet<String>();
        while ((line = br.readLine()) != null) {
            line = line.replace("\u00a0", " ");
            line = BuildSyllableSet.normalize(line.toLowerCase());
            set.add(line.trim());
        }
        System.out.println(set.size());
        String path = "dictionary/VNsylObject";
        Path filePath = Paths.get(path, new String[0]);
        BufferedWriter obj = Files.newBufferedWriter(filePath, Charset.forName("utf-8"), StandardOpenOption.CREATE);
        obj.close();
        FileOutputStream fout = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(set);
        oos.close();
    }

    public static String normalize(String token) {
        for (String regex : normalizationSet) {
            if (!token.contains(regex)) continue;
            token = token.replace(regex, normalizationMap.get(regex));
            break;
        }
        return token;
    }

    static {
        normalizationMap.put("\u00f2a", "o\u00e0");
        normalizationMap.put("\u00f3a", "o\u00e1");
        normalizationMap.put("\u1ecfa", "o\u1ea3");
        normalizationMap.put("\u00f5a", "o\u00e3");
        normalizationMap.put("\u1ecda", "o\u1ea1");
        normalizationMap.put("\u00f2e", "o\u00e8");
        normalizationMap.put("\u00f3e", "o\u00e9");
        normalizationMap.put("\u1ecfe", "o\u1ebb");
        normalizationMap.put("\u00f5e", "o\u1ebd");
        normalizationMap.put("\u1ecde", "o\u1eb9");
        normalizationMap.put("\u00f9y", "u\u1ef3");
        normalizationMap.put("\u00fay", "u\u00fd");
        normalizationMap.put("\u1ee7y", "u\u1ef7");
        normalizationMap.put("\u0169y", "u\u1ef9");
        normalizationMap.put("\u1ee5y", "u\u1ef5");
        normalizationMap.put("\u1ee6y", "U\u1ef7");
        normalizationSet = normalizationMap.keySet();
    }
}

