package org.maltparser.parser.history.kbest;

public class ScoredCandidate extends Candidate {
   protected float score;

   public ScoredCandidate() {
   }

   public float getScore() {
      return this.score;
   }

   public void setScore(Float score) {
      this.score = score;
   }

   public void reset() {
      super.reset();
      this.score = 0.0F / 0.0;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         ScoredCandidate item = (ScoredCandidate)obj;
         return this.actionCode == item.actionCode && this.score == item.score;
      }
   }

   public int hashCode() {
      return (217 + this.actionCode) * 31 + Float.floatToIntBits(this.score);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      sb.append('\t');
      sb.append(this.score);
      return sb.toString();
   }
}
