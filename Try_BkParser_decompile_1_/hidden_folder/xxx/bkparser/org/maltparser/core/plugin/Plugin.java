package org.maltparser.core.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.maltparser.core.exception.MaltChainedException;

public class Plugin {
   private JarFile archive;
   private URL url;
   private String pluginName;

   public Plugin(String filename) throws MaltChainedException {
      this(new File(filename));
   }

   public Plugin(File file) throws MaltChainedException {
      try {
         this.setArchive(new JarFile(file));
         this.setUrl(new URL("file", (String)null, file.getAbsolutePath()));
         this.register();
      } catch (FileNotFoundException var3) {
         throw new PluginException("The file '" + file.getPath() + File.separator + file.getName() + "' cannot be found. ", var3);
      } catch (MalformedURLException var4) {
         throw new PluginException("Malformed URL to the jar file '" + this.archive.getName() + "'. ", var4);
      } catch (IOException var5) {
         throw new PluginException("The jar file '" + file.getPath() + File.separator + file.getName() + "' cannot be initialized. ", var5);
      }
   }

   private void register() throws MaltChainedException {
      try {
         Attributes atts = this.archive.getManifest().getMainAttributes();
         this.pluginName = atts.getValue("Plugin-Name");
         if (this.pluginName == null) {
            this.pluginName = this.archive.getName();
         }

      } catch (IOException var2) {
         throw new PluginException("Could not get the 'Plugin-Name' in the manifest for the plugin (jar-file). ", var2);
      }
   }

   public JarFile getArchive() {
      return this.archive;
   }

   public void setArchive(JarFile archive) {
      this.archive = archive;
   }

   public String getPluginName() {
      return this.pluginName;
   }

   public void setPluginName(String pluginName) {
      this.pluginName = pluginName;
   }

   public URL getUrl() {
      return this.url;
   }

   public void setUrl(URL url) {
      this.url = url;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.pluginName + " : " + this.url.toString());
      return sb.toString();
   }
}
