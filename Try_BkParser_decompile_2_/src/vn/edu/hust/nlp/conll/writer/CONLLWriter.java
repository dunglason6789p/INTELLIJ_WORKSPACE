/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.hust.nlp.conll.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import vn.edu.hust.nlp.conll.model.Sentence;

public class CONLLWriter {
    private BufferedWriter writer;
    private boolean firstSentence = true;

    public CONLLWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    public void write(Sentence sentence) throws IOException {
        if (this.firstSentence) {
            this.firstSentence = false;
        } else {
            this.writer.write("\n");
        }
        this.writer.write(sentence.toString());
        this.writer.write(10);
    }

    public void close() throws IOException {
        this.writer.close();
    }
}

