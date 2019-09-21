package vn.edu.vnu.uet.nlp.segmenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import vn.edu.vnu.uet.nlp.utils.OldLogging;

public class FeatureExtractor {
   private FeatureMap featureMap = new FeatureMap();
   private List<List<SegmentFeature>> listOfSegmentFeatureLists = new ArrayList();
   private static Map<String, String> normalizationMap = new HashMap();
   private static Set<String> normalizationSet;

   public FeatureExtractor() {
   }

   public FeatureExtractor(String featMapPath) throws ClassNotFoundException, IOException {
      this.loadMap(featMapPath);
   }

   public void extract(List<String> sentences, int mode) {
      for(int i = 0; i < sentences.size(); ++i) {
         this.extract((String)sentences.get(i), mode);
         if (i % 1000 == 999 || i == sentences.size() - 1) {
            OldLogging.info(i + 1 + " sentences extracted to features");
         }
      }

   }

   public List<SyllabelFeature> extract(String sentence, int mode) {
      List<SyllabelFeature> sylList = convertToFeatureOfSyllabel(sentence, mode);
      int length = sylList.size();
      if (length == 0) {
         return null;
      } else {
         SortedSet<Integer> indexSet = new TreeSet();
         List<SegmentFeature> segfeats = new ArrayList();

         for(int i = 2; i < length - 2 - 1; ++i) {
            String featureName;
            int j;
            for(j = i - 2; j <= i + 2; ++j) {
               featureName = j - i + "|" + ((SyllabelFeature)sylList.get(j)).getSyllabel().toLowerCase();
               indexSet.add(this.featureMap.getIndex(featureName, mode));
               if (((SyllabelFeature)sylList.get(j)).getType() != SyllableType.LOWER) {
                  featureName = j - i + ":" + ((SyllabelFeature)sylList.get(j)).getType();
                  indexSet.add(this.featureMap.getIndex(featureName, mode));
               }
            }

            for(j = i - 2; j < i + 2; ++j) {
               featureName = j - i + "||" + ((SyllabelFeature)sylList.get(j)).getSyllabel().toLowerCase() + " " + ((SyllabelFeature)sylList.get(j + 1)).getSyllabel().toLowerCase();
               indexSet.add(this.featureMap.getIndex(featureName, mode));
               if (((SyllabelFeature)sylList.get(j)).getType() != SyllableType.LOWER) {
                  featureName = j - i + "::" + ((SyllabelFeature)sylList.get(j)).getType() + " " + ((SyllabelFeature)sylList.get(j + 1)).getType();
                  indexSet.add(this.featureMap.getIndex(featureName, mode));
               }
            }

            for(j = i - 2; j < i + 2 - 1; ++j) {
               if (((SyllabelFeature)sylList.get(j)).getType() != SyllableType.LOWER) {
                  featureName = j - i + ":::" + ((SyllabelFeature)sylList.get(j)).getType() + " " + ((SyllabelFeature)sylList.get(j + 1)).getType() + " " + ((SyllabelFeature)sylList.get(j + 2)).getType();
                  indexSet.add(this.featureMap.getIndex(featureName, mode));
               }
            }

            String thisSyl = ((SyllabelFeature)sylList.get(i)).getSyllabel().toLowerCase();
            String nextSyl = ((SyllabelFeature)sylList.get(i + 1)).getSyllabel().toLowerCase();
            if (((SyllabelFeature)sylList.get(i)).getType() == SyllableType.UPPER && ((SyllabelFeature)sylList.get(i + 1)).getType() == SyllableType.UPPER) {
               if (SyllableList.isVNsyl(thisSyl) && !SyllableList.isVNsyl(nextSyl) || SyllableList.isVNsyl(nextSyl) && !SyllableList.isVNsyl(thisSyl)) {
                  featureName = "(0:vi&&1:en)||(0:en&&1:vi)";
                  indexSet.add(this.featureMap.getIndex(featureName, mode));
               }

               if (FamilyName.isVNFamilyName(thisSyl)) {
                  featureName = "0.isVNFamilyName";
                  indexSet.add(this.featureMap.getIndex(featureName, mode));
               }
            }

            if (((SyllabelFeature)sylList.get(i)).getType() == SyllableType.LOWER && ((SyllabelFeature)sylList.get(i + 1)).getType() == SyllableType.LOWER && thisSyl.equalsIgnoreCase(nextSyl)) {
               featureName = "0&&1.reduplicativeword";
               indexSet.add(this.featureMap.getIndex(featureName, mode));
            }

            if (indexSet.size() > 0) {
               segfeats.add(new SegmentFeature(((SyllabelFeature)sylList.get(i)).getLabel(), indexSet));
            }

            featureName = "";
            indexSet.clear();
         }

         this.listOfSegmentFeatureLists.add(segfeats);
         return sylList;
      }
   }

   public static List<SyllabelFeature> convertToFeatureOfSyllabel(String sentence, int mode) {
      String sent = sentence.trim();
      if (!sent.equals(" ") && !sent.isEmpty()) {
         for(int i = 0; i < 2; ++i) {
            sent = "<s> " + sent + " " + "</s>";
         }

         return token(sent, mode);
      } else {
         return new ArrayList();
      }
   }

   public static List<SyllabelFeature> token(String sent, int mode) {
      List<SyllabelFeature> list = new ArrayList();
      String[] tokens = sent.split("\\s+");
      String[] var4;
      int var5;
      int var6;
      String token;
      String tmp;
      if (mode != 0 && mode != 2) {
         var4 = tokens;
         var5 = tokens.length;

         for(var6 = 0; var6 < var5; ++var6) {
            token = var4[var6];
            tmp = normalize(token);
            list.add(new SyllabelFeature(tmp, typeOf(tmp), 0));
         }
      } else {
         var4 = tokens;
         var5 = tokens.length;

         for(var6 = 0; var6 < var5; ++var6) {
            token = var4[var6];
            if (!token.contains("_")) {
               tmp = normalize(token);
               list.add(new SyllabelFeature(tmp, typeOf(tmp), 0));
            } else {
               String[] tmp = token.split("_");

               for(int i = 0; i < tmp.length - 1; ++i) {
                  String tmp_i = normalize(tmp[i]);
                  list.add(new SyllabelFeature(tmp_i, typeOf(tmp_i), 1));
               }

               try {
                  String tmp_last = normalize(tmp[tmp.length - 1]);
                  list.add(new SyllabelFeature(tmp_last, typeOf(tmp_last), 0));
               } catch (Exception var11) {
               }
            }
         }
      }

      return list;
   }

   public static String normalize(String token) {
      if (SyllableList.isVNsyl(token)) {
         return token;
      } else {
         Iterator var1 = normalizationSet.iterator();

         while(var1.hasNext()) {
            String wrongTyping = (String)var1.next();
            if (token.contains(wrongTyping)) {
               token = token.replace(wrongTyping, (CharSequence)normalizationMap.get(wrongTyping));
               break;
            }
         }

         return token;
      }
   }

   public static SyllableType typeOf(String syllabel) {
      if (syllabel.equals("<s>")) {
         return SyllableType.BOS;
      } else if (syllabel.equals("</s>")) {
         return SyllableType.EOS;
      } else {
         boolean upper = false;
         boolean lower = false;
         boolean num = false;
         boolean other = false;
         if (!syllabel.matches("\\p{Upper}\\p{L}*\\.") && !syllabel.matches("\\p{Upper}\\p{L}*-\\w+")) {
            for(int i = 0; i < syllabel.length(); ++i) {
               char character = syllabel.charAt(i);
               if (!Character.isLetterOrDigit(character)) {
                  if (character != '.' && character != ',') {
                     return SyllableType.OTHER;
                  }

                  other = true;
               } else if (Character.isDigit(character)) {
                  num = true;
               } else if (Character.isLowerCase(character)) {
                  lower = true;
               } else {
                  upper = true;
               }
            }

            if (num) {
               if (syllabel.matches("[-+]?\\d+([\\.,]\\d+)*")) {
                  return SyllableType.NUMBER;
               } else {
                  return SyllableType.OTHER;
               }
            } else if (other) {
               return SyllableType.OTHER;
            } else if (lower) {
               if (upper) {
                  return SyllableType.UPPER;
               } else {
                  return SyllableType.LOWER;
               }
            } else if (upper) {
               return SyllableType.ALLUPPER;
            } else {
               return SyllableType.OTHER;
            }
         } else {
            return SyllableType.UPPER;
         }
      }
   }

   protected FeatureMap getFeatureMap() {
      return this.featureMap;
   }

   protected void saveMap(String path) {
      try {
         this.featureMap.save(path);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   protected void loadMap(String path) throws ClassNotFoundException, IOException {
      this.featureMap.load(path);
   }

   protected void clearList() {
      this.listOfSegmentFeatureLists.clear();
   }

   protected void clearMap() {
      this.featureMap.clear();
   }

   protected int getNumSents() {
      return this.listOfSegmentFeatureLists.size();
   }

   protected int getNumSamples() {
      int cnt = 0;

      for(int i = 0; i < this.listOfSegmentFeatureLists.size(); ++i) {
         cnt += ((List)this.listOfSegmentFeatureLists.get(i)).size();
      }

      return cnt;
   }

   protected List<List<SegmentFeature>> getSegmentList() {
      return this.listOfSegmentFeatureLists;
   }

   public int getFeatureMapSize() {
      return this.featureMap.getSize();
   }

   static {
      normalizationMap.put("òa", "oà");
      normalizationMap.put("óa", "oá");
      normalizationMap.put("ỏa", "oả");
      normalizationMap.put("õa", "oã");
      normalizationMap.put("ọa", "oạ");
      normalizationMap.put("òe", "oè");
      normalizationMap.put("óe", "oé");
      normalizationMap.put("ỏe", "oẻ");
      normalizationMap.put("õe", "oẽ");
      normalizationMap.put("ọe", "oẹ");
      normalizationMap.put("ùy", "uỳ");
      normalizationMap.put("úy", "uý");
      normalizationMap.put("ủy", "uỷ");
      normalizationMap.put("ũy", "uỹ");
      normalizationMap.put("ụy", "uỵ");
      normalizationMap.put("Ủy", "Uỷ");
      normalizationSet = normalizationMap.keySet();
   }
}
