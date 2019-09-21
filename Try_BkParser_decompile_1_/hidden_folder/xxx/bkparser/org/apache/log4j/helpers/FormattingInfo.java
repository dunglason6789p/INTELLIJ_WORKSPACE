package org.apache.log4j.helpers;

public class FormattingInfo {
   int min = -1;
   int max = 2147483647;
   boolean leftAlign = false;

   public FormattingInfo() {
   }

   void reset() {
      this.min = -1;
      this.max = 2147483647;
      this.leftAlign = false;
   }

   void dump() {
      LogLog.debug("min=" + this.min + ", max=" + this.max + ", leftAlign=" + this.leftAlign);
   }
}
