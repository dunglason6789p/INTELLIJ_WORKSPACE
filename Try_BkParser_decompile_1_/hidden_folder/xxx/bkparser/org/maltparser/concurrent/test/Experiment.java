package org.maltparser.concurrent.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
      if (_charSet != null && _charSet.length() != 0) {
         this.charSet = _charSet;
      } else {
         this.charSet = "UTF-8";
      }

      if (_inURLs.size() != _outFiles.size()) {
         throw new ExperimentException("The lists of in-files and out-files must match in size.");
      } else {
         this.inURLs = Collections.synchronizedList(new ArrayList(_inURLs));
         this.outFiles = Collections.synchronizedList(new ArrayList(_outFiles));
      }
   }

   public Experiment(String _modelName, String _modelFileName, String _dataFormatFileName, String _charSet, List<String> _inFileNames, List<String> _outFileNames) throws ExperimentException {
      this.modelName = _modelName;

      try {
         this.modelURL = (new File(_modelFileName)).toURI().toURL();
      } catch (MalformedURLException var11) {
         throw new ExperimentException("The model file name is malformed", var11);
      }

      try {
         this.dataFormatURL = (new File(_dataFormatFileName)).toURI().toURL();
      } catch (MalformedURLException var10) {
         throw new ExperimentException("The data format file name is malformed", var10);
      }

      if (_charSet != null && _charSet.length() != 0) {
         this.charSet = _charSet;
      } else {
         this.charSet = "UTF-8";
      }

      if (_inFileNames.size() != _outFileNames.size()) {
         throw new ExperimentException("The lists of in-files and out-files must match in size.");
      } else {
         this.inURLs = Collections.synchronizedList(new ArrayList());

         int i;
         for(i = 0; i < _inFileNames.size(); ++i) {
            try {
               this.inURLs.add((new File((String)_inFileNames.get(i))).toURI().toURL());
            } catch (MalformedURLException var9) {
               throw new ExperimentException("The in file name is malformed", var9);
            }
         }

         this.outFiles = Collections.synchronizedList(new ArrayList());

         for(i = 0; i < _outFileNames.size(); ++i) {
            this.outFiles.add(new File((String)_outFileNames.get(i)));
         }

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
      return Collections.synchronizedList(new ArrayList(this.inURLs));
   }

   public List<File> getOutFiles() {
      return Collections.synchronizedList(new ArrayList(this.outFiles));
   }

   public int nInURLs() {
      return this.inURLs.size();
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.charSet == null ? 0 : this.charSet.hashCode());
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
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         Experiment other = (Experiment)obj;
         if (this.charSet == null) {
            if (other.charSet != null) {
               return false;
            }
         } else if (!this.charSet.equals(other.charSet)) {
            return false;
         }

         if (this.dataFormatURL == null) {
            if (other.dataFormatURL != null) {
               return false;
            }
         } else if (!this.dataFormatURL.equals(other.dataFormatURL)) {
            return false;
         }

         if (this.inURLs == null) {
            if (other.inURLs != null) {
               return false;
            }
         } else if (!this.inURLs.equals(other.inURLs)) {
            return false;
         }

         if (this.modelName == null) {
            if (other.modelName != null) {
               return false;
            }
         } else if (!this.modelName.equals(other.modelName)) {
            return false;
         }

         if (this.modelURL == null) {
            if (other.modelURL != null) {
               return false;
            }
         } else if (!this.modelURL.equals(other.modelURL)) {
            return false;
         }

         if (this.outFiles == null) {
            if (other.outFiles != null) {
               return false;
            }
         } else if (!this.outFiles.equals(other.outFiles)) {
            return false;
         }

         return true;
      }
   }

   public String toString() {
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

      int i;
      for(i = 0; i < this.inURLs.size(); ++i) {
         sb.append(((URL)this.inURLs.get(i)).toExternalForm());
         sb.append('\n');
      }

      sb.append("OUTFILES");
      sb.append('\n');

      for(i = 0; i < this.outFiles.size(); ++i) {
         sb.append(this.outFiles.get(i));
         sb.append('\n');
      }

      sb.append("#ENDEXP");
      sb.append('\n');
      return sb.toString();
   }

   public static List<Experiment> loadExperiments(String experimentsFileName) throws MalformedURLException, IOException, ExperimentException {
      return loadExperiments((new File(experimentsFileName)).toURI().toURL());
   }

   public static List<Experiment> loadExperiments(URL experimentsURL) throws IOException, ExperimentException {
      List<Experiment> experiments = Collections.synchronizedList(new ArrayList());
      BufferedReader reader = new BufferedReader(new InputStreamReader(experimentsURL.openStream(), "UTF-8"));
      boolean read_expdesc = false;
      int read_inouturls = 0;
      String modelName = null;
      URL modelURL = null;
      URL dataFormatURL = null;
      String charSet = null;
      List<URL> inURLs = new ArrayList();
      ArrayList outFiles = new ArrayList();

      while(true) {
         String line;
         while((line = reader.readLine()) != null) {
            if (line.trim().equals("#STARTEXP")) {
               read_expdesc = true;
            } else if (line.trim().toUpperCase().startsWith("MODELNAME") && read_expdesc) {
               modelName = line.trim().substring(line.trim().indexOf(58) + 1);
            } else if (line.trim().toUpperCase().startsWith("MODELURL") && read_expdesc) {
               modelURL = new URL(line.trim().substring(line.trim().indexOf(58) + 1));
            } else if (line.trim().toUpperCase().startsWith("MODELFILE") && read_expdesc) {
               modelURL = (new File(line.trim().substring(line.trim().indexOf(58) + 1))).toURI().toURL();
            } else if (line.trim().toUpperCase().startsWith("DATAFORMATURL") && read_expdesc) {
               dataFormatURL = new URL(line.trim().substring(line.trim().indexOf(58) + 1));
            } else if (line.trim().toUpperCase().startsWith("DATAFORMATFILE") && read_expdesc) {
               dataFormatURL = (new File(line.trim().substring(line.trim().indexOf(58) + 1))).toURI().toURL();
            } else if (line.trim().toUpperCase().startsWith("CHARSET") && read_expdesc) {
               charSet = line.trim().substring(line.trim().indexOf(58) + 1);
            } else if (line.trim().toUpperCase().startsWith("INURLS") && read_expdesc) {
               read_inouturls = 1;
            } else if (line.trim().toUpperCase().startsWith("INFILES") && read_expdesc) {
               read_inouturls = 2;
            } else if (line.trim().toUpperCase().startsWith("OUTFILES") && read_expdesc) {
               read_inouturls = 3;
            } else if (read_expdesc && !line.trim().equals("#ENDEXP")) {
               if (read_inouturls == 1) {
                  inURLs.add(new URL(line.trim()));
               } else if (read_inouturls == 2) {
                  inURLs.add((new File(line.trim())).toURI().toURL());
               } else if (read_inouturls == 3) {
                  outFiles.add(new File(line.trim()));
               }
            } else if (line.trim().equals("#ENDEXP") && read_expdesc) {
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
         }

         return experiments;
      }
   }
}
