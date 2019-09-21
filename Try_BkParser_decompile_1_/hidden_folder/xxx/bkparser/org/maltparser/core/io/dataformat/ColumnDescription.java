package org.maltparser.core.io.dataformat;

import org.maltparser.core.exception.MaltChainedException;

public class ColumnDescription implements Comparable<ColumnDescription> {
   public static final int INPUT = 1;
   public static final int HEAD = 2;
   public static final int DEPENDENCY_EDGE_LABEL = 3;
   public static final int PHRASE_STRUCTURE_EDGE_LABEL = 4;
   public static final int PHRASE_STRUCTURE_NODE_LABEL = 5;
   public static final int SECONDARY_EDGE_LABEL = 6;
   public static final int IGNORE = 7;
   public static final String[] categories = new String[]{"", "INPUT", "HEAD", "DEPENDENCY_EDGE_LABEL", "PHRASE_STRUCTURE_EDGE_LABEL", "PHRASE_STRUCTURE_NODE_LABEL", "SECONDARY_EDGE_LABEL", "IGNORE"};
   public static final int STRING = 1;
   public static final int INTEGER = 2;
   public static final int BOOLEAN = 3;
   public static final int REAL = 4;
   public static final String[] types = new String[]{"", "STRING", "INTEGER", "BOOLEAN", "REAL"};
   private static int positionCounter = 0;
   private final int position;
   private final String name;
   private final int category;
   private final int type;
   private final String defaultOutput;
   private final String nullValueStrategy;
   private final boolean internal;

   public ColumnDescription(String name, int category, int type, String defaultOutput, String nullValueStrategy, boolean internal) throws MaltChainedException {
      this(positionCounter++, name, category, type, defaultOutput, nullValueStrategy, internal);
   }

   private ColumnDescription(int position, String name, int category, int type, String defaultOutput, String nullValueStrategy, boolean internal) throws MaltChainedException {
      this.position = position;
      this.name = name;
      this.category = category;
      this.type = type;
      this.defaultOutput = defaultOutput;
      this.nullValueStrategy = nullValueStrategy;
      this.internal = internal;
   }

   public int getPosition() {
      return this.position;
   }

   public String getName() {
      return this.name;
   }

   public String getDefaultOutput() {
      return this.defaultOutput;
   }

   public String getNullValueStrategy() {
      return this.nullValueStrategy;
   }

   public boolean isInternal() {
      return this.internal;
   }

   public int getCategory() {
      return this.category;
   }

   public String getCategoryName() {
      return this.category >= 1 && this.category <= 7 ? categories[this.category] : "";
   }

   public int getType() {
      return this.type;
   }

   public String getTypeName() {
      return this.type >= 1 && this.type <= 4 ? types[this.type] : "";
   }

   public int compareTo(ColumnDescription that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == that) {
         return 0;
      } else if (this.position < that.position) {
         return -1;
      } else {
         return this.position > that.position ? 1 : 0;
      }
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + this.category;
      result = 31 * result + (this.defaultOutput == null ? 0 : this.defaultOutput.hashCode());
      result = 31 * result + (this.internal ? 1231 : 1237);
      result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
      result = 31 * result + (this.nullValueStrategy == null ? 0 : this.nullValueStrategy.hashCode());
      result = 31 * result + this.position;
      result = 31 * result + this.type;
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
         ColumnDescription other = (ColumnDescription)obj;
         if (this.category != other.category) {
            return false;
         } else {
            if (this.defaultOutput == null) {
               if (other.defaultOutput != null) {
                  return false;
               }
            } else if (!this.defaultOutput.equals(other.defaultOutput)) {
               return false;
            }

            if (this.internal != other.internal) {
               return false;
            } else {
               if (this.name == null) {
                  if (other.name != null) {
                     return false;
                  }
               } else if (!this.name.equals(other.name)) {
                  return false;
               }

               if (this.nullValueStrategy == null) {
                  if (other.nullValueStrategy != null) {
                     return false;
                  }
               } else if (!this.nullValueStrategy.equals(other.nullValueStrategy)) {
                  return false;
               }

               if (this.position != other.position) {
                  return false;
               } else {
                  return this.type == other.type;
               }
            }
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.name);
      sb.append('\t');
      sb.append(this.category);
      sb.append('\t');
      sb.append(this.type);
      if (this.defaultOutput != null) {
         sb.append('\t');
         sb.append(this.defaultOutput);
      }

      return sb.toString();
   }

   public static int getCategory(String categoryName) {
      if (categoryName.equals("INPUT")) {
         return 1;
      } else if (categoryName.equals("HEAD")) {
         return 2;
      } else if (categoryName.equals("OUTPUT")) {
         return 3;
      } else if (categoryName.equals("DEPENDENCY_EDGE_LABEL")) {
         return 3;
      } else if (categoryName.equals("PHRASE_STRUCTURE_EDGE_LABEL")) {
         return 4;
      } else if (categoryName.equals("PHRASE_STRUCTURE_NODE_LABEL")) {
         return 5;
      } else if (categoryName.equals("SECONDARY_EDGE_LABEL")) {
         return 6;
      } else {
         return categoryName.equals("IGNORE") ? 7 : -1;
      }
   }

   public static int getType(String typeName) {
      if (typeName.equals("STRING")) {
         return 1;
      } else if (typeName.equals("INTEGER")) {
         return 2;
      } else if (typeName.equals("BOOLEAN")) {
         return 3;
      } else if (typeName.equals("REAL")) {
         return 4;
      } else {
         return typeName.equals("ECHO") ? 2 : -1;
      }
   }
}
