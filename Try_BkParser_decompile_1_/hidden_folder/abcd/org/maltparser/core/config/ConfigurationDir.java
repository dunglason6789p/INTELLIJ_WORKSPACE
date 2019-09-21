/*
 * Decompiled with CFR 0.146.
 */
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.maltparser.core.config.ConfigurationException;
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
        if (name == null || name.length() <= 0 || type == null || type.length() <= 0) {
            throw new ConfigurationException("The configuration name is not specified. ");
        }
        this.setName(name);
        this.setType(type);
        this.setConfigDirectory(new File(this.workingDirectory.getPath() + File.separator + this.getName()));
        String mode = OptionManager.instance().getOptionValue(containerIndex, "config", "flowchart").toString().trim();
        if (mode.equals("parse")) {
            File mcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco");
            if (!mcoPath.exists()) {
                String classpath = System.getProperty("java.class.path");
                String[] items = classpath.split(System.getProperty("path.separator"));
                boolean found = false;
                for (String item : items) {
                    File candidateConfigFile;
                    File candidateDir = new File(item);
                    if (!candidateDir.exists() || !candidateDir.isDirectory() || !(candidateConfigFile = new File(candidateDir.getPath() + File.separator + this.getName() + ".mco")).exists()) continue;
                    this.initWorkingDirectory(candidateDir.getPath());
                    this.setConfigDirectory(new File(this.workingDirectory.getPath() + File.separator + this.getName()));
                    found = true;
                    break;
                }
                if (!found) {
                    throw new ConfigurationException("Couldn't find the MaltParser configuration file: " + this.getName() + ".mco");
                }
            }
            try {
                this.url = mcoPath.toURI().toURL();
            }
            catch (MalformedURLException e) {
                throw new ConfigurationException("File path could not be represented as a URL.");
            }
        }
    }

    public void initDataFormat() throws MaltChainedException {
        block13 : {
            HashSet<DataFormatSpecification.Dependency> deps;
            String mode;
            block14 : {
                String inputFormatName = OptionManager.instance().getOptionValue(this.containerIndex, "input", "format").toString().trim();
                String outputFormatName = OptionManager.instance().getOptionValue(this.containerIndex, "output", "format").toString().trim();
                URLFinder f = new URLFinder();
                if (this.configDirectory != null && this.configDirectory.exists()) {
                    URL inputFormatURL;
                    if (outputFormatName.length() == 0 || inputFormatName.equals(outputFormatName)) {
                        inputFormatURL = f.findURLinJars(inputFormatName);
                        outputFormatName = inputFormatURL != null ? (inputFormatName = this.copyToConfig(inputFormatURL)) : (inputFormatName = this.copyToConfig(inputFormatName));
                    } else {
                        inputFormatURL = f.findURLinJars(inputFormatName);
                        inputFormatName = inputFormatURL != null ? this.copyToConfig(inputFormatURL) : this.copyToConfig(inputFormatName);
                        URL outputFormatURL = f.findURLinJars(outputFormatName);
                        outputFormatName = inputFormatURL != null ? this.copyToConfig(outputFormatURL) : this.copyToConfig(outputFormatName);
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
                        InputStream is = this.outputFormatURL.openStream();
                    }
                    catch (FileNotFoundException e) {
                        this.outputFormatURL = f.findURL(outputFormatName);
                    }
                    catch (IOException e) {
                        this.outputFormatURL = f.findURL(outputFormatName);
                    }
                } else {
                    this.outputFormatURL = f.findURL(outputFormatName);
                }
                this.dataFormatManager = new DataFormatManager(this.inputFormatURL, this.outputFormatURL);
                mode = OptionManager.instance().getOptionValue(this.containerIndex, "config", "flowchart").toString().trim();
                this.symbolTables = mode.equals("parse") ? new ParseSymbolTableHandler(new HashSymbolTableHandler()) : new HashSymbolTableHandler();
                if (this.dataFormatManager.getInputDataFormatSpec().getDataStructure() != DataFormatSpecification.DataStructure.PHRASE) break block13;
                if (!mode.equals("learn")) break block14;
                deps = this.dataFormatManager.getInputDataFormatSpec().getDependencies();
                for (DataFormatSpecification.Dependency dep : deps) {
                    URL depFormatURL = f.findURLinJars(dep.getUrlString());
                    if (depFormatURL != null) {
                        this.copyToConfig(depFormatURL);
                        continue;
                    }
                    this.copyToConfig(dep.getUrlString());
                }
                break block13;
            }
            if (!mode.equals("parse")) break block13;
            deps = this.dataFormatManager.getInputDataFormatSpec().getDependencies();
            String nullValueStategy = OptionManager.instance().getOptionValue(this.containerIndex, "singlemalt", "null_value").toString();
            for (DataFormatSpecification.Dependency dep : deps) {
                DataFormatInstance dataFormatInstance = this.dataFormatManager.getDataFormatSpec(dep.getDependentOn()).createDataFormatInstance(this.symbolTables, nullValueStategy);
                this.addDataFormatInstance(this.dataFormatManager.getDataFormatSpec(dep.getDependentOn()).getDataFormatName(), dataFormatInstance);
                this.dataFormatManager.setInputDataFormatSpec(this.dataFormatManager.getDataFormatSpec(dep.getDependentOn()));
            }
        }
    }

    private URL findURL(String specModelFileName) throws MaltChainedException {
        URL url = null;
        File specFile = this.getFile(specModelFileName);
        if (specFile.exists()) {
            try {
                url = new URL("file:///" + specFile.getAbsolutePath());
            }
            catch (MalformedURLException e) {
                throw new MaltChainedException("Malformed URL: " + specFile, e);
            }
        } else {
            url = this.getConfigFileEntryURL(specModelFileName);
        }
        return url;
    }

    public OutputStreamWriter getOutputStreamWriter(String fileName, String charSet) throws MaltChainedException {
        try {
            return new OutputStreamWriter((OutputStream)new FileOutputStream(this.configDirectory.getPath() + File.separator + fileName), charSet);
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The file '" + fileName + "' cannot be created. ", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new ConfigurationException("The char set '" + charSet + "' is not supported. ", e);
        }
    }

    public OutputStreamWriter getOutputStreamWriter(String fileName) throws MaltChainedException {
        try {
            return new OutputStreamWriter((OutputStream)new FileOutputStream(this.configDirectory.getPath() + File.separator + fileName, true), "UTF-8");
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The file '" + fileName + "' cannot be created. ", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new ConfigurationException("The char set 'UTF-8' is not supported. ", e);
        }
    }

    public OutputStreamWriter getAppendOutputStreamWriter(String fileName) throws MaltChainedException {
        try {
            return new OutputStreamWriter((OutputStream)new FileOutputStream(this.configDirectory.getPath() + File.separator + fileName, true), "UTF-8");
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The file '" + fileName + "' cannot be created. ", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new ConfigurationException("The char set 'UTF-8' is not supported. ", e);
        }
    }

    public InputStreamReader getInputStreamReader(String fileName, String charSet) throws MaltChainedException {
        try {
            return new InputStreamReader((InputStream)new FileInputStream(this.configDirectory.getPath() + File.separator + fileName), charSet);
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The file '" + fileName + "' cannot be found. ", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new ConfigurationException("The char set '" + charSet + "' is not supported. ", e);
        }
    }

    public InputStreamReader getInputStreamReader(String fileName) throws MaltChainedException {
        return this.getInputStreamReader(fileName, "UTF-8");
    }

    public JarFile getConfigJarfile() throws MaltChainedException {
        JarFile mcoFile = null;
        if (this.url != null && !this.url.toString().startsWith("jar")) {
            try {
                JarURLConnection conn = (JarURLConnection)new URL("jar:" + this.url.toString() + "!/").openConnection();
                mcoFile = conn.getJarFile();
            }
            catch (IOException e) {
                throw new ConfigurationException("The mco-file '" + this.url + "' cannot be found. ", e);
            }
        }
        File mcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco");
        try {
            mcoFile = new JarFile(mcoPath.getAbsolutePath());
        }
        catch (IOException e) {
            throw new ConfigurationException("The mco-file '" + mcoPath + "' cannot be found. ", e);
        }
        if (mcoFile == null) {
            throw new ConfigurationException("The mco-file cannot be found. ");
        }
        return mcoFile;
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
            }
            return mcoFile.getInputStream(entry);
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The file entry '" + fileName + "' in the mco file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be found. ", e);
        }
        catch (IOException e) {
            throw new ConfigurationException("The file entry '" + fileName + "' in the mco file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be loaded. ", e);
        }
    }

    public InputStreamReader getInputStreamReaderFromConfigFileEntry(String fileName, String charSet) throws MaltChainedException {
        try {
            return new InputStreamReader(this.getInputStreamFromConfigFileEntry(fileName), charSet);
        }
        catch (UnsupportedEncodingException e) {
            throw new ConfigurationException("The char set '" + charSet + "' is not supported. ", e);
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
                }
                catch (IOException e) {
                    url = new URL("jar:" + this.url.toString() + "!/" + this.getName() + '\\' + fileName + "\n");
                }
                return url;
            }
            catch (MalformedURLException e) {
                throw new ConfigurationException("Couldn't find the URL 'jar:" + this.url.toString() + "!/" + this.getName() + '/' + fileName + "'", e);
            }
        }
        File mcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco");
        try {
            if (!mcoPath.exists()) {
                throw new ConfigurationException("Couldn't find mco-file '" + mcoPath.getAbsolutePath() + "'");
            }
            URL url = new URL("jar:" + new URL("file", null, mcoPath.getAbsolutePath()) + "!/" + this.getName() + '/' + fileName + "\n");
            try {
                InputStream is = url.openStream();
                is.close();
            }
            catch (IOException e) {
                url = new URL("jar:" + new URL("file", null, mcoPath.getAbsolutePath()) + "!/" + this.getName() + '\\' + fileName + "\n");
            }
            return url;
        }
        catch (MalformedURLException e) {
            throw new ConfigurationException("Couldn't find the URL 'jar:" + mcoPath.getAbsolutePath() + "!/" + this.getName() + '/' + fileName + "'", e);
        }
    }

    public String copyToConfig(File source) throws MaltChainedException {
        byte[] readBuffer = new byte[4096];
        String destination = this.configDirectory.getPath() + File.separator + source.getName();
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination), 4096);
            int n = 0;
            while ((n = bis.read(readBuffer, 0, 4096)) != -1) {
                bos.write(readBuffer, 0, n);
            }
            bos.flush();
            bos.close();
            bis.close();
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The source file '" + source + "' cannot be found or the destination file '" + destination + "' cannot be created when coping the file. ", e);
        }
        catch (IOException e) {
            throw new ConfigurationException("The source file '" + source + "' cannot be copied to destination '" + destination + "'. ", e);
        }
        return source.getName();
    }

    public String copyToConfig(String fileUrl) throws MaltChainedException {
        URLFinder f = new URLFinder();
        URL url = f.findURL(fileUrl);
        if (url == null) {
            throw new ConfigurationException("The file or URL '" + fileUrl + "' could not be found. ");
        }
        return this.copyToConfig(url);
    }

    public String copyToConfig(URL url) throws MaltChainedException {
        if (url == null) {
            throw new ConfigurationException("URL could not be found. ");
        }
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
            int n = 0;
            while ((n = bis.read(readBuffer, 0, 4096)) != -1) {
                bos.write(readBuffer, 0, n);
            }
            bos.flush();
            bos.close();
            bis.close();
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The destination file '" + destination + "' cannot be created when coping the file. ", e);
        }
        catch (IOException e) {
            throw new ConfigurationException("The URL '" + url + "' cannot be copied to destination '" + destination + "'. ", e);
        }
        return destFileName;
    }

    public void deleteConfigDirectory() throws MaltChainedException {
        if (!this.configDirectory.exists()) {
            return;
        }
        File infoFile = new File(this.configDirectory.getPath() + File.separator + this.getName() + "_" + this.getType() + ".info");
        if (!infoFile.exists()) {
            throw new ConfigurationException("There exists a directory that is not a MaltParser configuration directory. ");
        }
        this.deleteConfigDirectory(this.configDirectory);
    }

    private void deleteConfigDirectory(File directory) throws MaltChainedException {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory()) {
                    this.deleteConfigDirectory(files[i]);
                    continue;
                }
                files[i].delete();
            }
        } else {
            throw new ConfigurationException("The directory '" + directory.getPath() + "' cannot be found. ");
        }
        directory.delete();
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
        }
        if (this.configDirectory.exists()) {
            this.deleteConfigDirectory();
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
            HashSet<String> excludeGroups = new HashSet<String>();
            excludeGroups.add("system");
            this.infoFile.write("\nSETTINGS\n");
            this.infoFile.write(OptionManager.instance().toStringPrettyValues(this.containerIndex, excludeGroups));
            this.infoFile.flush();
        }
        catch (IOException e) {
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
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The maltparser configurtation file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be found. ", e);
        }
        catch (IOException e) {
            throw new ConfigurationException("The maltparser configurtation file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be created. ", e);
        }
    }

    private void createConfigFile(String directory, JarOutputStream jos) throws MaltChainedException {
        byte[] readBuffer = new byte[4096];
        try {
            File zipDir = new File(directory);
            String[] dirList = zipDir.list();
            int bytesIn = 0;
            for (int i = 0; i < dirList.length; ++i) {
                File f = new File(zipDir, dirList[i]);
                if (f.isDirectory()) {
                    String filePath = f.getPath();
                    this.createConfigFile(filePath, jos);
                    continue;
                }
                FileInputStream fis = new FileInputStream(f);
                String entryPath = f.getPath().substring(this.workingDirectory.getPath().length() + 1);
                entryPath = entryPath.replace('\\', '/');
                JarEntry entry = new JarEntry(entryPath);
                jos.putNextEntry(entry);
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    jos.write(readBuffer, 0, bytesIn);
                }
                fis.close();
            }
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("The directory '" + directory + "' cannot be found. ", e);
        }
        catch (IOException e) {
            throw new ConfigurationException("The directory '" + directory + "' cannot be compressed into a mco file. ", e);
        }
    }

    public void copyConfigFile(File in, File out, Versioning versioning) throws MaltChainedException {
        try {
            BufferedInputStream bis;
            JarFile jar = new JarFile(in);
            JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(out));
            byte[] buffer = new byte[4096];
            StringBuilder sb = new StringBuilder();
            URLFinder f = new URLFinder();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry inEntry = entries.nextElement();
                InputStream entryStream = jar.getInputStream(inEntry);
                JarEntry outEntry = versioning.getJarEntry(inEntry);
                if (!versioning.hasChanges(inEntry, outEntry)) {
                    int bytesRead;
                    tempJar.putNextEntry(outEntry);
                    while ((bytesRead = entryStream.read(buffer)) != -1) {
                        tempJar.write(buffer, 0, bytesRead);
                    }
                    continue;
                }
                tempJar.putNextEntry(outEntry);
                BufferedReader br = new BufferedReader(new InputStreamReader(entryStream));
                String line = null;
                sb.setLength(0);
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
                String outString = versioning.modifyJarEntry(inEntry, outEntry, sb);
                tempJar.write(outString.getBytes());
            }
            if (versioning.getFeatureModelXML() != null && versioning.getFeatureModelXML().startsWith("/appdata")) {
                int index = versioning.getFeatureModelXML().lastIndexOf(47);
                bis = new BufferedInputStream(f.findURLinJars(versioning.getFeatureModelXML()).openStream());
                tempJar.putNextEntry(new JarEntry(versioning.getNewConfigName() + "/" + versioning.getFeatureModelXML().substring(index + 1)));
                int n = 0;
                while ((n = bis.read(buffer, 0, 4096)) != -1) {
                    tempJar.write(buffer, 0, n);
                }
                bis.close();
            }
            if (versioning.getInputFormatXML() != null && versioning.getInputFormatXML().startsWith("/appdata")) {
                int index = versioning.getInputFormatXML().lastIndexOf(47);
                bis = new BufferedInputStream(f.findURLinJars(versioning.getInputFormatXML()).openStream());
                tempJar.putNextEntry(new JarEntry(versioning.getNewConfigName() + "/" + versioning.getInputFormatXML().substring(index + 1)));
                int n = 0;
                while ((n = bis.read(buffer, 0, 4096)) != -1) {
                    tempJar.write(buffer, 0, n);
                }
                bis.close();
            }
            tempJar.flush();
            tempJar.close();
            jar.close();
        }
        catch (IOException e) {
            throw new ConfigurationException("", e);
        }
    }

    protected void initNameNTypeFromInfoFile(URL url) throws MaltChainedException {
        if (url == null) {
            throw new ConfigurationException("The URL cannot be found. ");
        }
        try {
            JarEntry je;
            JarInputStream jis = new JarInputStream(url.openConnection().getInputStream());
            while ((je = jis.getNextJarEntry()) != null) {
                String entryName = je.getName();
                if (!entryName.endsWith(".info")) continue;
                int indexUnderScore = entryName.lastIndexOf(95);
                int indexSeparator = entryName.lastIndexOf(File.separator);
                if (indexSeparator == -1) {
                    indexSeparator = entryName.lastIndexOf(47);
                }
                if (indexSeparator == -1) {
                    indexSeparator = entryName.lastIndexOf(92);
                }
                int indexDot = entryName.lastIndexOf(46);
                if (indexUnderScore == -1 || indexDot == -1) {
                    throw new ConfigurationException("Could not find the configuration name and type from the URL '" + url.toString() + "'. ");
                }
                this.setName(entryName.substring(indexSeparator + 1, indexUnderScore));
                this.setType(entryName.substring(indexUnderScore + 1, indexDot));
                this.setConfigDirectory(new File(this.workingDirectory.getPath() + File.separator + this.getName()));
                jis.close();
                return;
            }
        }
        catch (IOException e) {
            throw new ConfigurationException("Could not find the configuration name and type from the URL '" + url.toString() + "'. ", e);
        }
    }

    public void echoInfoFile() throws MaltChainedException {
        this.checkConfigDirectory();
        try {
            JarEntry je;
            JarInputStream jis = this.url == null ? new JarInputStream(new FileInputStream(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco")) : new JarInputStream(this.url.openConnection().getInputStream());
            while ((je = jis.getNextJarEntry()) != null) {
                int c;
                String entryName = je.getName();
                if (!entryName.endsWith(this.getName() + "_" + this.getType() + ".info")) continue;
                while ((c = jis.read()) != -1) {
                    SystemLogger.logger().info(Character.valueOf((char)c));
                }
            }
            jis.close();
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("Could not print configuration information file. The configuration file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be found. ", e);
        }
        catch (IOException e) {
            throw new ConfigurationException("Could not print configuration information file. ", e);
        }
    }

    public void unpackConfigFile() throws MaltChainedException {
        this.checkConfigDirectory();
        try {
            JarInputStream jis = this.url == null ? new JarInputStream(new FileInputStream(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco")) : new JarInputStream(this.url.openConnection().getInputStream());
            this.unpackConfigFile(jis);
            jis.close();
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("Could not unpack configuration. The configuration file '" + this.workingDirectory.getPath() + File.separator + this.getName() + ".mco" + "' cannot be found. ", e);
        }
        catch (IOException e) {
            if (this.configDirectory.exists()) {
                this.deleteConfigDirectory();
            }
            throw new ConfigurationException("Could not unpack configuration. ", e);
        }
        this.initCreatedByMaltParserVersionFromInfoFile();
    }

    protected void unpackConfigFile(JarInputStream jis) throws MaltChainedException {
        try {
            JarEntry je;
            byte[] readBuffer = new byte[4096];
            TreeSet<String> directoryCache = new TreeSet<String>();
            while ((je = jis.getNextJarEntry()) != null) {
                String dirName;
                BufferedOutputStream bos;
                File directory;
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
                if (!(index <= 0 || directoryCache.contains(dirName = entryName.substring(0, index)) || (directory = new File(this.workingDirectory.getPath() + File.separator + dirName)).exists() && directory.isDirectory())) {
                    if (!directory.mkdirs()) {
                        throw new ConfigurationException("Unable to make directory '" + dirName + "'. ");
                    }
                    directoryCache.add(dirName);
                }
                if (new File(this.workingDirectory.getPath() + File.separator + entryName).isDirectory() && new File(this.workingDirectory.getPath() + File.separator + entryName).exists()) continue;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(this.workingDirectory.getPath() + File.separator + entryName), 4096);
                }
                catch (FileNotFoundException e) {
                    throw new ConfigurationException("Could not unpack configuration. The file '" + this.workingDirectory.getPath() + File.separator + entryName + "' cannot be unpacked. ", e);
                }
                int n = 0;
                while ((n = jis.read(readBuffer, 0, 4096)) != -1) {
                    bos.write(readBuffer, 0, n);
                }
                bos.flush();
                bos.close();
            }
        }
        catch (IOException e) {
            throw new ConfigurationException("Could not unpack configuration. ", e);
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
        }
        catch (NullPointerException e) {
            throw new ConfigurationException("The configuration cannot be found.", e);
        }
    }

    public void initWorkingDirectory(String pathPrefixString) throws MaltChainedException {
        this.workingDirectory = pathPrefixString == null || pathPrefixString.equalsIgnoreCase("user.dir") || pathPrefixString.equalsIgnoreCase(".") ? new File(System.getProperty("user.dir")) : new File(pathPrefixString);
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
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("Version:                       ")) continue;
                this.setCreatedByMaltParserVersion(line.substring(31));
                break;
            }
            br.close();
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException("Could not retrieve the version number of the MaltParser configuration.", e);
        }
        catch (IOException e) {
            throw new ConfigurationException("Could not retrieve the version number of the MaltParser configuration.", e);
        }
    }

    public void versioning() throws MaltChainedException {
        this.initCreatedByMaltParserVersionFromInfoFile();
        SystemLogger.logger().info("\nCurrent version      : " + SystemInfo.getVersion() + "\n");
        SystemLogger.logger().info("Parser model version : " + this.createdByMaltParserVersion + "\n");
        if (SystemInfo.getVersion() == null) {
            throw new ConfigurationException("Couln't determine the version of MaltParser");
        }
        if (this.createdByMaltParserVersion == null) {
            throw new ConfigurationException("Couln't determine the version of the parser model");
        }
        if (SystemInfo.getVersion().equals(this.createdByMaltParserVersion)) {
            SystemLogger.logger().info("The parser model " + this.getName() + ".mco has already the same version as the current version of MaltParser. \n");
            return;
        }
        File mcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + ".mco");
        File newMcoPath = new File(this.workingDirectory.getPath() + File.separator + this.getName() + "." + SystemInfo.getVersion().trim() + ".mco");
        Versioning versioning = new Versioning(this.name, this.type, mcoPath, this.createdByMaltParserVersion);
        if (!versioning.support(this.createdByMaltParserVersion)) {
            SystemLogger.logger().warn("The parser model '" + this.name + ".mco' is created by MaltParser " + this.getCreatedByMaltParserVersion() + ", which cannot be converted to a MaltParser " + SystemInfo.getVersion() + " parser model.\n");
            SystemLogger.logger().warn("Please retrain the parser model with MaltParser " + SystemInfo.getVersion() + " or download MaltParser " + this.getCreatedByMaltParserVersion() + " from http://maltparser.org/download.html\n");
            return;
        }
        SystemLogger.logger().info("Converts the parser model '" + mcoPath.getName() + "' into '" + newMcoPath.getName() + "'....\n");
        this.copyConfigFile(mcoPath, newMcoPath, versioning);
    }

    protected void checkNConvertConfigVersion() throws MaltChainedException {
        if (this.createdByMaltParserVersion.startsWith("1.0")) {
            BufferedWriter bw;
            String line;
            BufferedReader br;
            SystemLogger.logger().info("  Converts the MaltParser configuration ");
            SystemLogger.logger().info("1.0");
            SystemLogger.logger().info(" to ");
            SystemLogger.logger().info(SystemInfo.getVersion());
            SystemLogger.logger().info("\n");
            File[] configFiles = this.configDirectory.listFiles();
            int n = configFiles.length;
            for (int i = 0; i < n; ++i) {
                if (configFiles[i].getName().endsWith(".mod")) {
                    configFiles[i].renameTo(new File(this.configDirectory.getPath() + File.separator + "odm0." + configFiles[i].getName()));
                }
                if (configFiles[i].getName().endsWith(this.getName() + ".dsm")) {
                    configFiles[i].renameTo(new File(this.configDirectory.getPath() + File.separator + "odm0.dsm"));
                }
                if (configFiles[i].getName().equals("savedoptions.sop")) {
                    configFiles[i].renameTo(new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old"));
                }
                if (!configFiles[i].getName().equals("symboltables.sym")) continue;
                configFiles[i].renameTo(new File(this.configDirectory.getPath() + File.separator + "symboltables.sym.old"));
            }
            try {
                br = new BufferedReader(new FileReader(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old"));
                bw = new BufferedWriter(new FileWriter(this.configDirectory.getPath() + File.separator + "savedoptions.sop"));
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("0\tguide\tprediction_strategy")) {
                        bw.write("0\tguide\tdecision_settings\tT.TRANS+A.DEPREL\n");
                        continue;
                    }
                    bw.write(line);
                    bw.write(10);
                }
                br.close();
                bw.flush();
                bw.close();
                new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old").delete();
            }
            catch (FileNotFoundException e) {
                throw new ConfigurationException("Could convert savedoptions.sop version 1.0.4 to version 1.1. ", e);
            }
            catch (IOException e) {
                throw new ConfigurationException("Could convert savedoptions.sop version 1.0.4 to version 1.1. ", e);
            }
            try {
                br = new BufferedReader(new FileReader(this.configDirectory.getPath() + File.separator + "symboltables.sym.old"));
                bw = new BufferedWriter(new FileWriter(this.configDirectory.getPath() + File.separator + "symboltables.sym"));
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("AllCombinedClassTable")) {
                        bw.write("T.TRANS+A.DEPREL\n");
                        continue;
                    }
                    bw.write(line);
                    bw.write(10);
                }
                br.close();
                bw.flush();
                bw.close();
                new File(this.configDirectory.getPath() + File.separator + "symboltables.sym.old").delete();
            }
            catch (FileNotFoundException e) {
                throw new ConfigurationException("Could convert symboltables.sym version 1.0.4 to version 1.1. ", e);
            }
            catch (IOException e) {
                throw new ConfigurationException("Could convert symboltables.sym version 1.0.4 to version 1.1. ", e);
            }
        }
        if (!this.createdByMaltParserVersion.startsWith("1.3")) {
            SystemLogger.logger().info("  Converts the MaltParser configuration ");
            SystemLogger.logger().info(this.createdByMaltParserVersion);
            SystemLogger.logger().info(" to ");
            SystemLogger.logger().info(SystemInfo.getVersion());
            SystemLogger.logger().info("\n");
            new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop").renameTo(new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old"));
            try {
                String line;
                BufferedReader br = new BufferedReader(new FileReader(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old"));
                BufferedWriter bw = new BufferedWriter(new FileWriter(this.configDirectory.getPath() + File.separator + "savedoptions.sop"));
                while ((line = br.readLine()) != null) {
                    int index = line.indexOf(9);
                    int container = 0;
                    if (index > -1) {
                        container = Integer.parseInt(line.substring(0, index));
                    }
                    if (line.startsWith(container + "\tnivre\tpost_processing")) continue;
                    if (line.startsWith(container + "\tmalt0.4\tbehavior")) {
                        if (!line.endsWith("true")) continue;
                        SystemLogger.logger().info("MaltParser 1.3 doesn't support MaltParser 0.4 emulation.");
                        br.close();
                        bw.flush();
                        bw.close();
                        this.deleteConfigDirectory();
                        System.exit(0);
                        continue;
                    }
                    if (line.startsWith(container + "\tsinglemalt\tparsing_algorithm")) {
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
                        continue;
                    }
                    bw.write(line);
                    bw.write(10);
                }
                br.close();
                bw.flush();
                bw.close();
                new File(this.configDirectory.getPath() + File.separator + "savedoptions.sop.old").delete();
            }
            catch (FileNotFoundException e) {
                throw new ConfigurationException("Could convert savedoptions.sop version 1.0.4 to version 1.1. ", e);
            }
            catch (IOException e) {
                throw new ConfigurationException("Could convert savedoptions.sop version 1.0.4 to version 1.1. ", e);
            }
        }
    }

    public void terminate() throws MaltChainedException {
        if (this.infoFile != null) {
            try {
                this.infoFile.flush();
                this.infoFile.close();
            }
            catch (IOException e) {
                throw new ConfigurationException("Could not close configuration information file. ", e);
            }
        }
        this.symbolTables = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void finalize() throws Throwable {
        try {
            if (this.infoFile != null) {
                this.infoFile.flush();
                this.infoFile.close();
            }
        }
        finally {
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
        }
        return false;
    }

    public DataFormatInstance getDataFormatInstance(String key) {
        return this.dataFormatInstances.get(key);
    }

    public int sizeDataFormatInstance() {
        return this.dataFormatInstances.size();
    }

    public DataFormatInstance getInputDataFormatInstance() {
        return this.dataFormatInstances.get(this.dataFormatManager.getInputDataFormatSpec().getDataFormatName());
    }

    public URL getInputFormatURL() {
        return this.inputFormatURL;
    }

    public URL getOutputFormatURL() {
        return this.outputFormatURL;
    }
}

