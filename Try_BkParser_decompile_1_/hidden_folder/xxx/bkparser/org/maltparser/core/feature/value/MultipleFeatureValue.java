package org.maltparser.core.feature.value;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.helper.HashSet;

public class MultipleFeatureValue extends FeatureValue {
   protected SortedMap<Integer, String> featureValues;

   public MultipleFeatureValue(Function function) {
      super(function);
      this.setFeatureValues(new TreeMap());
   }

   public void reset() {
      super.reset();
      this.featureValues.clear();
   }

   public void addFeatureValue(int code, String Symbol) {
      this.featureValues.put(code, Symbol);
   }

   protected void setFeatureValues(SortedMap<Integer, String> featureValues) {
      this.featureValues = featureValues;
   }

   public Set<Integer> getCodes() {
      return this.featureValues.keySet();
   }

   public int getFirstCode() {
      return (Integer)this.featureValues.firstKey();
   }

   public Set<String> getSymbols() {
      return new HashSet(this.featureValues.values());
   }

   public String getFirstSymbol() {
      return (String)this.featureValues.get(this.featureValues.firstKey());
   }

   public boolean isMultiple() {
      return true;
   }

   public int nFeatureValues() {
      return this.featureValues.size();
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         MultipleFeatureValue v = (MultipleFeatureValue)obj;
         return !this.featureValues.equals(v.featureValues) ? false : super.equals(obj);
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      sb.append('{');
      Iterator i$ = this.featureValues.keySet().iterator();

      while(i$.hasNext()) {
         Integer code = (Integer)i$.next();
         sb.append('{');
         sb.append((String)this.featureValues.get(code));
         sb.append("->");
         sb.append(code);
         sb.append('}');
      }

      sb.append('}');
      return sb.toString();
   }
}
