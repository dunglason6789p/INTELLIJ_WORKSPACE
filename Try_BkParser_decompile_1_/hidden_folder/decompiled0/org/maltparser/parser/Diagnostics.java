/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import org.maltparser.core.exception.MaltChainedException;

public class Diagnostics {
    protected final BufferedWriter diaWriter;

    public Diagnostics(String fileName) throws MaltChainedException {
        try {
            this.diaWriter = fileName.equals("stdout") ? new BufferedWriter(new OutputStreamWriter(System.out)) : (fileName.equals("stderr") ? new BufferedWriter(new OutputStreamWriter(System.err)) : new BufferedWriter(new FileWriter(fileName)));
        }
        catch (IOException e) {
            throw new MaltChainedException("Could not open the diagnostic file. ", e);
        }
    }

    public BufferedWriter getDiaWriter() {
        return this.diaWriter;
    }

    public void writeToDiaFile(String message) throws MaltChainedException {
        try {
            this.getDiaWriter().write(message);
        }
        catch (IOException e) {
            throw new MaltChainedException("Could not write to the diagnostic file. ", e);
        }
    }

    public void closeDiaWriter() throws MaltChainedException {
        if (this.diaWriter != null) {
            try {
                this.diaWriter.flush();
                this.diaWriter.close();
            }
            catch (IOException e) {
                throw new MaltChainedException("Could not close the diagnostic file. ", e);
            }
        }
    }
}

