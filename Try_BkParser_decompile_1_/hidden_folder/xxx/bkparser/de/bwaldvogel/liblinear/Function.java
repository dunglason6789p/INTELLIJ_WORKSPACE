package de.bwaldvogel.liblinear;

interface Function {
   double fun(double[] var1);

   void grad(double[] var1, double[] var2);

   void Hv(double[] var1, double[] var2);

   int get_nr_variable();
}
