package org.maltparser.core.feature.spec;

import java.util.Iterator;
import java.util.LinkedHashMap;
import org.maltparser.core.exception.MaltChainedException;

public class SpecificationModel implements Iterable<SpecificationSubModel> {
   private final String specModelName;
   private final LinkedHashMap<String, SpecificationSubModel> subModelMap;

   public SpecificationModel() throws MaltChainedException {
      this((String)null);
   }

   public SpecificationModel(String _specModelName) throws MaltChainedException {
      this.specModelName = _specModelName;
      this.subModelMap = new LinkedHashMap();
   }

   public void add(String featureSpec) throws MaltChainedException {
      this.add("MAIN", featureSpec);
   }

   public void add(String subModelName, String featureSpec) throws MaltChainedException {
      if (subModelName != null && subModelName.length() >= 1 && !subModelName.toUpperCase().equals("MAIN")) {
         if (!this.subModelMap.containsKey(subModelName.toUpperCase())) {
            this.subModelMap.put(subModelName.toUpperCase(), new SpecificationSubModel(subModelName.toUpperCase()));
         }

         ((SpecificationSubModel)this.subModelMap.get(subModelName.toUpperCase())).add(featureSpec);
      } else {
         if (!this.subModelMap.containsKey("MAIN")) {
            this.subModelMap.put("MAIN", new SpecificationSubModel("MAIN"));
         }

         ((SpecificationSubModel)this.subModelMap.get("MAIN")).add(featureSpec);
      }

   }

   public String getSpecModelName() {
      return this.specModelName;
   }

   public Iterator<SpecificationSubModel> iterator() {
      return this.subModelMap.values().iterator();
   }

   public int size() {
      return this.subModelMap.size();
   }

   public SpecificationSubModel getSpecSubModel(String subModelName) {
      return (SpecificationSubModel)this.subModelMap.get(subModelName);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.iterator();

      while(true) {
         SpecificationSubModel subModel;
         do {
            if (!i$.hasNext()) {
               return sb.toString();
            }

            subModel = (SpecificationSubModel)i$.next();
         } while(subModel.size() <= 0);

         if (this.subModelMap.size() != 1 || subModel.getSubModelName().equalsIgnoreCase("MAIN")) {
            sb.append(subModel.getSubModelName());
            sb.append('\n');
         }

         sb.append(subModel.toString());
      }
   }
}
