package org.maltparser.parser.history.kbest;

public class Candidate {
   protected int actionCode;

   public Candidate() {
      this.reset();
   }

   public int getActionCode() {
      return this.actionCode;
   }

   public void setActionCode(int actionCode) {
      this.actionCode = actionCode;
   }

   public void reset() {
      this.actionCode = -1;
   }

   public int hashCode() {
      return 31 + this.actionCode;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         return this.actionCode == ((Candidate)obj).actionCode;
      }
   }

   public String toString() {
      return Integer.toString(this.actionCode);
   }
}
