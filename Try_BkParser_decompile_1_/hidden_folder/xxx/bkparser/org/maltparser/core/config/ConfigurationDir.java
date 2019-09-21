package org.maltparser.core.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import org.maltparser.core.config.version.Versioning;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.helper.SystemInfo;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatManager;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.options.OptionManager;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.hash.HashSymbolTableHandler;
import org.maltparser.core.symbol.parse.ParseSymbolTableHandler;

public class ConfigurationDir {
   protected static final int BUFFER = 4096;
   protected File configDirectory;
   protected String name;
   protected String type;
   protected File workingDirectory;
   protected URL url;
   protected int containerIndex;
   protected BufferedWriter infoFile = null;
   protected String createdByMaltParserVersion;
   private SymbolTableHandler symbolTables;
   private DataFormatManager dataFormatManager;
   private HashMap<String, DataFormatInstance> dataFormatInstances;
   private URL inputFormatURL;
   private URL outputFormatURL;

   public ConfigurationDir(URL url) throws MaltChainedException {
      this.initWorkingDirectory();
      this.setUrl(url);
      this.initNameNTypeFromInfoFile(url);
   }

   public ConfigurationDir(String name, String type, int containerIndex) throws MaltChainedException {
      this.setContainerIndex(containerIndex);
      this.initWorkingDirectory();
      if (name != null && name.length() > 0 && type != null && type.length() > 0) {
         this.setName(name);
         this.setType(type);
         this.setConfigDirectory(new File(this.workingDirectory.getPath() + File.separator + this.getName()));
         String var4 = OptionManager.instance().getOptionValue(containerIndex, "config", "flowchart").toString().trim();
         if (var4.equals("parse")) {
            File mcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco");
            if (!mcoPath.exists()) {
               String classpath = System.getProperty("java.class.path");
               String[] items = classpath.split(System.getProperty("path.separator"));
               boolean found = false;
               String[] arr$ = items;
               int len$ = items.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  String item = arr$[i$];
                  File candidateDir = new File(item);
                  if (candidateDir.exists() && candidateDir.isDirectory()) {
                     File candidateConfigFile = new File(candidateDir.getPath() + File.separator + this.getName() + ".mco");
                     if (candidateConfigFile.exists()) {
                        this.initWorkingDirectory(candidateDir.getPath());
                        this.setConfigDirectory(new File(this.workingDirectory.getPath() + File.separator + this.getName()));
                        found = true;
                        break;
                     }
                  }
               }

               if (!found) {
                  throw new ConfigurationException("Couldn't find the MaltParser configuration file: " + this.getName() + ".mco");
               }
            }

            try {
               this.url = mcoPath.toURI().toURL();
            } catch (MalformedURLException var15) {
               throw new ConfigurationException("File path could not be represented as a URL.");
            }
         }

      } else {
         throw new ConfigurationException("The configuration name is not specified. ");
      }
   }

   public void initDataFormat() throws MaltChainedException {
      String inputFormatName = OptionManager.instance().getOptionValue(this.containerIndex, "input", "format").toString().trim();
      String outputFormatName = OptionManager.instance().getOptionValue(this.containerIndex, "output", "format").toString().trim();
      URLFinder f = new URLFinder();
      if (this.configDirectory != null && this.configDirectory.exists()) {
         URL inputFormatURL;
         if (outputFormatName.length() != 0 && !inputFormatName.equals(outputFormatName)) {
            inputFormatURL = f.findURLinJars(inputFormatName);
            if (inputFormatURL != null) {
               inputFormatName = this.copyToConfig(inputFormatURL);
            } else {
               inputFormatName = this.copyToConfig(inputFormatName);
            }

            URL outputFormatURL = f.findURLinJars(outputFormatName);
            if (inputFormatURL != null) {
               outputFormatName = this.copyToConfig(outputFormatURL);
            } else {
               outputFormatName = this.copyToConfig(outputFormatName);
            }
         } else {
            inputFormatURL = f.findURLinJars(inputFormatName);
            if (inputFormatURL != null) {
               outputFormatName = inputFormatName = this.copyToConfig(inputFormatURL);
            } else {
               outputFormatName = inputFormatName = this.copyToConfig(inputFormatName);
            }
         }

         OptionManager.instance().overloadOptionValue(this.containerIndex, "input", "format", inputFormatName);
      } else if (outputFormatName.length() == 0) {
         outputFormatName = inputFormatName;
      }

      this.dataFormatInstances = new HashMap(3);
      this.inputFormatURL = this.findURL(inputFormatName);
      this.outputFormatURL = this.findURL(outputFormatName);
      if (this.outputFormatURL != null) {
         try {
            InputStream var12 = this.outputFormatURL.openStream();
         } catch (FileNotFoundException var10) {
            this.outputFormatURL = f.findURL(outputFormatName);
         } catch (IOException var11) {
            this.outputFormatURL = f.findURL(outputFormatName);
         }
      } else {
         this.outputFormatURL = f.findURL(outputFormatName);
      }

      this.dataFormatManager = new DataFormatManager(this.inputFormatURL, this.outputFormatURL);
      String mode = OptionManager.instance().getOptionValue(this.containerIndex, "config", "flowchart").toString().trim();
      if (mode.equals("parse")) {
         this.symbolTables = new ParseSymbolTableHandler(new HashSymbolTableHandler());
      } else {
         this.symbolTables = new HashSymbolTableHandler();
      }

      if (this.dataFormatManager.getInputDataFormatSpec().getDataStructure() == DataFormatSpecification.DataStructure.PHRASE) {
         HashSet deps;
         if (mode.equals("learn")) {
            deps = this.dataFormatManager.getInputDataFormatSpec().getDependencies();
            Iterator i$ = deps.iterator();

            while(i$.hasNext()) {
               DataFormatSpecification.Dependency dep = (DataFormatSpecification.Dependency)i$.next();
               URL depFormatURL = f.findURLinJars(dep.getUrlString());
               if (depFormatURL != null) {
                  this.copyToConfig(depFormatURL);
               } else {
                  this.copyToConfig(dep.getUrlString());
               }
            }
         } else if (mode.equals("parse")) {
            deps = this.dataFormatManager.getInputDataFormatSpec().getDependencies();
            String nullValueStategy = OptionManager.instance().getOptionValue(this.containerIndex, "singlemalt", "null_value").toString();
            Iterator i$ = deps.iterator();

            while(i$.hasNext()) {
               DataFormatSpecification.Dependency dep = (DataFormatSpecification.Dependency)i$.next();
               DataFormatInstance dataFormatInstance = this.dataFormatManager.getDataFormatSpec(dep.getDependentOn()).createDataFormatInstance(this.symbolTables, nullValueStategy);
               this.addDataFormatInstance(this.dataFormatManager.getDataFormatSpec(dep.getDependentOn()).getDataFormatName(), dataFormatInstance);
               this.dataFormatManager.setInputDataFormatSpec(this.dataFormatManager.getDataFormatSpec(dep.getDependentOn()));
            }
         }
      }

   }

   private URL findURL(String specModelFileName) throws MaltChainedException {
      URL url = null;
      File specFile = this.getFile(specModelFileName);
      if (specFile.exists()) {
         try {
            url = new URL("file:///" + specFile.getAbsolutePath());
         } catch (MalformedURLException var5) {
            throw new MaltChainedException("Malformed URL: " + specFile, var5);
         }
      } else {
         url = this.getConfigFileEntryURL(specModelFileName);
      }

      return url;
   }

   public OutputStreamWriter getOutputStreamWriter(String fileName, String charSet) throws MaltChainedException {
      try {
         return new OutputStreamWriter(new FileOutputStream(this.configDirectory.getPath() + File.separator + fileName), charSet);
      } catch (FileNotFoundException var4) {
         throw new ConfigurationException("The file '" + fileName + "' cannot be created. ", var4);
      } catch (UnsupportedEncodingException var5) {
         throw new ConfigurationException("The char set '" + charSet + "' is not supported. ", var5);
      }
   }

   public OutputStreamWriter getOutputStreamWriter(String fileName) throws MaltChainedException {
      try {
         return new OutputStreamWriter(new FileOutputStream(this.configDirectory.getPath() + File.separator + fileName, true), "UTF-8");
      } catch (FileNotFoundException var3) {
         throw new ConfigurationException("The file '" + fileName + "' cannot be created. ", var3);
      } catch (UnsupportedEncodingException var4) {
         throw new ConfigurationException("The char set 'UTF-8' is not supported. ", var4);
      }
   }

   public OutputStreamWriter getAppendOutputStreamWriter(String fileName) throws MaltChainedException {
      try {
         return new OutputStreamWriter(new FileOutputStream(this.configDirectory.getPath() + File.separator + fileName, true), "UTF-8");
      } catch (FileNotFoundException var3) {
         throw new ConfigurationException("The file '" + fileName + "' cannot be created. ", var3);
      } catch (UnsupportedEncodingException var4) {
         throw new ConfigurationException("The char set 'UTF-8' is not supported. ", var4);
      }
   }

   public InputStreamReader getInputStreamReader(String fileName, String charSet) throws MaltChainedException {
      try {
         return new InputStreamReader(new FileInputStream(this.configDirectory.getPath() + File.separator + fileName), charSet);
      } catch (FileNotFoundException var4) {
         throw new ConfigurationException("The file '" + fileName + "' cannot be found. ", var4);
      } catch (UnsupportedEncodingException var5) {
         throw new ConfigurationException("The char set '" + charSet + "' is not supported. ", var5);
      }
   }

   public InputStreamReader getInputStreamReader(String fileName) throws MaltChainedException {
      return this.getInputStreamReader(fileName, "UTF-8");
   }

   public JarFile getConfigJarfile() throws MaltChainedException {
      JarFile mcoFile = null;
      if (this.url != null && !this.url.toString().startsWith("jar")) {
         try {
            JarURLConnection conn = (JarURLConnection)(new URL("jar:" + this.url.toString() + "!/")).openConnection();
            mcoFile = conn.getJarFile();
         } catch (IOException var5) {
            throw new ConfigurationException("The mco-file '" + this.url + "' cannot be found. ", var5);
         }
      } else {
         File mcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco");

         try {
            mcoFile = new JarFile(mcoPath.getAbsolutePath());
         } catch (IOException var4) {
            throw new ConfigurationException("The mco-file '" + mcoPath + "' cannot be found. ", var4);
         }
      }

      if (mcoFile == null) {
         throw new ConfigurationException("The mco-file cannot be found. ");
      } else {
         return mcoFile;
      }
   }

   public JarEntry getConfigFileEntry(String fileName) throws MaltChainedException {
      JarFile mcoFile = this.getConfigJarfile();
      JarEntry entry = mcoFile.getJarEntry(this.getName() + '/' + fileName);
      if (entry == null) {
         entry = mcoFile.getJarEntry(this.getName() + '\\' + fileName);
      }

      return entry;
   }

   public InputStream getInputStreamFromConfigFileEntry(String fileName) throws MaltChainedException {
      JarFile mcoFile = this.getConfigJarfile();
      JarEntry entry = this.getConfigFileEntry(fileName);

      try {
         if (entry == null) {
            throw new FileNotFoundException();
         } else {
            return mcoFile.getInputStream(entry);
         }
      } catch (FileNotFoundException var5) {
         throw new ConfigurationException("The file entry '" + fileName + "' in the mco file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be found. ", var5);
      } catch (IOException var6) {
         throw new ConfigurationException("The file entry '" + fileName + "' in the mco file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be loaded. ", var6);
      }
   }

   public InputStreamReader getInputStreamReaderFromConfigFileEntry(String fileName, String charSet) throws MaltChainedException {
      try {
         return new InputStreamReader(this.getInputStreamFromConfigFileEntry(fileName), charSet);
      } catch (UnsupportedEncodingException var4) {
         throw new ConfigurationException("The char set '" + charSet + "' is not supported. ", var4);
      }
   }

   public InputStreamReader getInputStreamReaderFromConfigFile(String fileName) throws MaltChainedException {
      return this.getInputStreamReaderFromConfigFileEntry(fileName, "UTF-8");
   }

   public File getFile(String fileName) throws MaltChainedException {
      return new File(this.configDirectory.getPath() + File.separator + fileName);
   }

   public URL getConfigFileEntryURL(String fileName) throws MaltChainedException {
      if (this.url != null && !this.url.toString().startsWith("jar")) {
         try {
            URL url = new URL("jar:" + this.url.toString() + "!/" + this.getName() + '/' + fileName + "\n");

            try {
               InputStream is = url.openStream();
               is.close();
            } catch (IOException var5) {
               url = new URL("jar:" + this.url.toString() + "!/" + this.getName() + '\\' + fileName + "\n");
            }

            return url;
         } catch (MalformedURLException var6) {
            throw new ConfigurationException("Couldn't find the URL 'jar:" + this.url.toString() + "!/" + this.getName() + '/' + fileName + "'", var6);
         }
      } else {
         File mcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco");

         try {
            if (!mcoPath.exists()) {
               throw new ConfigurationException("Couldn't find mco-file '" + mcoPath.getAbsolutePath() + "'");
            } else {
               URL url = new URL("jar:" + new URL("file", (String)null, mcoPath.getAbsolutePath()) + "!/" + this.getName() + '/' + fileName + "\n");

               try {
                  InputStream is = url.openStream();
                  is.close();
               } catch (IOException var7) {
                  url = new URL("jar:" + new URL("file", (String)null, mcoPath.getAbsolutePath()) + "!/" + this.getName() + '\\' + fileName + "\n");
               }

               return url;
            }
         } catch (MalformedURLException var8) {
            throw new ConfigurationException("Couldn't find the URL 'jar:" + mcoPath.getAbsolutePath() + "!/" + this.getName() + '/' + fileName + "'", var8);
         }
      }
   }

   public String copyToConfig(File source) throws MaltChainedException {
      byte[] readBuffer = new byte[4096];
      String destination = this.configDirectory.getPath() + File.separator + source.getName();

      try {
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
         BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination), 4096);
         boolean var6 = false;

         int n;
         while((n = bis.read(readBuffer, 0, 4096)) != -1) {
            bos.write(readBuffer, 0, n);
         }

         bos.flush();
         bos.close();
         bis.close();
         return source.getName();
      } catch (FileNotFoundException var7) {
         throw new ConfigurationException("The source file '" + source + "' cannot be found or the destination file '" + destination + "' cannot be created when coping the file. ", var7);
      } catch (IOException var8) {
         throw new ConfigurationException("The source file '" + source + "' cannot be copied to destination '" + destination + "'. ", var8);
      }
   }

   public String copyToConfig(String fileUrl) throws MaltChainedException {
      URLFinder f = new URLFinder();
      URL url = f.findURL(fileUrl);
      if (url == null) {
         throw new ConfigurationException("The file or URL '" + fileUrl + "' could not be found. ");
      } else {
         return this.copyToConfig(url);
      }
   }

   public String copyToConfig(URL url) throws MaltChainedException {
      if (url == null) {
         throw new ConfigurationException("URL could not be found. ");
      } else {
         byte[] readBuffer = new byte[4096];
         String destFileName = url.getPath();
         int indexSlash = destFileName.lastIndexOf(47);
         if (indexSlash == -1) {
            indexSlash = destFileName.lastIndexOf(92);
         }

         if (indexSlash != -1) {
            destFileName = destFileName.substring(indexSlash + 1);
         }

         String destination = this.configDirectory.getPath() + File.separator + destFileName;

         try {
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination), 4096);
            boolean var8 = false;

            int n;
            while((n = bis.read(readBuffer, 0, 4096)) != -1) {
               bos.write(readBuffer, 0, n);
            }

            bos.flush();
            bos.close();
            bis.close();
            return destFileName;
         } catch (FileNotFoundException var9) {
            throw new ConfigurationException("The destination file '" + destination + "' cannot be created when coping the file. ", var9);
         } catch (IOException var10) {
            throw new ConfigurationException("The URL '" + url + "' cannot be copied to destination '" + destination + "'. ", var10);
         }
      }
   }

   public void deleteConfigDirectory() throws MaltChainedException {
      if (this.configDirectory.exists()) {
         File infoFile = new File(this.configDirectory.getPath() + File.separator + this.getName() + "_" + this.getType() + ".info");
         if (infoFile.exists()) {
            this.deleteConfigDirectory(this.configDirectory);
         } else {
            throw new ConfigurationException("There exists a directory that is not a MaltParser configuration directory. ");
         }
      }
   }

   private void deleteConfigDirectory(File directory) throws MaltChainedException {
      if (directory.exists()) {
         File[] files = directory.listFiles();

         for(int i = 0; i < files.length; ++i) {
            if (files[i].isDirectory()) {
               this.deleteConfigDirectory(files[i]);
            } else {
               files[i].delete();
            }
         }

         directory.delete();
      } else {
         throw new ConfigurationException("The directory '" + directory.getPath() + "' cannot be found. ");
      }
   }

   public File getConfigDirectory() {
      return this.configDirectory;
   }

   protected void setConfigDirectory(File dir) {
      this.configDirectory = dir;
   }

   public void createConfigDirectory() throws MaltChainedException {
      this.checkConfigDirectory();
      this.configDirectory.mkdir();
      this.createInfoFile();
   }

   protected void checkConfigDirectory() throws MaltChainedException {
      if (this.configDirectory.exists() && !this.configDirectory.isDirectory()) {
         throw new ConfigurationException("The configuration directory name already exists and is not a directory. ");
      } else {
         if (this.configDirectory.exists()) {
            this.deleteConfigDirectory();
         }

      }
   }

   protected void createInfoFile() throws MaltChainedException {
      this.infoFile = new BufferedWriter(this.getOutputStreamWriter(this.getName() + "_" + this.getType() + ".info"));

      try {
         this.infoFile.write("CONFIGURATION\n");
         this.infoFile.write("Configuration name:   " + this.getName() + "\n");
         this.infoFile.write("Configuration type:   " + this.getType() + "\n");
         this.infoFile.write("Created:              " + new Date(System.currentTimeMillis()) + "\n");
         this.infoFile.write("\nSYSTEM\n");
         this.infoFile.write("Operating system architecture: " + System.getProperty("os.arch") + "\n");
         this.infoFile.write("Operating system name:         " + System.getProperty("os.name") + "\n");
         this.infoFile.write("JRE vendor name:               " + System.getProperty("java.vendor") + "\n");
         this.infoFile.write("JRE version number:            " + System.getProperty("java.version") + "\n");
         this.infoFile.write("\nMALTPARSER\n");
         this.infoFile.write("Version:                       " + SystemInfo.getVersion() + "\n");
         this.infoFile.write("Build date:                    " + SystemInfo.getBuildDate() + "\n");
         Set<String> excludeGroups = new HashSet();
         excludeGroups.add("system");
         this.infoFile.write("\nSETTINGS\n");
         this.infoFile.write(OptionManager.instance().toStringPrettyValues(this.containerIndex, excludeGroups));
         this.infoFile.flush();
      } catch (IOException var2) {
         throw new ConfigurationException("Could not create the maltparser info file. ");
      }
   }

   public BufferedWriter getInfoFileWriter() throws MaltChainedException {
      return this.infoFile;
   }

   public void createConfigFile() throws MaltChainedException {
      try {
         JarOutputStream jos = new JarOutputStream(new FileOutputStream(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco"));
         this.createConfigFile(this.configDirectory.getPath(), jos);
         jos.close();
      } catch (FileNotFoundException var2) {
         throw new ConfigurationException("The maltparser configurtation file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be found. ", var2);
      } catch (IOException var3) {
         throw new ConfigurationException("The maltparser configurtation file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be created. ", var3);
      }
   }

   private void createConfigFile(String directory, JarOutputStream jos) throws MaltChainedException {
      byte[] readBuffer = new byte[4096];

      try {
         File zipDir = new File(directory);
         String[] dirList = zipDir.list();
         int bytesIn = false;

         for(int i = 0; i < dirList.length; ++i) {
            File f = new File(zipDir, dirList[i]);
            if (f.isDirectory()) {
               String filePath = f.getPath();
               this.createConfigFile(filePath, jos);
            } else {
               FileInputStream fis = new FileInputStream(f);
               String entryPath = f.getPath().substring(this.workingDirectory.getPath().length() + 1);
               entryPath = entryPath.replace('\\', '/');
               JarEntry entry = new JarEntry(entryPath);
               jos.putNextEntry(entry);

               int bytesIn;
               while((bytesIn = fis.read(readBuffer)) != -1) {
                  jos.write(readBuffer, 0, bytesIn);
               }

               fis.close();
            }
         }

      } catch (FileNotFoundException var12) {
         throw new ConfigurationException("The directory '" + directory + "' cannot be found. ", var12);
      } catch (IOException var13) {
         throw new ConfigurationException("The directory '" + directory + "' cannot be compressed into a mco file. ", var13);
      }
   }

   public void copyConfigFile(File in, File out, Versioning versioning) throws MaltChainedException {
      try {
         JarFile jar = new JarFile(in);
         JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(out));
         byte[] buffer = new byte[4096];
         StringBuilder sb = new StringBuilder();
         URLFinder f = new URLFinder();
         Enumeration entries = jar.entries();

         while(true) {
            while(entries.hasMoreElements()) {
               JarEntry inEntry = (JarEntry)entries.nextElement();
               InputStream entryStream = jar.getInputStream(inEntry);
               JarEntry outEntry = versioning.getJarEntry(inEntry);
               if (!versioning.hasChanges(inEntry, outEntry)) {
                  tempJar.putNextEntry(outEntry);

                  int bytesRead;
                  while((bytesRead = entryStream.read(buffer)) != -1) {
                     tempJar.write(buffer, 0, bytesRead);
                  }
               } else {
                  tempJar.putNextEntry(outEntry);
                  BufferedReader br = new BufferedReader(new InputStreamReader(entryStream));
                  String line = null;
                  sb.setLength(0);

                  while((line = br.readLine()) != null) {
                     sb.append(line);
                     sb.append('\n');
                  }

                  String outString = versioning.modifyJarEntry(inEntry, outEntry, sb);
                  tempJar.write(outString.getBytes());
               }
            }

            int index;
            BufferedInputStream bis;
            boolean var20;
            int n;
            if (versioning.getFeatureModelXML() != null && versioning.getFeatureModelXML().startsWith("/appdata")) {
               index = versioning.getFeatureModelXML().lastIndexOf(47);
               bis = new BufferedInputStream(f.findURLinJars(versioning.getFeatureModelXML()).openStream());
               tempJar.putNextEntry(new JarEntry(versioning.getNewConfigName() + "/" + versioning.getFeatureModelXML().substring(index + 1)));
               var20 = false;

               while((n = bis.read(buffer, 0, 4096)) != -1) {
                  tempJar.write(buffer, 0, n);
               }

               bis.close();
            }

            if (versioning.getInputFormatXML() != null && versioning.getInputFormatXML().startsWith("/appdata")) {
               index = versioning.getInputFormatXML().lastIndexOf(47);
               bis = new BufferedInputStream(f.findURLinJars(versioning.getInputFormatXML()).openStream());
               tempJar.putNextEntry(new JarEntry(versioning.getNewConfigName() + "/" + versioning.getInputFormatXML().substring(index + 1)));
               var20 = false;

               while((n = bis.read(buffer, 0, 4096)) != -1) {
                  tempJar.write(buffer, 0, n);
               }

               bis.close();
            }

            tempJar.flush();
            tempJar.close();
            jar.close();
            return;
         }
      } catch (IOException var17) {
         throw new ConfigurationException("", var17);
      }
   }

   protected void initNameNTypeFromInfoFile(URL url) throws MaltChainedException {
      if (url == null) {
         throw new ConfigurationException("The URL cannot be found. ");
      } else {
         try {
            JarInputStream jis = new JarInputStream(url.openConnection().getInputStream());

            String entryName;
            do {
               JarEntry je;
               if ((je = jis.getNextJarEntry()) == null) {
                  return;
               }

               entryName = je.getName();
            } while(!entryName.endsWith(".info"));

            int indexUnderScore = entryName.lastIndexOf(95);
            int indexSeparator = entryName.lastIndexOf(File.separator);
            if (indexSeparator == -1) {
               indexSeparator = entryName.lastIndexOf(47);
            }

            if (indexSeparator == -1) {
               indexSeparator = entryName.lastIndexOf(92);
            }

            int indexDot = entryName.lastIndexOf(46);
            if (indexUnderScore != -1 && indexDot != -1) {
               this.setName(entryName.substring(indexSeparator + 1, indexUnderScore));
               this.setType(entryName.substring(indexUnderScore + 1, indexDot));
               this.setConfigDirectory(new File(this.workingDirectory.getPath() + File.separator + this.getName()));
               jis.close();
            } else {
               throw new ConfigurationException("Could not find the configuration name and type from the URL '" + url.toString() + "'. ");
            }
         } catch (IOException var8) {
            throw new ConfigurationException("Could not find the configuration name and type from the URL '" + url.toString() + "'. ", var8);
         }
      }
   }

   public void echoInfoFile() throws MaltChainedException {
      this.checkConfigDirectory();

      try {
         JarInputStream jis;
         if (this.url == null) {
            jis = new JarInputStream(new FileInputStream(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco"));
         } else {
            jis = new JarInputStream(this.url.openConnection().getInputStream());
         }

         while(true) {
            String entryName;
            do {
               JarEntry je;
               if ((je = jis.getNextJarEntry()) == null) {
                  jis.close();
                  return;
               }

               entryName = je.getName();
            } while(!entryName.endsWith(this.getName() + "_" + this.getType() + ".info"));

            int c;
            while((c = jis.read()) != -1) {
               SystemLogger.logger().info((char)c);
            }
         }
      } catch (FileNotFoundException var5) {
         throw new ConfigurationException("Could not print configuration information file. The configuration file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be found. ", var5);
      } catch (IOException var6) {
         throw new ConfigurationException("Could not print configuration information file. ", var6);
      }
   }

   public void unpackConfigFile() throws MaltChainedException {
      this.checkConfigDirectory();

      try {
         JarInputStream jis;
         if (this.url == null) {
            jis = new JarInputStream(new FileInputStream(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco"));
         } else {
            jis = new JarInputStream(this.url.openConnection().getInputStream());
         }

         this.unpackConfigFile(jis);
         jis.close();
      } catch (FileNotFoundException var3) {
         throw new ConfigurationException("Could not unpack configuration. The configuration file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be found. ", var3);
      } catch (IOException var4) {
         if (this.configDirectory.exists()) {
            this.deleteConfigDirectory();
         }

         throw new ConfigurationException("Could not unpack configuration. ", var4);
      }

      this.initCreatedByMaltParserVersionFromInfoFile();
   }

   protected void unpackConfigFile(JarInputStream jis) throws MaltChainedException {
      try {
         byte[] readBuffer = new byte[4096];
         TreeSet directoryCache = new TreeSet();

         JarEntry je;
         while((je = jis.getNextJarEntry()) != null) {
            String entryName = je.getName();
            if (entryName.startsWith("/")) {
               entryName = entryName.substring(1);
            }

            if (entryName.endsWith(File.separator) || entryName.endsWith("/")) {
               return;
            }

            int index = -1;
            if (File.separator.equals("\\")) {
               entryName = entryName.replace('/', '\\');
               index = entryName.lastIndexOf("\\");
            } else if (File.separator.equals("/")) {
               entryName = entryName.replace('\\', '/');
               index = entryName.lastIndexOf("/");
            }

            if (index > 0) {
               String dirName = entryName.substring(0, index);
               if (!directoryCache.contains(dirName)) {
                  File directory = new File(this.workingDirectory.getPath() + File.separator + dirName);
                  if (!directory.exists() || !directory.isDirectory()) {
                     if (!directory.mkdirs()) {
                        throw new ConfigurationException("Unable to make directory '" + dirName + "'. ");
                     }

                     directoryCache.add(dirName);
                  }
               }
            }

            if (!(new File(this.workingDirectory.getPath() + File.separator + entryName)).isDirectory() || !(new File(this.workingDirectory.getPath() + File.separator + entryName)).exists()) {
               BufferedOutputStream bos;
               try {
                  bos = new BufferedOutputStream(new FileOutputStream(this.workingDirectory.getPath() + File.separator + entryName), 4096);
               } catch (FileNotFoundException var9) {
                  throw new ConfigurationException("Could not unpack configuration. The file '" + this.workingDirectory.getPath() + File.separator + entryName + "' cannot be unpacked. ", var9);
               }

               boolean var12 = false;

               int n;
               while((n = jis.read(readBuffer, 0, 4096)) != -1) {
                  bos.write(readBuffer, 0, n);
               }

               bos.flush();
               bos.close();
            }
         }

      } catch (IOException var10) {
         throw new ConfigurationException("Could not unpack configuration. ", var10);
      }
   }

   public String getName() {
      return this.name;
   }

   protected void setName(String name) {
      this.name = name;
   }

   public String getType() {
      return this.type;
   }

   protected void setType(String type) {
      this.type = type;
   }

   public File getWorkingDirectory() {
      return this.workingDirectory;
   }

   public void initWorkingDirectory() throws MaltChainedException {
      try {
         this.initWorkingDirectory(OptionManager.instance().getOptionValue(this.containerIndex, "config", "workingdir").toString());
      } catch (NullPointerException var2) {
         throw new ConfigurationException("The configuration cannot be found.", var2);
      }
   }

   public void initWorkingDirectory(String pathPrefixString) throws MaltChainedException {
      if (pathPrefixString != null && !pathPrefixString.equalsIgnoreCase("user.dir") && !pathPrefixString.equalsIgnoreCase(".")) {
         this.workingDirectory = new File(pathPrefixString);
      } else {
         this.workingDirectory = new File(System.getProperty("user.dir"));
      }

      if (this.workingDirectory == null || !this.workingDirectory.isDirectory()) {
         new ConfigurationException("The specified working directory '" + pathPrefixString + "' is not a directory. ");
      }

   }

   public URL getUrl() {
      return this.url;
   }

   protected void setUrl(URL url) {
      this.url = url;
   }

   public int getContainerIndex() {
      return this.containerIndex;
   }

   public void setContainerIndex(int containerIndex) {
      this.containerIndex = containerIndex;
   }

   public String getCreatedByMaltParserVersion() {
      return this.createdByMaltParserVersion;
   }

   public void setCreatedByMaltParserVersion(String createdByMaltParserVersion) {
      this.createdByMaltParserVersion = createdByMaltParserVersion;
   }

   public void initCreatedByMaltParserVersionFromInfoFile() throws MaltChainedException {
      try {
         BufferedReader br = new BufferedReader(this.getInputStreamReaderFromConfigFileEntry(this.getName() + "_" + this.getType() + ".info", "UTF-8"));
         String line = null;

         while((line = br.readLine()) != null) {
            if (line.startsWith("Version:                       ")) {
               this.setCreatedByMaltParserVersion(line.substring(31));
               break;
            }
         }

         br.close();
      } catch (FileNotFoundException var3) {
         throw new ConfigurationException("Could not retrieve the version number of the MaltParser configuration.", var3);
      } catch (IOException var4) {
         throw new ConfigurationException("Could not retrieve the version number of the MaltParser configuration.", var4);
      }
   }

   public void versioning() throws MaltChainedException {
      this.initCreatedByMaltParserVersionFromInfoFile();
      SystemLogger.logger().info("\nCurrent version      : " + SystemInfo.getVersion() + "\n");
      SystemLogger.logger().info("Parser model version : " + this.createdByMaltParserVersion + "\n");
      if (SystemInfo.getVersion() == null) {
         throw new ConfigurationException("Couln't determine the version of MaltParser");
      } else if (this.createdByMaltParserVersion == null) {
         throw new ConfigurationException("Couln't determine the version of the parser model");
      } else if (SystemInfo.getVersion().equals(this.createdByMaltParserVersion)) {
         SystemLogger.logger().info("The parser model " + this.getName() + ".mco has already the same version as the current version of MaltParser. \n");
      } else {
         File mcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco");
         File newMcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + "." + SystemInfo.getVersion().trim() + ".mco");
         Versioning versioning = new Versioning(this.name, this.type, mcoPath, this.createdByMaltParserVersion);
         if (!versioning.support(this.createdByMaltParserVersion)) {
            SystemLogger.logger().warn("The parser model '" + this.name + ".mco' is created by MaltParser " + this.getCreatedByMaltParserVersion() + ", which cannot be converted to a MaltParser " + SystemInfo.getVersion() + " parser model.\n");
            SystemLogger.logger().warn("Please retrain the parser model with MaltParser " + SystemInfo.getVersion() + " or download MaltParser " + this.getCreatedByMaltParserVersion() + " from http://maltparser.org/download.html\n");
         } else {
            SystemLogger.logger().info("Converts the parser model '" + mcoPath.getName() + "' into '" + newMcoPath.getName() + "'....\n");
            this.copyConfigFile(mcoPath, newMcoPath, versioning);
         }
      }
   }

   protected void checkNConvertConfigVersion() throws MaltChainedException {
      if (this.createdByMaltParserVersion.startsWith("1.0")) {
         SystemLogger.logger().info("  Converts the MaltParser configuration ");
         SystemLogger.logger().info("1.0");
         SystemLogger.logger().info(" to ");
         SystemLogger.logger().info(SystemInfo.getVersion());
         SystemLogger.logger().info("\n");
         File[] configFiles = this.configDirectory.listFiles();
         int i = 0;

         for(int n = configFiles.length; i < n; ++i) {
            if (configFiles[i].getName().endsWith(".mod")) {
               configFiles[i].renameTo(new File(this.configDirectory.getPath() + File.separator + "odm0." + configFiles[i].getName()));
            }

            if (configFiles[i].getName().endsWith(this.getName() + ".dsm")) {
               configFiles[i].renameTo(new File(this.configDirectory.getPath() + File.separator + "odm0.dsm"));
            }

            if (configFiles[i].getName().equals("savedoptions.sop")) {
               configFiles[i].renameTo(new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old"));
            }

            if (configFiles[i].getName().equals("symboltables.sym")) {
               configFiles[i].renameTo(new File(this.configDirectory.getPath() + File.separator + "symboltables.sym.old"));
            }
         }

         String line;
         BufferedReader br;
         BufferedWriter bw;
         try {
            br = new BufferedReader(new FileReader(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old"));
            bw = new BufferedWriter(new FileWriter(this.configDirectory.getPath() + File.separator + "savedoptions.sop"));

            while(true) {
               if ((line = br.readLine()) == null) {
                  br.close();
                  bw.flush();
                  bw.close();
                  (new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old")).delete();
                  break;
               }

               if (line.startsWith("0\tguide\tprediction_strategy")) {
                  bw.write("0\tguide\tdecision_settings\tT.TRANS+A.DEPREL\n");
               } else {
                  bw.write(line);
                  bw.write(10);
               }
            }
         } catch (FileNotFoundException var10) {
            throw new ConfigurationException("Could convert savedoptions.sop version 1.0.4 to version 1.1. ", var10);
         } catch (IOException var11) {
            throw new ConfigurationException("Could convert savedoptions.sop version 1.0.4 to version 1.1. ", var11);
         }

         try {
            br = new BufferedReader(new FileReader(this.configDirectory.getPath() + File.separator + "symboltables.sym.old"));
            bw = new BufferedWriter(new FileWriter(this.configDirectory.getPath() + File.separator + "symboltables.sym"));

            while((line = br.readLine()) != null) {
               if (line.startsWith("AllCombinedClassTable")) {
                  bw.write("T.TRANS+A.DEPREL\n");
               } else {
                  bw.write(line);
                  bw.write(10);
               }
            }

            br.close();
            bw.flush();
            bw.close();
            (new File(this.configDirectory.getPath() + File.separator + "symboltables.sym.old")).delete();
         } catch (FileNotFoundException var8) {
            throw new ConfigurationException("Could convert symboltables.sym version 1.0.4 to version 1.1. ", var8);
         } catch (IOException var9) {
            throw new ConfigurationException("Could convert symboltables.sym version 1.0.4 to version 1.1. ", var9);
         }
      }

      if (!this.createdByMaltParserVersion.startsWith("1.3")) {
         SystemLogger.logger().info("  Converts the MaltParser configuration ");
         SystemLogger.logger().info(this.createdByMaltParserVersion);
         SystemLogger.logger().info(" to ");
         SystemLogger.logger().info(SystemInfo.getVersion());
         SystemLogger.logger().info("\n");
         (new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop")).renameTo(new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old"));

         try {
            BufferedReader br = new BufferedReader(new FileReader(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old"));
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.configDirectory.getPath() + File.separator + "savedoptions.sop"));

            String line;
            while((line = br.readLine()) != null) {
               int index = line.indexOf(9);
               int container = 0;
               if (index > -1) {
                  container = Integer.parseInt(line.substring(0, index));
               }

               if (!line.startsWith(container + "\tnivre\tpost_processing")) {
                  if (line.startsWith(container + "\tmalt0.4\tbehavior")) {
                     if (line.endsWith("true")) {
                        SystemLogger.logger().info("MaltParser 1.3 doesn't support MaltParser 0.4 emulation.");
                        br.close();
                        bw.flush();
                        bw.close();
                        this.deleteConfigDirectory();
                        System.exit(0);
                     }
                  } else if (line.startsWith(container + "\tsinglemalt\tparsing_algorithm")) {
                     bw.write(container);
                     bw.write("\tsinglemalt\tparsing_algorithm\t");
                     if (line.endsWith("NivreStandard")) {
                        bw.write("class org.maltparser.parser.algorithm.nivre.NivreArcStandardFactory");
                     } else if (line.endsWith("NivreEager")) {
                        bw.write("class org.maltparser.parser.algorithm.nivre.NivreArcEagerFactory");
                     } else if (line.endsWith("CovingtonNonProjective")) {
                        bw.write("class org.maltparser.parser.algorithm.covington.CovingtonNonProjFactory");
                     } else if (line.endsWith("CovingtonProjective")) {
                        bw.write("class org.maltparser.parser.algorithm.covington.CovingtonProjFactory");
                     }

                     bw.write(10);
                  } else {
                     bw.write(line);
                     bw.write(10);
                  }
               }
            }

            br.close();
            bw.flush();
            bw.close();
            (new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old")).delete();
         } catch (FileNotFoundException var6) {
            throw new ConfigurationException("Could convert savedoptions.sop version 1.0.4 to version 1.1. ", var6);
         } catch (IOException var7) {
            throw new ConfigurationException("Could convert savedoptions.sop version 1.0.4 to version 1.1. ", var7);
         }
      }

   }

   public void terminate() throws MaltChainedException {
      if (this.infoFile != null) {
         try {
            this.infoFile.flush();
            this.infoFile.close();
         } catch (IOException var2) {
            throw new ConfigurationException("Could not close configuration information file. ", var2);
         }
      }

      this.symbolTables = null;
   }

   protected void finalize() throws Throwable {
      try {
         if (this.infoFile != null) {
            this.infoFile.flush();
            this.infoFile.close();
         }
      } finally {
         super.finalize();
      }

   }

   public SymbolTableHandler getSymbolTables() {
      return this.symbolTables;
   }

   public void setSymbolTables(SymbolTableHandler symbolTables) {
      this.symbolTables = symbolTables;
   }

   public DataFormatManager getDataFormatManager() {
      return this.dataFormatManager;
   }

   public void setDataFormatManager(DataFormatManager dataFormatManager) {
      this.dataFormatManager = dataFormatManager;
   }

   public Set<String> getDataFormatInstanceKeys() {
      return this.dataFormatInstances.keySet();
   }

   public boolean addDataFormatInstance(String key, DataFormatInstance dataFormatInstance) {
      if (!this.dataFormatInstances.containsKey(key)) {
         this.dataFormatInstances.put(key, dataFormatInstance);
         return true;
      } else {
         return false;
      }
   }

   public DataFormatInstance getDataFormatInstance(String key) {
      return (DataFormatInstance)this.dataFormatInstances.get(key);
   }

   public int sizeDataFormatInstance() {
      return this.dataFormatInstances.size();
   }

   public DataFormatInstance getInputDataFormatInstance() {
      return (DataFormatInstance)this.dataFormatInstances.get(this.dataFormatManager.getInputDataFormatSpec().getDataFormatName());
   }

   public URL getInputFormatURL() {
      return this.inputFormatURL;
   }

   public URL getOutputFormatURL() {
      return this.outputFormatURL;
   }
}
