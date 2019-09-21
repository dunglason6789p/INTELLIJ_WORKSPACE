package vn.edu.vnu.uet.liblinear;

public interface Feature {
   int getIndex();

   double getValue();

   void setValue(double var1);
}
