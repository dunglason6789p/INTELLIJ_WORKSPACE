package org.maltparser.core.io.dataformat;

import java.net.URL;
import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.helper.URLFinder;

public class DataFormatManager {
   private DataFormatSpecification inputDataFormatSpec;
   private DataFormatSpecification outputDataFormatSpec;
   private final HashMap<String, DataFormatSpecification> fileNameDataFormatSpecs = new HashMap();
   private final HashMap<String, DataFormatSpecification> nameDataFormatSpecs = new HashMap();

   public DataFormatManager(URL inputFormatUrl, URL outputFormatUrl) throws MaltChainedException {
      this.inputDataFormatSpec = this.loadDataFormat(inputFormatUrl);
      this.outputDataFormatSpec = this.loadDataFormat(outputFormatUrl);
   }

   public DataFormatSpecification loadDataFormat(URL dataFormatUrl) throws MaltChainedException {
      if (dataFormatUrl == null) {
         return null;
      } else {
         DataFormatSpecification dataFormat = (DataFormatSpecification)this.fileNameDataFormatSpecs.get(dataFormatUrl.toString());
         if (dataFormat == null) {
            dataFormat = new DataFormatSpecification();
            dataFormat.parseDataFormatXMLfile(dataFormatUrl);
            this.fileNameDataFormatSpecs.put(dataFormatUrl.toString(), dataFormat);
            this.nameDataFormatSpecs.put(dataFormat.getDataFormatName(), dataFormat);
            URLFinder f = new URLFinder();
            Iterator i$ = dataFormat.getDependencies().iterator();

            while(i$.hasNext()) {
               DataFormatSpecification.Dependency dep = (DataFormatSpecification.Dependency)i$.next();
               this.loadDataFormat(f.findURLinJars(dep.getUrlString()));
            }
         }

         return dataFormat;
      }
   }

   public DataFormatSpecification getInputDataFormatSpec() {
      return this.inputDataFormatSpec;
   }

   public DataFormatSpecification getOutputDataFormatSpec() {
      return this.outputDataFormatSpec;
   }

   public void setInputDataFormatSpec(DataFormatSpecification inputDataFormatSpec) {
      this.inputDataFormatSpec = inputDataFormatSpec;
   }

   public void setOutputDataFormatSpec(DataFormatSpecification outputDataFormatSpec) {
      this.outputDataFormatSpec = outputDataFormatSpec;
   }

   public DataFormatSpecification getDataFormatSpec(String name) {
      return (DataFormatSpecification)this.nameDataFormatSpecs.get(name);
   }
}
