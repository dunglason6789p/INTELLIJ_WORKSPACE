package org.maltparser.core.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;

public interface Configuration {
   boolean isLoggerInfoEnabled();

   boolean isLoggerDebugEnabled();

   void logErrorMessage(String var1);

   void logInfoMessage(String var1);

   void logInfoMessage(char var1);

   void logDebugMessage(String var1);

   void writeInfoToConfigFile(String var1) throws MaltChainedException;

   OutputStreamWriter getOutputStreamWriter(String var1) throws MaltChainedException;

   OutputStreamWriter getAppendOutputStreamWriter(String var1) throws MaltChainedException;

   InputStreamReader getInputStreamReader(String var1) throws MaltChainedException;

   InputStream getInputStreamFromConfigFileEntry(String var1) throws MaltChainedException;

   URL getConfigFileEntryURL(String var1) throws MaltChainedException;

   File getFile(String var1) throws MaltChainedException;

   Object getConfigFileEntryObject(String var1) throws MaltChainedException;

   String getConfigFileEntryString(String var1) throws MaltChainedException;

   SymbolTableHandler getSymbolTables();

   Object getOptionValue(String var1, String var2) throws MaltChainedException;

   String getOptionValueString(String var1, String var2) throws MaltChainedException;
}
