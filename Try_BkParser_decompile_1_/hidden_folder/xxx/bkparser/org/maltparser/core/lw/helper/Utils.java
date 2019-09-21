package org.maltparser.core.lw.helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public final class Utils {
   public Utils() {
   }

   public static JarFile getConfigJarfile(URL url) {
      JarFile mcoFile = null;

      try {
         JarURLConnection conn = (JarURLConnection)(new URL("jar:" + url.toString() + "!/")).openConnection();
         mcoFile = conn.getJarFile();
      } catch (IOException var3) {
         var3.printStackTrace();
      }

      return mcoFile;
   }

   public static JarEntry getConfigFileEntry(JarFile mcoJarFile, String mcoName, String fileName) {
      JarEntry entry = mcoJarFile.getJarEntry(mcoName + '/' + fileName);
      if (entry == null) {
         entry = mcoJarFile.getJarEntry(mcoName + '\\' + fileName);
      }

      return entry;
   }

   public static InputStream getInputStreamFromConfigFileEntry(URL mcoURL, String mcoName, String fileName) {
      JarFile mcoJarFile = getConfigJarfile(mcoURL);
      JarEntry entry = getConfigFileEntry(mcoJarFile, mcoName, fileName);

      try {
         if (entry == null) {
            throw new FileNotFoundException();
         }

         return mcoJarFile.getInputStream(entry);
      } catch (FileNotFoundException var6) {
         var6.printStackTrace();
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      return null;
   }

   public static InputStreamReader getInputStreamReaderFromConfigFileEntry(URL mcoURL, String mcoName, String fileName, String charSet) {
      try {
         return new InputStreamReader(getInputStreamFromConfigFileEntry(mcoURL, mcoName, fileName), charSet);
      } catch (UnsupportedEncodingException var5) {
         var5.printStackTrace();
         return null;
      }
   }

   public static URL getConfigFileEntryURL(URL mcoURL, String mcoName, String fileName) {
      try {
         URL url = new URL("jar:" + mcoURL.toString() + "!/" + mcoName + '/' + fileName + "\n");

         try {
            InputStream is = url.openStream();
            is.close();
         } catch (IOException var5) {
            url = new URL("jar:" + mcoURL.toString() + "!/" + mcoName + '\\' + fileName + "\n");
         }

         return url;
      } catch (MalformedURLException var6) {
         var6.printStackTrace();
         return null;
      }
   }

   public static String getInternalParserModelName(URL mcoUrl) {
      String internalParserModelName = null;

      try {
         JarInputStream jis = new JarInputStream(mcoUrl.openConnection().getInputStream());

         JarEntry je;
         while((je = jis.getNextJarEntry()) != null) {
            String fileName = je.getName();
            jis.closeEntry();
            int index = fileName.indexOf(47);
            if (index == -1) {
               index = fileName.indexOf(92);
            }

            if (internalParserModelName == null) {
               internalParserModelName = fileName.substring(0, index);
               break;
            }
         }

         jis.close();
      } catch (IOException var6) {
         var6.printStackTrace();
      }

      return internalParserModelName;
   }

   public static String[] toStringArray(DependencyGraph graph, DataFormatInstance dataFormatInstance, SymbolTableHandler symbolTables) throws MaltChainedException {
      String[] tokens = new String[graph.nTokenNode()];
      StringBuilder sb = new StringBuilder();
      Iterator<ColumnDescription> columns = dataFormatInstance.iterator();
      Iterator i$ = graph.getTokenIndices().iterator();

      label43:
      while(true) {
         Integer index;
         do {
            if (!i$.hasNext()) {
               return tokens;
            }

            index = (Integer)i$.next();
            sb.setLength(0);
         } while(index > tokens.length);

         ColumnDescription column = null;
         TokenNode node = graph.getTokenNode(index);

         while(true) {
            do {
               if (!columns.hasNext()) {
                  sb.setLength(sb.length() - 1);
                  tokens[index - 1] = sb.toString();
                  columns = dataFormatInstance.iterator();
                  continue label43;
               }

               column = (ColumnDescription)columns.next();
            } while(column.getCategory() != 1);

            if (!column.getName().equals("ID")) {
               if (node.hasLabel(symbolTables.getSymbolTable(column.getName())) && node.getLabelSymbol(symbolTables.getSymbolTable(column.getName())).length() > 0) {
                  sb.append(node.getLabelSymbol(symbolTables.getSymbolTable(column.getName())));
               } else {
                  sb.append('_');
               }
            } else {
               sb.append(index.toString());
            }

            sb.append('\t');
         }
      }
   }
}
