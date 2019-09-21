package org.maltparser.concurrent.graph;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;

public final class ConcurrentDependencyEdge implements Comparable<ConcurrentDependencyEdge> {
   private final ConcurrentDependencyNode source;
   private final ConcurrentDependencyNode target;
   private final SortedMap<Integer, String> labels;

   protected ConcurrentDependencyEdge(ConcurrentDependencyEdge edge) throws ConcurrentGraphException {
      this.source = edge.source;
      this.target = edge.target;
      this.labels = new TreeMap(edge.labels);
   }

   protected ConcurrentDependencyEdge(DataFormat dataFormat, ConcurrentDependencyNode _source, ConcurrentDependencyNode _target, SortedMap<Integer, String> _labels) throws ConcurrentGraphException {
      if (_source == null) {
         throw new ConcurrentGraphException("Not allowed to have an edge without a source node");
      } else if (_target == null) {
         throw new ConcurrentGraphException("Not allowed to have an edge without a target node");
      } else {
         this.source = _source;
         this.target = _target;
         if (this.target.getIndex() == 0) {
            throw new ConcurrentGraphException("Not allowed to have an edge target as root node");
         } else {
            this.labels = new TreeMap();
            if (_labels != null) {
               Iterator i$ = _labels.keySet().iterator();

               while(i$.hasNext()) {
                  Integer i = (Integer)i$.next();
                  if (dataFormat.getColumnDescription(i).getCategory() == 3) {
                     this.labels.put(i, _labels.get(i));
                  }
               }
            }

         }
      }
   }

   public ConcurrentDependencyNode getSource() {
      return this.source;
   }

   public ConcurrentDependencyNode getTarget() {
      return this.target;
   }

   public String getLabel(ColumnDescription column) {
      if (this.labels.containsKey(column.getPosition())) {
         return (String)this.labels.get(column.getPosition());
      } else {
         return column.getCategory() == 7 ? column.getDefaultOutput() : "";
      }
   }

   public String getLabel(String columnName) {
      ColumnDescription column = this.source.getDataFormat().getColumnDescription(columnName);
      if (column != null) {
         if (this.labels.containsKey(column.getPosition())) {
            return (String)this.labels.get(column.getPosition());
         }

         if (column.getCategory() == 7) {
            return column.getDefaultOutput();
         }
      }

      return "";
   }

   public int nLabels() {
      return this.labels.size();
   }

   public boolean isLabeled() {
      return this.labels.size() > 0;
   }

   public int compareTo(ConcurrentDependencyEdge that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == that) {
         return 0;
      } else if (this.target.getIndex() < that.target.getIndex()) {
         return -1;
      } else if (this.target.getIndex() > that.target.getIndex()) {
         return 1;
      } else if (this.source.getIndex() < that.source.getIndex()) {
         return -1;
      } else if (this.source.getIndex() > that.source.getIndex()) {
         return 1;
      } else if (this.labels.equals(that.labels)) {
         return 0;
      } else {
         Iterator<Integer> itthis = this.labels.keySet().iterator();
         Iterator itthat = that.labels.keySet().iterator();

         while(itthis.hasNext() && itthat.hasNext()) {
            int keythis = (Integer)itthis.next();
            int keythat = (Integer)itthat.next();
            if (keythis < keythat) {
               return -1;
            }

            if (keythis > keythat) {
               return 1;
            }

            if (((String)this.labels.get(keythis)).compareTo((String)that.labels.get(keythat)) != 0) {
               return ((String)this.labels.get(keythis)).compareTo((String)that.labels.get(keythat));
            }
         }

         if (!itthis.hasNext() && itthat.hasNext()) {
            return -1;
         } else {
            return itthis.hasNext() && !itthat.hasNext() ? 1 : 0;
         }
      }
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.source == null ? 0 : this.source.hashCode());
      result = 31 * result + (this.target == null ? 0 : this.target.hashCode());
      result = 31 * result + (this.labels == null ? 0 : this.labels.hashCode());
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
         ConcurrentDependencyEdge other = (ConcurrentDependencyEdge)obj;
         if (this.source == null) {
            if (other.source != null) {
               return false;
            }
         } else if (!this.source.equals(other.source)) {
            return false;
         }

         if (this.target == null) {
            if (other.target != null) {
               return false;
            }
         } else if (!this.target.equals(other.target)) {
            return false;
         }

         if (this.labels == null) {
            if (other.labels != null) {
               return false;
            }
         } else if (!this.labels.equals(other.labels)) {
            return false;
         }

         return true;
      }
   }
}
