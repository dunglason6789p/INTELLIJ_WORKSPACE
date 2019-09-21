/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.config.version;

import java.io.File;
import java.util.Date;
import java.util.jar.JarEntry;
import org.maltparser.core.helper.SystemInfo;
import org.maltparser.core.helper.SystemLogger;

public class Versioning {
    private String maltParserVersion;
    private String parserModelVersion;
    private File mcoPath;
    private String configName;
    private String newConfigName;
    private String configType;
    private String featureModelXML;
    private String inputFormatXML;
    public static String[] availableVersions = new String[]{"1.0.0", "1.0.1", "1.0.2", "1.0.3", "1.1", "1.2", "1.3", "1.3.1", "1.4", "1.4.1"};
    public static boolean[] supportVersions = new boolean[]{false, false, false, false, false, false, true, true, true};

    public Versioning(String configName, String configType, File mcoPath, String parserModelVersion) {
        this.setConfigName(configName);
        this.setConfigType(configType);
        this.setMcoPath(mcoPath);
        this.setMaltParserVersion(SystemInfo.getVersion());
        this.setParserModelVersion(parserModelVersion);
        this.setNewConfigName(configName + "." + this.maltParserVersion);
    }

    public JarEntry getJarEntry(JarEntry in) {
        if (this.maltParserVersion.equals(this.parserModelVersion)) {
            return in;
        }
        String entryName = in.getName().replace(this.configName + File.separator, this.newConfigName + File.separator);
        if (entryName.endsWith(".info")) {
            return new JarEntry(entryName.replace(File.separator + this.configName + "_", File.separator + this.newConfigName + "_"));
        }
        return new JarEntry(entryName);
    }

    public boolean hasChanges(JarEntry in, JarEntry out) {
        if (this.maltParserVersion.equals(this.parserModelVersion)) {
            return false;
        }
        return in.getName().endsWith(".info") || in.getName().endsWith(".sop");
    }

    public String modifyJarEntry(JarEntry in, JarEntry out, StringBuilder sb) {
        if (this.maltParserVersion.equals(this.parserModelVersion)) {
            return sb.toString();
        }
        if (in.getName().endsWith(".info")) {
            StringBuilder outString = new StringBuilder();
            String[] lines = sb.toString().split("\\n");
            for (int i = 0; i < lines.length; ++i) {
                int index;
                if (lines[i].startsWith("Configuration name:")) {
                    outString.append("Configuration name:   ");
                    outString.append(this.configName);
                    outString.append('.');
                    outString.append(this.maltParserVersion);
                    outString.append('\n');
                    continue;
                }
                if (lines[i].startsWith("Created:")) {
                    outString.append(lines[i]);
                    outString.append('\n');
                    outString.append("Converted:            ");
                    outString.append(new Date(System.currentTimeMillis()));
                    outString.append('\n');
                    continue;
                }
                if (lines[i].startsWith("Version:")) {
                    outString.append("Version:                       ");
                    outString.append(this.maltParserVersion);
                    outString.append('\n');
                    outString.append("Created by:                    ");
                    outString.append(this.parserModelVersion);
                    outString.append('\n');
                    continue;
                }
                if (lines[i].startsWith("  name (  -c)                           ")) {
                    outString.append("  name (  -c)                           ");
                    outString.append(this.newConfigName);
                    outString.append('\n');
                    continue;
                }
                if (lines[i].startsWith("  format ( -if)                         /appdata/dataformat/")) {
                    outString.append("  format ( -if)                         ");
                    index = lines[i].lastIndexOf("/");
                    outString.append(lines[i].substring(index + 1));
                    outString.append('\n');
                    continue;
                }
                if (lines[i].startsWith("  format ( -of)                         /appdata/dataformat/")) {
                    outString.append("  format ( -of)                         ");
                    index = lines[i].lastIndexOf("/");
                    outString.append(lines[i].substring(index + 1));
                    outString.append('\n');
                    continue;
                }
                if (lines[i].startsWith("--guide-features (  -F)                 /appdata/features/")) {
                    outString.append("--guide-features (  -F)                 ");
                    index = lines[i].lastIndexOf("/");
                    outString.append(lines[i].substring(index + 1));
                    outString.append('\n');
                    continue;
                }
                outString.append(lines[i]);
                outString.append('\n');
            }
            return outString.toString();
        }
        if (in.getName().endsWith(".sop")) {
            StringBuilder outString = new StringBuilder();
            String[] lines = sb.toString().split("\\n");
            for (int i = 0; i < lines.length; ++i) {
                int slashIndex;
                int tabIndex;
                String path;
                String xmlFile;
                int index = lines[i].indexOf(9);
                int container = 0;
                if (index > -1) {
                    container = Integer.parseInt(lines[i].substring(0, index));
                }
                if (lines[i].startsWith(container + "\tguide\tfeatures")) {
                    tabIndex = lines[i].lastIndexOf(9);
                    if (lines[i].substring(tabIndex + 1).startsWith("/appdata/features/")) {
                        slashIndex = lines[i].lastIndexOf("/");
                        xmlFile = lines[i].substring(slashIndex + 1);
                        path = lines[i].substring(tabIndex + 1, slashIndex);
                        this.setFeatureModelXML(path + "/libsvm/" + xmlFile);
                        outString.append(container);
                        outString.append("\tguide\tfeatures\t");
                        outString.append(xmlFile);
                        outString.append('\n');
                        continue;
                    }
                    outString.append(lines[i]);
                    outString.append('\n');
                    continue;
                }
                if (lines[i].startsWith(container + "\tinput\tformat")) {
                    tabIndex = lines[i].lastIndexOf(9);
                    if (lines[i].substring(tabIndex + 1).startsWith("/appdata/dataformat/")) {
                        slashIndex = lines[i].lastIndexOf("/");
                        xmlFile = lines[i].substring(slashIndex + 1);
                        path = lines[i].substring(tabIndex + 1, slashIndex);
                        this.setInputFormatXML(path + "/" + xmlFile);
                        outString.append(container);
                        outString.append("\tinput\tformat\t");
                        outString.append(xmlFile);
                        outString.append('\n');
                        continue;
                    }
                    outString.append(lines[i]);
                    outString.append('\n');
                    continue;
                }
                if (this.earlierVersion("1.3")) {
                    if (lines[i].startsWith(container + "\tnivre\tpost_processing")) continue;
                    if (lines[i].startsWith(container + "\tmalt0.4\tbehavior")) {
                        if (!lines[i].endsWith("true")) continue;
                        SystemLogger.logger().info("MaltParser " + this.maltParserVersion + " doesn't support MaltParser 0.4 emulation.");
                        continue;
                    }
                    if (!lines[i].startsWith(container + "\tsinglemalt\tparsing_algorithm")) continue;
                    outString.append(container);
                    outString.append("\tsinglemalt\tparsing_algorithm\t");
                    if (lines[i].endsWith("NivreStandard")) {
                        outString.append("class org.maltparser.parser.algorithm.nivre.NivreArcStandardFactory");
                    } else if (lines[i].endsWith("NivreEager")) {
                        outString.append("class org.maltparser.parser.algorithm.nivre.NivreArcEagerFactory");
                    } else if (lines[i].endsWith("CovingtonNonProjective")) {
                        outString.append("class org.maltparser.parser.algorithm.covington.CovingtonNonProjFactory");
                    } else if (lines[i].endsWith("CovingtonProjective")) {
                        outString.append("class org.maltparser.parser.algorithm.covington.CovingtonProjFactory");
                    }
                    outString.append('\n');
                    continue;
                }
                outString.append(lines[i]);
                outString.append('\n');
            }
            return outString.toString();
        }
        return sb.toString();
    }

    public boolean earlierVersion(String version) {
        boolean e = false;
        for (int i = 0; i < availableVersions.length && !availableVersions[i].equals(version); ++i) {
            if (!availableVersions[i].equals(this.parserModelVersion)) continue;
            e = true;
        }
        return e;
    }

    public boolean support(String version) {
        for (int i = 0; i < availableVersions.length; ++i) {
            if (!availableVersions[i].equals(version)) continue;
            return supportVersions[i];
        }
        return false;
    }

    public String getFeatureModelXML() {
        return this.featureModelXML;
    }

    public void setFeatureModelXML(String featureModelXML) {
        this.featureModelXML = featureModelXML;
    }

    public String getInputFormatXML() {
        return this.inputFormatXML;
    }

    public void setInputFormatXML(String inputFormatXML) {
        this.inputFormatXML = inputFormatXML;
    }

    public String getNewConfigName() {
        return this.newConfigName;
    }

    public void setNewConfigName(String newConfigName) {
        this.newConfigName = newConfigName;
    }

    public String getConfigName() {
        return this.configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigType() {
        return this.configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public File getMcoPath() {
        return this.mcoPath;
    }

    public void setMcoPath(File mcoPath) {
        this.mcoPath = mcoPath;
    }

    public String getMaltParserVersion() {
        return this.maltParserVersion;
    }

    public void setMaltParserVersion(String maltParserVersion) {
        this.maltParserVersion = maltParserVersion;
    }

    public String getParserModelVersion() {
        return this.parserModelVersion;
    }

    public void setParserModelVersion(String parserModelVersion) {
        this.parserModelVersion = parserModelVersion;
    }
}

