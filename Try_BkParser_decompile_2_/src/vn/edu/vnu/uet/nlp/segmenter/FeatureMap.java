/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.segmenter;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Set;

public class FeatureMap {
    private HashMap<String, Integer> map = new HashMap(100000);

    public void clear() {
        this.map.clear();
    }

    public Integer getIndex(String feature, int mode) {
        if (!this.map.containsKey(feature = feature.intern())) {
            if (mode == 1 || mode == 2) {
                return this.map.size();
            }
            this.map.put(feature, new Integer(this.map.size()));
            if (this.getSize() % 50000 == 0 && this.getSize() > 0) {
                System.out.println("\t\t\t\t\t\tNumber of unique features: " + this.getSize());
            }
            return this.map.size() - 1;
        }
        return (int)this.map.get(feature);
    }

    public int getSize() {
        return this.map.size();
    }

    public void load(String path) throws IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fin);
        this.map = (HashMap)ois.readObject();
        ois.close();
    }

    public void save(String path) throws IOException {
        Path filePath = Paths.get(path, new String[0]);
        BufferedWriter file = Files.newBufferedWriter(filePath, Charset.forName("utf-8"), StandardOpenOption.CREATE);
        file.close();
        FileOutputStream fout = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(this.map);
        oos.close();
    }

    public String toString() {
        Set<String> keys = this.map.keySet();
        StringBuffer sb = new StringBuffer();
        sb.append("Feature map size: " + this.map.size() + "\n");
        for (String key : keys) {
            sb.append(key + "\t" + this.map.get(key) + "\n");
        }
        return sb.toString();
    }
}

