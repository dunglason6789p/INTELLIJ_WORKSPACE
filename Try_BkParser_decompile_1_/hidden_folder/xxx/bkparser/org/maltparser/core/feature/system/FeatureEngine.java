package org.maltparser.core.feature.system;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.plugin.Plugin;
import org.maltparser.core.plugin.PluginLoader;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FeatureEngine extends HashMap<String, FunctionDescription> {
   public static final long serialVersionUID = 3256444702936019250L;

   public FeatureEngine() {
   }

   public Function newFunction(String functionName, FeatureRegistry registry) throws MaltChainedException {
      int i = 0;
      Function func = null;

      while(true) {
         FunctionDescription funcDesc = (FunctionDescription)this.get(functionName + "~~" + i);
         if (funcDesc == null) {
            break;
         }

         func = funcDesc.newFunction(registry);
         if (func != null) {
            break;
         }

         ++i;
      }

      return func;
   }

   public void load(String urlstring) throws MaltChainedException {
      URLFinder f = new URLFinder();
      this.load(f.findURL(urlstring));
   }

   public void load(PluginLoader plugins) throws MaltChainedException {
      Iterator i$ = plugins.iterator();

      while(i$.hasNext()) {
         Plugin plugin = (Plugin)i$.next();
         URL url = null;

         try {
            url = new URL("jar:" + plugin.getUrl() + "!/appdata/plugin.xml");
         } catch (MalformedURLException var6) {
            throw new FeatureException("Malformed URL: 'jar:" + plugin.getUrl() + "!plugin.xml'", var6);
         }

         try {
            InputStream is = url.openStream();
            is.close();
         } catch (IOException var7) {
            continue;
         }

         this.load(url);
      }

   }

   public void load(URL specModelURL) throws MaltChainedException {
      try {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Element root = null;
         root = db.parse(specModelURL.openStream()).getDocumentElement();
         if (root == null) {
            throw new FeatureException("The feature system file '" + specModelURL.getFile() + "' cannot be found. ");
         } else {
            this.readFeatureSystem(root);
         }
      } catch (IOException var5) {
         throw new FeatureException("The feature system file '" + specModelURL.getFile() + "' cannot be found. ", var5);
      } catch (ParserConfigurationException var6) {
         throw new FeatureException("Problem parsing the file " + specModelURL.getFile() + ". ", var6);
      } catch (SAXException var7) {
         throw new FeatureException("Problem parsing the file " + specModelURL.getFile() + ". ", var7);
      }
   }

   public void readFeatureSystem(Element system) throws MaltChainedException {
      NodeList functions = system.getElementsByTagName("function");

      for(int i = 0; i < functions.getLength(); ++i) {
         this.readFunction((Element)functions.item(i));
      }

   }

   public void readFunction(Element function) throws MaltChainedException {
      boolean hasSubFunctions = function.getAttribute("hasSubFunctions").equalsIgnoreCase("true");
      boolean hasFactory = false;
      if (function.getAttribute("hasFactory").length() > 0) {
         hasFactory = function.getAttribute("hasFactory").equalsIgnoreCase("true");
      }

      Class clazz = null;

      try {
         if (PluginLoader.instance() != null) {
            clazz = PluginLoader.instance().getClass(function.getAttribute("class"));
         }

         if (clazz == null) {
            clazz = Class.forName(function.getAttribute("class"));
         }
      } catch (ClassNotFoundException var7) {
         throw new FeatureException("The feature system could not find the function class" + function.getAttribute("class") + ".", var7);
      }

      if (hasSubFunctions) {
         NodeList subfunctions = function.getElementsByTagName("subfunction");

         for(int i = 0; i < subfunctions.getLength(); ++i) {
            this.readSubFunction((Element)subfunctions.item(i), clazz, hasFactory);
         }
      } else {
         int i = 0;
         String n = null;

         while(true) {
            n = function.getAttribute("name") + "~~" + i;
            if (!this.containsKey(n)) {
               this.put(n, new FunctionDescription(function.getAttribute("name"), clazz, false, hasFactory));
               break;
            }

            ++i;
         }
      }

   }

   public void readSubFunction(Element subfunction, Class<?> clazz, boolean hasFactory) throws MaltChainedException {
      int i = 0;
      String n = null;

      while(true) {
         n = subfunction.getAttribute("name") + "~~" + i;
         if (!this.containsKey(n)) {
            this.put(n, new FunctionDescription(subfunction.getAttribute("name"), clazz, true, hasFactory));
            return;
         }

         ++i;
      }
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else if (this.size() != ((FeatureEngine)obj).size()) {
         return false;
      } else {
         Iterator i$ = this.keySet().iterator();

         String name;
         do {
            if (!i$.hasNext()) {
               return true;
            }

            name = (String)i$.next();
         } while(((FunctionDescription)this.get(name)).equals(((FeatureEngine)obj).get(name)));

         return false;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.keySet().iterator();

      while(i$.hasNext()) {
         String name = (String)i$.next();
         sb.append(name);
         sb.append('\t');
         sb.append(this.get(name));
         sb.append('\n');
      }

      return sb.toString();
   }
}
