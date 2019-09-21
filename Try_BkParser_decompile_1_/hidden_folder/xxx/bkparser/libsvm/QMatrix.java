package libsvm;

abstract class QMatrix {
   QMatrix() {
   }

   abstract float[] get_Q(int var1, int var2);

   abstract double[] get_QD();

   abstract void swap_index(int var1, int var2);
}
