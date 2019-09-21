package org.maltparser.parser.transition;

public class Transition implements Comparable<Transition> {
   private final int code;
   private final String symbol;
   private final boolean labeled;
   private final int cachedHash;

   public Transition(int code, String symbol, boolean labeled) {
      this.code = code;
      this.symbol = symbol;
      this.labeled = labeled;
      int prime = true;
      int result = 31 + code;
      result = 31 * result + (labeled ? 1231 : 1237);
      this.cachedHash = 31 * result + (symbol == null ? 0 : symbol.hashCode());
   }

   public int getCode() {
      return this.code;
   }

   public String getSymbol() {
      return this.symbol;
   }

   public boolean isLabeled() {
      return this.labeled;
   }

   public int compareTo(Transition that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this.code < that.code) {
         return -1;
      } else {
         return this.code > that.code ? 1 : 0;
      }
   }

   public int hashCode() {
      return this.cachedHash;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         Transition other = (Transition)obj;
         if (this.code != other.code) {
            return false;
         } else if (this.labeled != other.labeled) {
            return false;
         } else {
            if (this.symbol == null) {
               if (other.symbol != null) {
                  return false;
               }
            } else if (!this.symbol.equals(other.symbol)) {
               return false;
            }

            return true;
         }
      }
   }

   public String toString() {
      return this.symbol + " [" + this.code + "] " + this.labeled;
   }
}
