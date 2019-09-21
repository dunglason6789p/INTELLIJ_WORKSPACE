/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.maltparser.concurrent.test.ExperimentException;

public class Experiment {
    private final String modelName;
    private final URL modelURL;
    private final URL dataFormatURL;
    private final String charSet;
    private final List<URL> inURLs;
    private final List<File> outFiles;

    public Experiment(String _modelName, URL _modelURL, URL _dataFormatURL, String _charSet, List<URL> _inURLs, List<File> _outFiles) throws ExperimentException {
        this.modelName = _modelName;
        this.modelURL = _modelURL;
        this.dataFormatURL = _dataFormatURL;
        this.charSet = _charSet == null || _charSet.length() == 0 ? "UTF-8" : _charSet;
        if (_inURLs.size() != _outFiles.size()) {
            throw new ExperimentException("The lists of in-files and out-files must match in size.");
        }
        this.inURLs = Collections.synchronizedList(new ArrayList<URL>(_inURLs));
        this.outFiles = Collections.synchronizedList(new ArrayList<File>(_outFiles));
    }

    public Experiment(String _modelName, String _modelFileName, String _dataFormatFileName, String _charSet, List<String> _inFileNames, List<String> _outFileNames) throws ExperimentException {
        int i;
        this.modelName = _modelName;
        try {
            this.modelURL = new File(_modelFileName).toURI().toURL();
        }
        catch (MalformedURLException e) {
            throw new ExperimentException("The model file name is malformed", e);
        }
        try {
            this.dataFormatURL = new File(_dataFormatFileName).toURI().toURL();
        }
        catch (MalformedURLException e) {
            throw new ExperimentException("The data format file name is malformed", e);
        }
        this.charSet = _charSet == null || _charSet.length() == 0 ? "UTF-8" : _charSet;
        if (_inFileNames.size() != _outFileNames.size()) {
            throw new ExperimentException("The lists of in-files and out-files must match in size.");
        }
        this.inURLs = Collections.synchronizedList(new ArrayList());
        for (i = 0; i < _inFileNames.size(); ++i) {
            try {
                this.inURLs.add(new File(_inFileNames.get(i)).toURI().toURL());
                continue;
            }
            catch (MalformedURLException e) {
                throw new ExperimentException("The in file name is malformed", e);
            }
        }
        this.outFiles = Collections.synchronizedList(new ArrayList());
        for (i = 0; i < _outFileNames.size(); ++i) {
            this.outFiles.add(new File(_outFileNames.get(i)));
        }
    }

    public String getModelName() {
        return this.modelName;
    }

    public URL getModelURL() {
        return this.modelURL;
    }

    public URL getDataFormatURL() {
        return this.dataFormatURL;
    }

    public String getCharSet() {
        return this.charSet;
    }

    public List<URL> getInURLs() {
        return Collections.synchronizedList(new ArrayList<URL>(this.inURLs));
    }

    public List<File> getOutFiles() {
        return Collections.synchronizedList(new ArrayList<File>(this.outFiles));
    }

    public int nInURLs() {
        return this.inURLs.size();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.charSet == null ? 0 : this.charSet.hashCode());
        result = 31 * result + (this.dataFormatURL == null ? 0 : this.dataFormatURL.hashCode());
        result = 31 * result + (this.inURLs == null ? 0 : this.inURLs.hashCode());
        result = 31 * result + (this.modelName == null ? 0 : this.modelName.hashCode());
        result = 31 * result + (this.modelURL == null ? 0 : this.modelURL.hashCode());
        result = 31 * result + (this.outFiles == null ? 0 : this.outFiles.hashCode());
        return result;
    }

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
        Experiment other = (Experiment)obj;
        if (this.charSet == null ? other.charSet != null : !this.charSet.equals(other.charSet)) {
            return false;
        }
        if (this.dataFormatURL == null ? other.dataFormatURL != null : !this.dataFormatURL.equals(other.dataFormatURL)) {
            return false;
        }
        if (this.inURLs == null ? other.inURLs != null : !this.inURLs.equals(other.inURLs)) {
            return false;
        }
        if (this.modelName == null ? other.modelName != null : !this.modelName.equals(other.modelName)) {
            return false;
        }
        if (this.modelURL == null ? other.modelURL != null : !this.modelURL.equals(other.modelURL)) {
            return false;
        }
        return !(this.outFiles == null ? other.outFiles != null : !this.outFiles.equals(other.outFiles));
    }

    public String toString() {
        int i;
        StringBuilder sb = new StringBuilder();
        sb.append("#STARTEXP");
        sb.append('\n');
        sb.append("MODELNAME:");
        sb.append(this.modelName);
        sb.append('\n');
        sb.append("MODELURL:");
        sb.append(this.modelURL);
        sb.append('\n');
        sb.append("DATAFORMATURL:");
        sb.append(this.dataFormatURL);
        sb.append('\n');
        sb.append("CHARSET:");
        sb.append(this.charSet);
        sb.append('\n');
        sb.append("INURLS");
        sb.append('\n');
        for (i = 0; i < this.inURLs.size(); ++i) {
            sb.append(this.inURLs.get(i).toExternalForm());
            sb.append('\n');
        }
        sb.append("OUTFILES");
        sb.append('\n');
        for (i = 0; i < this.outFiles.size(); ++i) {
            sb.append(this.outFiles.get(i));
            sb.append('\n');
        }
        sb.append("#ENDEXP");
        sb.append('\n');
        return sb.toString();
    }

    public static List<Experiment> loadExperiments(String experimentsFileName) throws MalformedURLException, IOException, ExperimentException {
        return Experiment.loadExperiments(new File(experimentsFileName).toURI().toURL());
    }

    public static List<Experiment> loadExperiments(URL experimentsURL) throws IOException, ExperimentException {
        String line;
        List<Experiment> experiments = Collections.synchronizedList(new ArrayList());
        BufferedReader reader = new BufferedReader(new InputStreamReader(experimentsURL.openStream(), "UTF-8"));
        boolean read_expdesc = false;
        int read_inouturls = 0;
        String modelName = null;
        URL modelURL = null;
        URL dataFormatURL = null;
        String charSet = null;
        ArrayList<URL> inURLs = new ArrayList<URL>();
        ArrayList<File> outFiles = new ArrayList<File>();
        while ((line = reader.readLine()) != null) {
            if (line.trim().equals("#STARTEXP")) {
                read_expdesc = true;
                continue;
            }
            if (line.trim().toUpperCase().startsWith("MODELNAME") && read_expdesc) {
                modelName = line.trim().substring(line.trim().indexOf(58) + 1);
                continue;
            }
            if (line.trim().toUpperCase().startsWith("MODELURL") && read_expdesc) {
                modelURL = new URL(line.trim().substring(line.trim().indexOf(58) + 1));
                continue;
            }
            if (line.trim().toUpperCase().startsWith("MODELFILE") && read_expdesc) {
                modelURL = new File(line.trim().substring(line.trim().indexOf(58) + 1)).toURI().toURL();
                continue;
            }
            if (line.trim().toUpperCase().startsWith("DATAFORMATURL") && read_expdesc) {
                dataFormatURL = new URL(line.trim().substring(line.trim().indexOf(58) + 1));
                continue;
            }
            if (line.trim().toUpperCase().startsWith("DATAFORMATFILE") && read_expdesc) {
                dataFormatURL = new File(line.trim().substring(line.trim().indexOf(58) + 1)).toURI().toURL();
                continue;
            }
            if (line.trim().toUpperCase().startsWith("CHARSET") && read_expdesc) {
                charSet = line.trim().substring(line.trim().indexOf(58) + 1);
                continue;
            }
            if (line.trim().toUpperCase().startsWith("INURLS") && read_expdesc) {
                read_inouturls = 1;
                continue;
            }
            if (line.trim().toUpperCase().startsWith("INFILES") && read_expdesc) {
                read_inouturls = 2;
                continue;
            }
            if (line.trim().toUpperCase().startsWith("OUTFILES") && read_expdesc) {
                read_inouturls = 3;
                continue;
            }
            if (read_expdesc && !line.trim().equals("#ENDEXP")) {
                if (read_inouturls == 1) {
                    inURLs.add(new URL(line.trim()));
                    continue;
                }
                if (read_inouturls == 2) {
                    inURLs.add(new File(line.trim()).toURI().toURL());
                    continue;
                }
                if (read_inouturls != 3) continue;
                outFiles.add(new File(line.trim()));
                continue;
            }
            if (!line.trim().equals("#ENDEXP") || !read_expdesc) continue;
            if (inURLs.size() > 0 && outFiles.size() > 0) {
                experiments.add(new Experiment(modelName, modelURL, dataFormatURL, charSet, inURLs, outFiles));
            }
            charSet = null;
            modelName = null;
            dataFormatURL = null;
            modelURL = null;
            inURLs.clear();
            outFiles.clear();
            read_expdesc = false;
            read_inouturls = 0;
        }
        return experiments;
    }
}

