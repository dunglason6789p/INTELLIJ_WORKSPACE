/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;

public interface Configuration {
    public boolean isLoggerInfoEnabled();

    public boolean isLoggerDebugEnabled();

    public void logErrorMessage(String var1);

    public void logInfoMessage(String var1);

    public void logInfoMessage(char var1);

    public void logDebugMessage(String var1);

    public void writeInfoToConfigFile(String var1) throws MaltChainedException;

    public OutputStreamWriter getOutputStreamWriter(String var1) throws MaltChainedException;

    public OutputStreamWriter getAppendOutputStreamWriter(String var1) throws MaltChainedException;

    public InputStreamReader getInputStreamReader(String var1) throws MaltChainedException;

    public InputStream getInputStreamFromConfigFileEntry(String var1) throws MaltChainedException;

    public URL getConfigFileEntryURL(String var1) throws MaltChainedException;

    public File getFile(String var1) throws MaltChainedException;

    public Object getConfigFileEntryObject(String var1) throws MaltChainedException;

    public String getConfigFileEntryString(String var1) throws MaltChainedException;

    public SymbolTableHandler getSymbolTables();

    public Object getOptionValue(String var1, String var2) throws MaltChainedException;

    public String getOptionValueString(String var1, String var2) throws MaltChainedException;
}

