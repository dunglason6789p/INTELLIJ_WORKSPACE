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
import java.util.HashSet;
import vn.edu.vnu.uet.nlp.helper.BuildSyllableSet;

public class BuildDictionary {
    public static void main(String[] args) throws IOException {
        String line;
        Path file = Paths.get("/home/tuanphong94/workspace/dictionary/original_word_list.txt", new String[0]);
        BufferedReader br = Files.newBufferedReader(file, Charset.forName("utf-8"));
        HashSet<String> set = new HashSet<String>();
        while ((line = br.readLine()) != null) {
            line = line.replace("\u00a0", " ");
            String[] tokens = line.split("\\s+");
            line = "";
            for (String token : tokens) {
                line = line + BuildSyllableSet.normalize(token.toLowerCase()) + " ";
            }
            set.add(line.trim().toLowerCase());
        }
        System.out.println(set.size());
        String path = "dictionary/VNDictObject";
        Path filePath = Paths.get(path, new String[0]);
        BufferedWriter obj = Files.newBufferedWriter(filePath, Charset.forName("utf-8"), StandardOpenOption.CREATE);
        obj.close();
        FileOutputStream fout = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(set);
        oos.close();
    }
}

