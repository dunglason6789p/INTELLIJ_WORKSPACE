/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

public class Dictionary {
    private static Set<String> dict;
    private static String path;

    private static void getInstance() {
        dict = new HashSet<String>();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(path);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(fin);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dict = (Set)ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ois.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean inVNDict(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        if (dict == null) {
            Dictionary.getInstance();
        }
        return dict.contains(word.trim().toLowerCase());
    }

    public static void setPath(String _path) {
        path = _path;
    }

    static {
        path = "dictionary/VNDictObject";
    }
}

