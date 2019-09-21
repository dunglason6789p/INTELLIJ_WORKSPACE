package org.maltparser.core.feature.spec.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.spec.SpecificationModels;

public class ParReader implements FeatureSpecReader {
   private EnumMap<ParReader.ColumnNames, String> columnNameMap;
   private EnumMap<ParReader.DataStructures, String> dataStructuresMap;
   private boolean useSplitFeats = true;
   private boolean covington = false;
   private boolean pppath;
   private boolean pplifted;
   private boolean ppcoveredRoot;

   public ParReader() throws MaltChainedException {
      this.initializeColumnNameMap();
      this.initializeDataStructuresMap();
      this.setPppath(false);
      this.setPplifted(false);
      this.setPpcoveredRoot(false);
   }

   public void load(URL specModelURL, SpecificationModels featureSpecModels) throws MaltChainedException {
      BufferedReader br = null;
      Pattern tabPattern = Pattern.compile("\t");
      if (specModelURL == null) {
         throw new FeatureException("The feature specification file cannot be found. ");
      } else {
         try {
            br = new BufferedReader(new InputStreamReader(specModelURL.openStream()));
         } catch (IOException var25) {
            throw new FeatureException("Could not read the feature specification file '" + specModelURL.toString() + "'. ", var25);
         }

         if (br != null) {
            int specModelIndex = featureSpecModels.getNextIndex();
            StringBuilder featureText = new StringBuilder();
            String splitfeats = "";
            ArrayList<String> fileLines = new ArrayList();
            ArrayList orderFileLines = new ArrayList();

            while(true) {
               String fileLine;
               do {
                  try {
                     fileLine = br.readLine();
                  } catch (IOException var24) {
                     throw new FeatureException("Could not read the feature specification file '" + specModelURL.toString() + "'. ", var24);
                  }

                  if (fileLine == null) {
                     try {
                        br.close();
                     } catch (IOException var23) {
                        throw new FeatureException("Could not close the feature specification file '" + specModelURL.toString() + "'. ", var23);
                     }

                     for(int j = 0; j < fileLines.size(); ++j) {
                        orderFileLines.add(fileLines.get(j));
                     }

                     boolean deprel = false;

                     for(int j = 0; j < orderFileLines.size(); ++j) {
                        deprel = false;
                        featureText.setLength(0);
                        splitfeats = "";
                        String[] items = tabPattern.split((CharSequence)orderFileLines.get(j));
                        if (items.length < 2) {
                           throw new FeatureException("The feature specification file '" + specModelURL.toString() + "' must contain at least two columns.");
                        }

                        if (!this.columnNameMap.containsKey(ParReader.ColumnNames.valueOf(items[0].trim())) && !this.columnNameMap.containsValue(items[0].trim())) {
                           throw new FeatureException("Column one in the feature specification file '" + specModelURL.toString() + "' contains an unknown value '" + items[0].trim() + "'. ");
                        }

                        if (!items[0].trim().equalsIgnoreCase("DEP") && !items[0].trim().equalsIgnoreCase("DEPREL")) {
                           if (this.columnNameMap.containsKey(ParReader.ColumnNames.valueOf(items[0].trim()))) {
                              featureText.append("InputColumn(" + (String)this.columnNameMap.get(ParReader.ColumnNames.valueOf(items[0].trim())) + ", ");
                           } else if (this.columnNameMap.containsValue(items[0].trim())) {
                              featureText.append("InputColumn(" + items[0].trim() + ", ");
                           }

                           if (items[0].trim().equalsIgnoreCase("FEATS") && this.isUseSplitFeats()) {
                              splitfeats = "Split(";
                           }
                        } else {
                           featureText.append("OutputColumn(DEPREL, ");
                           deprel = true;
                        }

                        if (!items[1].trim().equalsIgnoreCase("STACK") && !items[1].trim().equalsIgnoreCase("INPUT") && !items[1].trim().equalsIgnoreCase("CONTEXT")) {
                           throw new FeatureException("Column two in the feature specification file '" + specModelURL.toString() + "' should be either 'STACK', 'INPUT' or 'CONTEXT' (Covington), not '" + items[1].trim() + "'. ");
                        }

                        int offset = 0;
                        if (items.length >= 3) {
                           try {
                              offset = new Integer(Integer.parseInt(items[2]));
                           } catch (NumberFormatException var22) {
                              throw new FeatureException("The feature specification file '" + specModelURL.toString() + "' contains a illegal integer value. ", var22);
                           }
                        }

                        String functionArg = "";
                        if (items[1].trim().equalsIgnoreCase("CONTEXT")) {
                           if (offset >= 0) {
                              functionArg = (String)this.dataStructuresMap.get(ParReader.DataStructures.valueOf("LEFTCONTEXT")) + "[" + offset + "]";
                           } else {
                              functionArg = (String)this.dataStructuresMap.get(ParReader.DataStructures.valueOf("RIGHTCONTEXT")) + "[" + Math.abs(offset + 1) + "]";
                           }
                        } else if (this.dataStructuresMap.containsKey(ParReader.DataStructures.valueOf(items[1].trim()))) {
                           if (this.covington) {
                              if (((String)this.dataStructuresMap.get(ParReader.DataStructures.valueOf(items[1].trim()))).equalsIgnoreCase("Stack")) {
                                 functionArg = "Left[" + offset + "]";
                              } else {
                                 functionArg = "Right[" + offset + "]";
                              }
                           } else {
                              functionArg = (String)this.dataStructuresMap.get(ParReader.DataStructures.valueOf(items[1].trim())) + "[" + offset + "]";
                           }
                        } else {
                           if (!this.dataStructuresMap.containsValue(items[1].trim())) {
                              throw new FeatureException("Column two in the feature specification file '" + specModelURL.toString() + "' should not contain the value '" + items[1].trim());
                           }

                           if (this.covington) {
                              if (items[1].trim().equalsIgnoreCase("Stack")) {
                                 functionArg = "Left[" + offset + "]";
                              } else {
                                 functionArg = "Right[" + offset + "]";
                              }
                           } else {
                              functionArg = items[1].trim() + "[" + offset + "]";
                           }
                        }

                        int linearOffset = 0;
                        int headOffset = 0;
                        int depOffset = 0;
                        int sibOffset = 0;
                        int suffixLength = 0;
                        if (items.length >= 4) {
                           linearOffset = new Integer(Integer.parseInt(items[3]));
                        }

                        if (items.length >= 5) {
                           headOffset = new Integer(Integer.parseInt(items[4]));
                        }

                        if (items.length >= 6) {
                           depOffset = new Integer(Integer.parseInt(items[5]));
                        }

                        if (items.length >= 7) {
                           sibOffset = new Integer(Integer.parseInt(items[6]));
                        }

                        if (items.length >= 8) {
                           suffixLength = new Integer(Integer.parseInt(items[7]));
                        }

                        int i;
                        if (linearOffset < 0) {
                           linearOffset = Math.abs(linearOffset);

                           for(i = 0; i < linearOffset; ++i) {
                              functionArg = "pred(" + functionArg + ")";
                           }
                        } else if (linearOffset > 0) {
                           for(i = 0; i < linearOffset; ++i) {
                              functionArg = "succ(" + functionArg + ")";
                           }
                        }

                        if (headOffset < 0) {
                           throw new FeatureException("The feature specification file '" + specModelURL.toString() + "' should not contain a negative head function value. ");
                        }

                        for(i = 0; i < headOffset; ++i) {
                           functionArg = "head(" + functionArg + ")";
                        }

                        if (depOffset < 0) {
                           depOffset = Math.abs(depOffset);

                           for(i = 0; i < depOffset; ++i) {
                              functionArg = "ldep(" + functionArg + ")";
                           }
                        } else if (depOffset > 0) {
                           for(i = 0; i < depOffset; ++i) {
                              functionArg = "rdep(" + functionArg + ")";
                           }
                        }

                        if (sibOffset < 0) {
                           sibOffset = Math.abs(sibOffset);

                           for(i = 0; i < sibOffset; ++i) {
                              functionArg = "lsib(" + functionArg + ")";
                           }
                        } else if (sibOffset > 0) {
                           for(i = 0; i < sibOffset; ++i) {
                              functionArg = "rsib(" + functionArg + ")";
                           }
                        }

                        if (!deprel || !this.pppath && !this.pplifted && !this.ppcoveredRoot) {
                           if (suffixLength != 0) {
                              featureSpecModels.add(specModelIndex, "Suffix(" + featureText.toString() + functionArg + ")," + suffixLength + ")");
                           } else if (splitfeats.equals("Split(")) {
                              featureSpecModels.add(specModelIndex, splitfeats + featureText.toString() + functionArg + "),\\|)");
                           } else {
                              featureSpecModels.add(specModelIndex, featureText.toString() + functionArg + ")");
                           }
                        } else {
                           featureSpecModels.add(specModelIndex, this.mergePseudoProjColumns(functionArg));
                        }
                     }

                     return;
                  }
               } while(fileLine.length() <= 1 && fileLine.trim().substring(0, 2).trim().equals("--"));

               fileLines.add(fileLine);
            }
         }
      }
   }

   private String mergePseudoProjColumns(String functionArg) {
      StringBuilder newFeatureText = new StringBuilder();
      int c = 1;
      if (this.pplifted) {
         ++c;
      }

      if (this.pppath) {
         ++c;
      }

      if (this.ppcoveredRoot) {
         ++c;
      }

      if (c == 1) {
         newFeatureText.append("OutputColumn(DEPREL, ");
         newFeatureText.append(functionArg);
         newFeatureText.append(')');
         return newFeatureText.toString();
      } else {
         if (c == 2) {
            newFeatureText.append("Merge(");
            newFeatureText.append("OutputColumn(DEPREL, ");
            newFeatureText.append(functionArg);
            newFeatureText.append("), ");
            if (this.pplifted) {
               newFeatureText.append("OutputTable(PPLIFTED, ");
               newFeatureText.append(functionArg);
               newFeatureText.append(")");
            }

            if (this.pppath) {
               newFeatureText.append("OutputTable(PPPATH, ");
               newFeatureText.append(functionArg);
               newFeatureText.append(")");
            }

            if (this.ppcoveredRoot) {
               newFeatureText.append("OutputTable(PPCOVERED, ");
               newFeatureText.append(functionArg);
               newFeatureText.append(")");
            }

            newFeatureText.append(")");
         } else if (c == 3) {
            int i = 0;
            newFeatureText.append("Merge3(");
            newFeatureText.append("OutputColumn(DEPREL, ");
            newFeatureText.append(functionArg);
            newFeatureText.append("), ");
            int i = i + 1;
            if (this.pplifted) {
               newFeatureText.append("OutputTable(PPLIFTED, ");
               newFeatureText.append(functionArg);
               ++i;
               if (i < 3) {
                  newFeatureText.append("), ");
               } else {
                  newFeatureText.append(")");
               }
            }

            if (this.pppath) {
               newFeatureText.append("OutputTable(PPPATH, ");
               newFeatureText.append(functionArg);
               ++i;
               if (i < 3) {
                  newFeatureText.append("), ");
               } else {
                  newFeatureText.append(")");
               }
            }

            if (this.ppcoveredRoot) {
               newFeatureText.append("OutputTable(PPCOVERED, ");
               newFeatureText.append(functionArg);
               ++i;
               if (i < 3) {
                  newFeatureText.append("), ");
               } else {
                  newFeatureText.append(")");
               }
            }

            newFeatureText.append(")");
         } else {
            newFeatureText.append("Merge(Merge(");
            newFeatureText.append("OutputColumn(DEPREL, ");
            newFeatureText.append(functionArg);
            newFeatureText.append("), ");
            newFeatureText.append("OutputTable(PPLIFTED, ");
            newFeatureText.append(functionArg);
            newFeatureText.append(")), Merge(");
            newFeatureText.append("OutputTable(PPPATH, ");
            newFeatureText.append(functionArg);
            newFeatureText.append("), ");
            newFeatureText.append("OutputTable(PPCOVERED, ");
            newFeatureText.append(functionArg);
            newFeatureText.append(")))");
         }

         return newFeatureText.toString();
      }
   }

   public EnumMap<ParReader.ColumnNames, String> getColumnNameMap() {
      return this.columnNameMap;
   }

   public void initializeColumnNameMap() {
      this.columnNameMap = new EnumMap(ParReader.ColumnNames.class);
      this.columnNameMap.put(ParReader.ColumnNames.POS, "POSTAG");
      this.columnNameMap.put(ParReader.ColumnNames.CPOS, "CPOSTAG");
      this.columnNameMap.put(ParReader.ColumnNames.DEP, "DEPREL");
      this.columnNameMap.put(ParReader.ColumnNames.LEX, "FORM");
      this.columnNameMap.put(ParReader.ColumnNames.LEMMA, "LEMMA");
      this.columnNameMap.put(ParReader.ColumnNames.FEATS, "FEATS");
   }

   public void setColumnNameMap(EnumMap<ParReader.ColumnNames, String> columnNameMap) {
      this.columnNameMap = columnNameMap;
   }

   public EnumMap<ParReader.DataStructures, String> getDataStructuresMap() {
      return this.dataStructuresMap;
   }

   public void initializeDataStructuresMap() {
      this.dataStructuresMap = new EnumMap(ParReader.DataStructures.class);
      this.dataStructuresMap.put(ParReader.DataStructures.STACK, "Stack");
      this.dataStructuresMap.put(ParReader.DataStructures.INPUT, "Input");
   }

   public void setDataStructuresMap(EnumMap<ParReader.DataStructures, String> dataStructuresMap) {
      this.dataStructuresMap = dataStructuresMap;
   }

   public boolean isUseSplitFeats() {
      return this.useSplitFeats;
   }

   public void setUseSplitFeats(boolean useSplitFeats) {
      this.useSplitFeats = useSplitFeats;
   }

   public boolean isCovington() {
      return this.covington;
   }

   public void setCovington(boolean covington) {
      this.covington = covington;
   }

   public boolean isPppath() {
      return this.pppath;
   }

   public void setPppath(boolean pppath) {
      this.pppath = pppath;
   }

   public boolean isPplifted() {
      return this.pplifted;
   }

   public void setPplifted(boolean pplifted) {
      this.pplifted = pplifted;
   }

   public boolean isPpcoveredRoot() {
      return this.ppcoveredRoot;
   }

   public void setPpcoveredRoot(boolean ppcoveredRoot) {
      this.ppcoveredRoot = ppcoveredRoot;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Mapping of column names:\n");
      ParReader.ColumnNames[] arr$ = ParReader.ColumnNames.values();
      int len$ = arr$.length;

      int i$;
      for(i$ = 0; i$ < len$; ++i$) {
         ParReader.ColumnNames columnName = arr$[i$];
         sb.append(columnName.toString() + "\t" + (String)this.columnNameMap.get(columnName) + "\n");
      }

      sb.append("Mapping of data structures:\n");
      ParReader.DataStructures[] arr$ = ParReader.DataStructures.values();
      len$ = arr$.length;

      for(i$ = 0; i$ < len$; ++i$) {
         ParReader.DataStructures dataStruct = arr$[i$];
         sb.append(dataStruct.toString() + "\t" + (String)this.dataStructuresMap.get(dataStruct) + "\n");
      }

      sb.append("Split FEATS column: " + this.useSplitFeats + "\n");
      return sb.toString();
   }

   public static enum ColumnNames {
      POS,
      DEP,
      LEX,
      LEMMA,
      CPOS,
      FEATS;

      private ColumnNames() {
      }
   }

   public static enum DataStructures {
      STACK,
      INPUT,
      LEFTCONTEXT,
      RIGHTCONTEXT;

      private DataStructures() {
      }
   }
}
