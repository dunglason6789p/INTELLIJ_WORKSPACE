package org.maltparser.core.io.dataformat;

public class DataFormatEntry {
   private String dataFormatEntryName;
   private String category;
   private String type;
   private String defaultOutput;
   private int cachedHash;

   public DataFormatEntry(String dataFormatEntryName, String category, String type, String defaultOutput) {
      this.setDataFormatEntryName(dataFormatEntryName);
      this.setCategory(category);
      this.setType(type);
      this.setDefaultOutput(defaultOutput);
   }

   public String getDataFormatEntryName() {
      return this.dataFormatEntryName;
   }

   public void setDataFormatEntryName(String dataFormatEntryName) {
      this.dataFormatEntryName = dataFormatEntryName.toUpperCase();
   }

   public String getCategory() {
      return this.category;
   }

   public void setCategory(String category) {
      this.category = category.toUpperCase();
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type.toUpperCase();
   }

   public String getDefaultOutput() {
      return this.defaultOutput;
   }

   public void setDefaultOutput(String defaultOutput) {
      this.defaultOutput = defaultOutput;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         boolean var10000;
         label65: {
            label57: {
               DataFormatEntry objC = (DataFormatEntry)obj;
               if (this.dataFormatEntryName == null) {
                  if (objC.dataFormatEntryName != null) {
                     break label57;
                  }
               } else if (!this.dataFormatEntryName.equals(objC.dataFormatEntryName)) {
                  break label57;
               }

               if (this.type == null) {
                  if (objC.type != null) {
                     break label57;
                  }
               } else if (!this.type.equals(objC.type)) {
                  break label57;
               }

               if (this.category == null) {
                  if (objC.category != null) {
                     break label57;
                  }
               } else if (!this.category.equals(objC.category)) {
                  break label57;
               }

               if (this.defaultOutput == null) {
                  if (objC.defaultOutput == null) {
                     break label65;
                  }
               } else if (this.defaultOutput.equals(objC.defaultOutput)) {
                  break label65;
               }
            }

            var10000 = false;
            return var10000;
         }

         var10000 = true;
         return var10000;
      }
   }

   public int hashCode() {
      if (this.cachedHash == 0) {
         int hash = 7;
         int hash = 31 * hash + (null == this.dataFormatEntryName ? 0 : this.dataFormatEntryName.hashCode());
         hash = 31 * hash + (null == this.type ? 0 : this.type.hashCode());
         hash = 31 * hash + (null == this.category ? 0 : this.category.hashCode());
         hash = 31 * hash + (null == this.defaultOutput ? 0 : this.defaultOutput.hashCode());
         this.cachedHash = hash;
      }

      return this.cachedHash;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.dataFormatEntryName);
      sb.append("\t");
      sb.append(this.category);
      sb.append("\t");
      sb.append(this.type);
      if (this.defaultOutput != null) {
         sb.append("\t");
         sb.append(this.defaultOutput);
      }

      return sb.toString();
   }
}
