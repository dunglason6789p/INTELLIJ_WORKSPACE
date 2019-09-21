package org.maltparser.core.propagation.spec;

public class PropagationSpec {
   public static final long serialVersionUID = 1L;
   private final String from;
   private final String to;
   private final String _for;
   private final String over;

   public PropagationSpec(String from, String to, String _for, String over) {
      this.from = from;
      this.to = to;
      this._for = _for;
      this.over = over;
   }

   public String getFrom() {
      return this.from;
   }

   public String getTo() {
      return this.to;
   }

   public String getFor() {
      return this._for;
   }

   public String getOver() {
      return this.over;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this._for == null ? 0 : this._for.hashCode());
      result = 31 * result + (this.from == null ? 0 : this.from.hashCode());
      result = 31 * result + (this.over == null ? 0 : this.over.hashCode());
      result = 31 * result + (this.to == null ? 0 : this.to.hashCode());
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
         PropagationSpec other = (PropagationSpec)obj;
         if (this._for == null) {
            if (other._for != null) {
               return false;
            }
         } else if (!this._for.equals(other._for)) {
            return false;
         }

         if (this.from == null) {
            if (other.from != null) {
               return false;
            }
         } else if (!this.from.equals(other.from)) {
            return false;
         }

         if (this.over == null) {
            if (other.over != null) {
               return false;
            }
         } else if (!this.over.equals(other.over)) {
            return false;
         }

         if (this.to == null) {
            if (other.to != null) {
               return false;
            }
         } else if (!this.to.equals(other.to)) {
            return false;
         }

         return true;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("FROM: ");
      sb.append(this.from);
      sb.append("\n");
      sb.append("TO  : ");
      sb.append(this.to);
      sb.append("\n");
      sb.append("FOR : ");
      sb.append(this._for);
      sb.append("\n");
      sb.append("OVER: ");
      sb.append(this.over);
      sb.append("\n");
      return sb.toString();
   }
}
