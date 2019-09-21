package org.maltparser.core.lw.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.helper.HashSet;

public final class McoModel {
   private final URL mcoUrl;
   private final Map<String, URL> nameUrlMap;
   private final Map<String, Object> preLoadedObjects;
   private final Map<String, String> preLoadedStrings;
   private final URL infoURL;
   private final String internalMcoName;

   public McoModel(URL _mcoUrl) {
      this.mcoUrl = _mcoUrl;
      this.nameUrlMap = Collections.synchronizedMap(new HashMap());
      this.preLoadedObjects = Collections.synchronizedMap(new HashMap());
      this.preLoadedStrings = Collections.synchronizedMap(new HashMap());
      URL tmpInfoURL = null;
      String tmpInternalMcoName = null;

      try {
         JarEntry je;
         JarInputStream jis;
         for(jis = new JarInputStream(this.mcoUrl.openConnection().getInputStream()); (je = jis.getNextJarEntry()) != null; jis.closeEntry()) {
            String fileName = je.getName();
            URL entryURL = new URL("jar:" + this.mcoUrl + "!/" + fileName + "\n");
            int index = fileName.indexOf(47);
            if (index == -1) {
               index = fileName.indexOf(92);
            }

            this.nameUrlMap.put(fileName.substring(index + 1), entryURL);
            if (fileName.endsWith(".info") && tmpInfoURL == null) {
               tmpInfoURL = entryURL;
            } else if (!fileName.endsWith(".moo") && !fileName.endsWith(".map")) {
               if (fileName.endsWith(".dsm")) {
                  this.preLoadedStrings.put(fileName.substring(index + 1), this.preLoadString(entryURL.openStream()));
               }
            } else {
               this.preLoadedObjects.put(fileName.substring(index + 1), this.preLoadObject(entryURL.openStream()));
            }

            if (tmpInternalMcoName == null) {
               tmpInternalMcoName = fileName.substring(0, index);
            }
         }

         jis.close();
      } catch (IOException var9) {
         var9.printStackTrace();
      } catch (ClassNotFoundException var10) {
         var10.printStackTrace();
      }

      this.internalMcoName = tmpInternalMcoName;
      this.infoURL = tmpInfoURL;
   }

   private Object preLoadObject(InputStream is) throws IOException, ClassNotFoundException {
      Object object = null;
      ObjectInputStream input = new ObjectInputStream(is);

      try {
         object = input.readObject();
      } finally {
         input.close();
      }

      return object;
   }

   private String preLoadString(InputStream is) throws IOException, ClassNotFoundException {
      BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      StringBuilder sb = new StringBuilder();

      String line;
      while((line = in.readLine()) != null) {
         sb.append(line);
         sb.append('\n');
      }

      return sb.toString();
   }

   public InputStream getInputStream(String fileName) throws IOException {
      return ((URL)this.nameUrlMap.get(fileName)).openStream();
   }

   public InputStreamReader getInputStreamReader(String fileName, String charSet) throws IOException, UnsupportedEncodingException {
      return new InputStreamReader(this.getInputStream(fileName), charSet);
   }

   public URL getMcoEntryURL(String fileName) throws MalformedURLException {
      return new URL(((URL)this.nameUrlMap.get(fileName)).toString());
   }

   public URL getMcoURL() throws MalformedURLException {
      return new URL(this.mcoUrl.toString());
   }

   public Object getMcoEntryObject(String fileName) {
      return this.preLoadedObjects.get(fileName);
   }

   public Set<String> getMcoEntryObjectKeys() {
      return Collections.synchronizedSet(new HashSet(this.preLoadedObjects.keySet()));
   }

   public String getMcoEntryString(String fileName) {
      return (String)this.preLoadedStrings.get(fileName);
   }

   public String getInternalName() {
      return this.internalMcoName;
   }

   public String getMcoURLString() {
      return this.mcoUrl.toString();
   }

   public String getMcoInfo() throws IOException {
      StringBuilder sb = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(this.infoURL.openStream(), "UTF-8"));

      String line;
      while((line = reader.readLine()) != null) {
         sb.append(line);
         sb.append('\n');
      }

      return sb.toString();
   }
}
