/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.maltparser.core.options.OptionDescriptions;
import org.maltparser.core.options.OptionManager;

public class SystemInfo {
    private static SystemInfo uniqueInstance = new SystemInfo();
    private static String version;
    private static String buildDate;

    private SystemInfo() {
        URL url = this.getClass().getResource("/appdata/release.properties");
        if (url != null) {
            Properties properties = new Properties();
            try {
                properties.load(url.openStream());
            }
            catch (IOException e) {
                // empty catch block
            }
            version = properties.getProperty("version", "undef");
            buildDate = properties.getProperty("builddate", "undef");
        } else {
            version = "undef";
            buildDate = "undef";
        }
    }

    public static SystemInfo instance() {
        return uniqueInstance;
    }

    public static String header() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------------------------------------------\n                          MaltParser " + version + "                             \n" + "-----------------------------------------------------------------------------\n" + "         MALT (Models and Algorithms for Language Technology) Group          \n" + "             Vaxjo University and Uppsala University                         \n" + "                             Sweden                                          \n" + "-----------------------------------------------------------------------------\n");
        return sb.toString();
    }

    public static String shortHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nUsage: \n   java -jar maltparser-" + version + ".jar -f <path to option file> <options>\n" + "   java -jar maltparser-" + version + ".jar -h for more help and options\n\n" + OptionManager.instance().getOptionDescriptions().toStringOptionGroup("system") + "Documentation: docs/index.html\n");
        return sb.toString();
    }

    public static String getVersion() {
        return version;
    }

    public static String getBuildDate() {
        return buildDate;
    }
}

