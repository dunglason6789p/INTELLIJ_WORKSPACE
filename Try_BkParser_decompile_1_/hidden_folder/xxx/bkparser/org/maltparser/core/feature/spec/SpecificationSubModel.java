package org.maltparser.core.feature.spec;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class SpecificationSubModel implements Iterable<String> {
   private final Pattern blanks;
   private final Set<String> featureSpecSet;
   private final String name;

   public SpecificationSubModel() {
      this("MAIN");
   }

   public SpecificationSubModel(String _name) {
      this.blanks = Pattern.compile("\\s+");
      this.name = _name;
      this.featureSpecSet = new TreeSet();
   }

   public void add(String featureSpec) {
      if (featureSpec != null && featureSpec.trim().length() > 0) {
         String strippedFeatureSpec = this.blanks.matcher(featureSpec).replaceAll("");
         this.featureSpecSet.add(strippedFeatureSpec);
      }

   }

   public String getSubModelName() {
      return this.name;
   }

   public int size() {
      return this.featureSpecSet.size();
   }

   public Iterator<String> iterator() {
      return this.featureSpecSet.iterator();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.featureSpecSet.iterator();

      while(i$.hasNext()) {
         String str = (String)i$.next();
         sb.append(str);
         sb.append('\n');
      }

      return sb.toString();
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.featureSpecSet == null ? 0 : this.featureSpecSet.hashCode());
      result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
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
         SpecificationSubModel other = (SpecificationSubModel)obj;
         if (this.featureSpecSet == null) {
            if (other.featureSpecSet != null) {
               return false;
            }
         } else if (!this.featureSpecSet.equals(other.featureSpecSet)) {
            return false;
         }

         if (this.name == null) {
            if (other.name != null) {
               return false;
            }
         } else if (!this.name.equals(other.name)) {
            return false;
         }

         return true;
      }
   }
}
