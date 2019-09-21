/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import vn.edu.vnu.uet.nlp.utils.Constants;

public class FileUtils {
    public static final Charset UNICODE = Charset.forName("utf-8");

    public static void appendFile(List<String> lines, String pathname) {
        try {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathname, true)));){
                for (String line : lines) {
                    if (line.isEmpty() || line == null) continue;
                    out.println(line);
                }
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void truncateFile(String filename) throws IOException {
        Path file = Paths.get(filename, new String[0]);
        BufferedWriter bf = Files.newBufferedWriter(file, UNICODE, StandardOpenOption.CREATE);
        bf = Files.newBufferedWriter(file, UNICODE, StandardOpenOption.WRITE);
        bf = Files.newBufferedWriter(file, UNICODE, StandardOpenOption.TRUNCATE_EXISTING);
        bf.close();
    }

    public static BufferedWriter newUTF8BufferedWriterFromNewFile(String filename) throws IOException {
        BufferedWriter bw = Files.newBufferedWriter(Paths.get(filename, new String[0]), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        bw = Files.newBufferedWriter(Paths.get(filename, new String[0]), StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        bw = Files.newBufferedWriter(Paths.get(filename, new String[0]), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        return bw;
    }

    public static BufferedReader newUTF8BufferedReaderFromFile(String filename) throws IOException {
        return Files.newBufferedReader(Paths.get(filename, new String[0]), Constants.cs);
    }

    public static List<String> readFile(String filename) throws IOException {
        String line;
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader br = Files.newBufferedReader(Paths.get(filename, new String[0]), Constants.cs);
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            list.add(line.trim());
        }
        return list;
    }
}

