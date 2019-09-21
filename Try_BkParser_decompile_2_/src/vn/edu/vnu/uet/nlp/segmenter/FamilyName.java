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

public class FamilyName {
    private static Set<String> nameList;
    private static String path;

    private static void getInstance() {
        nameList = new HashSet<String>();
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
            nameList = (Set)ois.readObject();
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

    public static boolean isVNFamilyName(String syl) {
        if (nameList == null) {
            FamilyName.getInstance();
        }
        return nameList.contains(syl.trim().toLowerCase());
    }

    public static void setPath(String _path) {
        path = _path;
    }

    static {
        path = "dictionary/VNFamilyNameObject";
    }
}

