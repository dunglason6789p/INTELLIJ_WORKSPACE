/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.system;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.FeatureRegistry;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.system.FunctionDescription;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.helper.URLFinder;
import org.maltparser.core.plugin.Plugin;
import org.maltparser.core.plugin.PluginLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FeatureEngine
extends HashMap<String, FunctionDescription> {
    public static final long serialVersionUID = 3256444702936019250L;

    public Function newFunction(String functionName, FeatureRegistry registry) throws MaltChainedException {
        FunctionDescription funcDesc;
        int i = 0;
        Function func = null;
        while ((funcDesc = (FunctionDescription)this.get(functionName + "~~" + i)) != null && (func = funcDesc.newFunction(registry)) == null) {
            ++i;
        }
        return func;
    }

    public void load(String urlstring) throws MaltChainedException {
        URLFinder f = new URLFinder();
        this.load(f.findURL(urlstring));
    }

    public void load(PluginLoader plugins) throws MaltChainedException {
        for (Plugin plugin : plugins) {
            URL url = null;
            try {
                url = new URL("jar:" + plugin.getUrl() + "!/appdata/plugin.xml");
            }
            catch (MalformedURLException e) {
                throw new FeatureException("Malformed URL: 'jar:" + plugin.getUrl() + "!plugin.xml'", e);
            }
            try {
                InputStream is = url.openStream();
                is.close();
            }
            catch (IOException e) {
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
            }
            this.readFeatureSystem(root);
        }
        catch (IOException e) {
            throw new FeatureException("The feature system file '" + specModelURL.getFile() + "' cannot be found. ", e);
        }
        catch (ParserConfigurationException e) {
            throw new FeatureException("Problem parsing the file " + specModelURL.getFile() + ". ", e);
        }
        catch (SAXException e) {
            throw new FeatureException("Problem parsing the file " + specModelURL.getFile() + ". ", e);
        }
    }

    public void readFeatureSystem(Element system) throws MaltChainedException {
        NodeList functions = system.getElementsByTagName("function");
        for (int i = 0; i < functions.getLength(); ++i) {
            this.readFunction((Element)functions.item(i));
        }
    }

    public void readFunction(Element function) throws MaltChainedException {
        boolean hasSubFunctions = function.getAttribute("hasSubFunctions").equalsIgnoreCase("true");
        boolean hasFactory = false;
        if (function.getAttribute("hasFactory").length() > 0) {
            hasFactory = function.getAttribute("hasFactory").equalsIgnoreCase("true");
        }
        Class<?> clazz = null;
        try {
            if (PluginLoader.instance() != null) {
                clazz = PluginLoader.instance().getClass(function.getAttribute("class"));
            }
            if (clazz == null) {
                clazz = Class.forName(function.getAttribute("class"));
            }
        }
        catch (ClassNotFoundException e) {
            throw new FeatureException("The feature system could not find the function class" + function.getAttribute("class") + ".", e);
        }
        if (hasSubFunctions) {
            NodeList subfunctions = function.getElementsByTagName("subfunction");
            for (int i = 0; i < subfunctions.getLength(); ++i) {
                this.readSubFunction((Element)subfunctions.item(i), clazz, hasFactory);
            }
        } else {
            int i = 0;
            String n = null;
            while (this.containsKey(n = function.getAttribute("name") + "~~" + i)) {
                ++i;
            }
            this.put(n, new FunctionDescription(function.getAttribute("name"), clazz, false, hasFactory));
        }
    }

    public void readSubFunction(Element subfunction, Class<?> clazz, boolean hasFactory) throws MaltChainedException {
        int i = 0;
        String n = null;
        while (this.containsKey(n = subfunction.getAttribute("name") + "~~" + i)) {
            ++i;
        }
        this.put(n, new FunctionDescription(subfunction.getAttribute("name"), clazz, true, hasFactory));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        if (this.size() != ((FeatureEngine)obj).size()) {
            return false;
        }
        for (String name : this.keySet()) {
            if (((FunctionDescription)this.get(name)).equals(((FeatureEngine)obj).get(name))) continue;
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : this.keySet()) {
            sb.append(name);
            sb.append('\t');
            sb.append(this.get(name));
            sb.append('\n');
        }
        return sb.toString();
    }
}

