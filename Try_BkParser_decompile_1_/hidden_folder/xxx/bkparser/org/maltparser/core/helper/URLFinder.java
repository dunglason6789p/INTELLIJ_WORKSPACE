package org.maltparser.core.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.plugin.Plugin;
import org.maltparser.core.plugin.PluginLoader;

public class URLFinder {
   public URLFinder() {
   }

   public URL findURL(String fileString) throws MaltChainedException {
      File specFile = new File(fileString);

      try {
         if (specFile.exists()) {
            return new URL("file:///" + specFile.getAbsolutePath());
         } else {
            return !fileString.startsWith("http:") && !fileString.startsWith("file:") && !fileString.startsWith("ftp:") && !fileString.startsWith("jar:") ? this.findURLinJars(fileString) : new URL(fileString);
         }
      } catch (MalformedURLException var4) {
         throw new MaltChainedException("Malformed URL: " + fileString, var4);
      }
   }

   public URL findURLinJars(String fileString) throws MaltChainedException {
      try {
         if (this.getClass().getResource(fileString) != null) {
            return this.getClass().getResource(fileString);
         } else {
            Iterator i$ = PluginLoader.instance().iterator();

            while(i$.hasNext()) {
               Plugin plugin = (Plugin)i$.next();
               URL url = null;
               if (!fileString.startsWith("/")) {
                  url = new URL("jar:" + plugin.getUrl() + "!/" + fileString);
               } else {
                  url = new URL("jar:" + plugin.getUrl() + "!" + fileString);
               }

               try {
                  InputStream is = url.openStream();
                  is.close();
                  return url;
               } catch (IOException var6) {
               }
            }

            return null;
         }
      } catch (MalformedURLException var7) {
         throw new MaltChainedException("Malformed URL: " + fileString, var7);
      }
   }
}
