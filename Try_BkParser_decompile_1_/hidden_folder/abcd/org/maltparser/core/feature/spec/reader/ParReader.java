/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.spec.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.spec.SpecificationModels;
import org.maltparser.core.feature.spec.reader.FeatureSpecReader;

public class ParReader
implements FeatureSpecReader {
    private EnumMap<ColumnNames, String> columnNameMap;
    private EnumMap<DataStructures, String> dataStructuresMap;
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

    @Override
    public void load(URL specModelURL, SpecificationModels featureSpecModels) throws MaltChainedException {
        BufferedReader br = null;
        Pattern tabPattern = Pattern.compile("\t");
        if (specModelURL == null) {
            throw new FeatureException("The feature specification file cannot be found. ");
        }
        try {
            br = new BufferedReader(new InputStreamReader(specModelURL.openStream()));
        }
        catch (IOException e) {
            throw new FeatureException("Could not read the feature specification file '" + specModelURL.toString() + "'. ", e);
        }
        if (br != null) {
            int specModelIndex = featureSpecModels.getNextIndex();
            StringBuilder featureText = new StringBuilder();
            String splitfeats = "";
            ArrayList<String> fileLines = new ArrayList<String>();
            ArrayList orderFileLines = new ArrayList();
            do {
                String fileLine;
                try {
                    fileLine = br.readLine();
                }
                catch (IOException e) {
                    throw new FeatureException("Could not read the feature specification file '" + specModelURL.toString() + "'. ", e);
                }
                if (fileLine == null) break;
                if (fileLine.length() <= 1 && fileLine.trim().substring(0, 2).trim().equals("--")) continue;
                fileLines.add(fileLine);
            } while (true);
            try {
                br.close();
            }
            catch (IOException e) {
                throw new FeatureException("Could not close the feature specification file '" + specModelURL.toString() + "'. ", e);
            }
            for (int j = 0; j < fileLines.size(); ++j) {
                orderFileLines.add(fileLines.get(j));
            }
            boolean deprel = false;
            for (int j = 0; j < orderFileLines.size(); ++j) {
                int i;
                deprel = false;
                featureText.setLength(0);
                splitfeats = "";
                String[] items = tabPattern.split((CharSequence)orderFileLines.get(j));
                if (items.length < 2) {
                    throw new FeatureException("The feature specification file '" + specModelURL.toString() + "' must contain at least two columns.");
                }
                if (!this.columnNameMap.containsKey((Object)ColumnNames.valueOf(items[0].trim())) && !this.columnNameMap.containsValue(items[0].trim())) {
                    throw new FeatureException("Column one in the feature specification file '" + specModelURL.toString() + "' contains an unknown value '" + items[0].trim() + "'. ");
                }
                if (items[0].trim().equalsIgnoreCase("DEP") || items[0].trim().equalsIgnoreCase("DEPREL")) {
                    featureText.append("OutputColumn(DEPREL, ");
                    deprel = true;
                } else {
                    if (this.columnNameMap.containsKey((Object)ColumnNames.valueOf(items[0].trim()))) {
                        featureText.append("InputColumn(" + this.columnNameMap.get((Object)ColumnNames.valueOf(items[0].trim())) + ", ");
                    } else if (this.columnNameMap.containsValue(items[0].trim())) {
                        featureText.append("InputColumn(" + items[0].trim() + ", ");
                    }
                    if (items[0].trim().equalsIgnoreCase("FEATS") && this.isUseSplitFeats()) {
                        splitfeats = "Split(";
                    }
                }
                if (!(items[1].trim().equalsIgnoreCase("STACK") || items[1].trim().equalsIgnoreCase("INPUT") || items[1].trim().equalsIgnoreCase("CONTEXT"))) {
                    throw new FeatureException("Column two in the feature specification file '" + specModelURL.toString() + "' should be either 'STACK', 'INPUT' or 'CONTEXT' (Covington), not '" + items[1].trim() + "'. ");
                }
                int offset = 0;
                if (items.length >= 3) {
                    try {
                        offset = new Integer(Integer.parseInt(items[2]));
                    }
                    catch (NumberFormatException e) {
                        throw new FeatureException("The feature specification file '" + specModelURL.toString() + "' contains a illegal integer value. ", e);
                    }
                }
                String functionArg = "";
                if (items[1].trim().equalsIgnoreCase("CONTEXT")) {
                    functionArg = offset >= 0 ? this.dataStructuresMap.get((Object)DataStructures.valueOf("LEFTCONTEXT")) + "[" + offset + "]" : this.dataStructuresMap.get((Object)DataStructures.valueOf("RIGHTCONTEXT")) + "[" + Math.abs(offset + 1) + "]";
                } else if (this.dataStructuresMap.containsKey((Object)DataStructures.valueOf(items[1].trim()))) {
                    functionArg = this.covington ? (this.dataStructuresMap.get((Object)DataStructures.valueOf(items[1].trim())).equalsIgnoreCase("Stack") ? "Left[" + offset + "]" : "Right[" + offset + "]") : this.dataStructuresMap.get((Object)DataStructures.valueOf(items[1].trim())) + "[" + offset + "]";
                } else if (this.dataStructuresMap.containsValue(items[1].trim())) {
                    functionArg = this.covington ? (items[1].trim().equalsIgnoreCase("Stack") ? "Left[" + offset + "]" : "Right[" + offset + "]") : items[1].trim() + "[" + offset + "]";
                } else {
                    throw new FeatureException("Column two in the feature specification file '" + specModelURL.toString() + "' should not contain the value '" + items[1].trim());
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
                if (linearOffset < 0) {
                    linearOffset = Math.abs(linearOffset);
                    for (i = 0; i < linearOffset; ++i) {
                        functionArg = "pred(" + functionArg + ")";
                    }
                } else if (linearOffset > 0) {
                    for (i = 0; i < linearOffset; ++i) {
                        functionArg = "succ(" + functionArg + ")";
                    }
                }
                if (headOffset >= 0) {
                    for (i = 0; i < headOffset; ++i) {
                        functionArg = "head(" + functionArg + ")";
                    }
                } else {
                    throw new FeatureException("The feature specification file '" + specModelURL.toString() + "' should not contain a negative head function value. ");
                }
                if (depOffset < 0) {
                    depOffset = Math.abs(depOffset);
                    for (i = 0; i < depOffset; ++i) {
                        functionArg = "ldep(" + functionArg + ")";
                    }
                } else if (depOffset > 0) {
                    for (i = 0; i < depOffset; ++i) {
                        functionArg = "rdep(" + functionArg + ")";
                    }
                }
                if (sibOffset < 0) {
                    sibOffset = Math.abs(sibOffset);
                    for (i = 0; i < sibOffset; ++i) {
                        functionArg = "lsib(" + functionArg + ")";
                    }
                } else if (sibOffset > 0) {
                    for (i = 0; i < sibOffset; ++i) {
                        functionArg = "rsib(" + functionArg + ")";
                    }
                }
                if (deprel && (this.pppath || this.pplifted || this.ppcoveredRoot)) {
                    featureSpecModels.add(specModelIndex, this.mergePseudoProjColumns(functionArg));
                    continue;
                }
                if (suffixLength != 0) {
                    featureSpecModels.add(specModelIndex, "Suffix(" + featureText.toString() + functionArg + ")," + suffixLength + ")");
                    continue;
                }
                if (splitfeats.equals("Split(")) {
                    featureSpecModels.add(specModelIndex, splitfeats + featureText.toString() + functionArg + "),\\|)");
                    continue;
                }
                featureSpecModels.add(specModelIndex, featureText.toString() + functionArg + ")");
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
        }
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
            ++i;
            if (this.pplifted) {
                newFeatureText.append("OutputTable(PPLIFTED, ");
                newFeatureText.append(functionArg);
                if (++i < 3) {
                    newFeatureText.append("), ");
                } else {
                    newFeatureText.append(")");
                }
            }
            if (this.pppath) {
                newFeatureText.append("OutputTable(PPPATH, ");
                newFeatureText.append(functionArg);
                if (++i < 3) {
                    newFeatureText.append("), ");
                } else {
                    newFeatureText.append(")");
                }
            }
            if (this.ppcoveredRoot) {
                newFeatureText.append("OutputTable(PPCOVERED, ");
                newFeatureText.append(functionArg);
                if (++i < 3) {
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

    public EnumMap<ColumnNames, String> getColumnNameMap() {
        return this.columnNameMap;
    }

    public void initializeColumnNameMap() {
        this.columnNameMap = new EnumMap(ColumnNames.class);
        this.columnNameMap.put(ColumnNames.POS, "POSTAG");
        this.columnNameMap.put(ColumnNames.CPOS, "CPOSTAG");
        this.columnNameMap.put(ColumnNames.DEP, "DEPREL");
        this.columnNameMap.put(ColumnNames.LEX, "FORM");
        this.columnNameMap.put(ColumnNames.LEMMA, "LEMMA");
        this.columnNameMap.put(ColumnNames.FEATS, "FEATS");
    }

    public void setColumnNameMap(EnumMap<ColumnNames, String> columnNameMap) {
        this.columnNameMap = columnNameMap;
    }

    public EnumMap<DataStructures, String> getDataStructuresMap() {
        return this.dataStructuresMap;
    }

    public void initializeDataStructuresMap() {
        this.dataStructuresMap = new EnumMap(DataStructures.class);
        this.dataStructuresMap.put(DataStructures.STACK, "Stack");
        this.dataStructuresMap.put(DataStructures.INPUT, "Input");
    }

    public void setDataStructuresMap(EnumMap<DataStructures, String> dataStructuresMap) {
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
        for (ColumnNames columnName : ColumnNames.values()) {
            sb.append(columnName.toString() + "\t" + this.columnNameMap.get((Object)columnName) + "\n");
        }
        sb.append("Mapping of data structures:\n");
        for (Enum dataStruct : DataStructures.values()) {
            sb.append(dataStruct.toString() + "\t" + this.dataStructuresMap.get(dataStruct) + "\n");
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
        
    }

    public static enum DataStructures {
        STACK,
        INPUT,
        LEFTCONTEXT,
        RIGHTCONTEXT;
        
    }

}

