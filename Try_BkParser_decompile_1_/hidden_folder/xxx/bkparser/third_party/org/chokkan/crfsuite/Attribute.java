package third_party.org.chokkan.crfsuite;

public class Attribute {
   private long swigCPtr;
   protected boolean swigCMemOwn;

   public Attribute(long cPtr, boolean cMemoryOwn) {
      this.swigCMemOwn = cMemoryOwn;
      this.swigCPtr = cPtr;
   }

   public static long getCPtr(Attribute obj) {
      return obj == null ? 0L : obj.swigCPtr;
   }

   protected void finalize() {
      this.delete();
   }

   public synchronized void delete() {
      if (this.swigCPtr != 0L) {
         if (this.swigCMemOwn) {
            this.swigCMemOwn = false;
            crfsuiteJNI.delete_Attribute(this.swigCPtr);
         }

         this.swigCPtr = 0L;
      }

   }

   public void setAttr(String value) {
      crfsuiteJNI.Attribute_attr_set(this.swigCPtr, this, value);
   }

   public String getAttr() {
      return crfsuiteJNI.Attribute_attr_get(this.swigCPtr, this);
   }

   public void setValue(double value) {
      crfsuiteJNI.Attribute_value_set(this.swigCPtr, this, value);
   }

   public double getValue() {
      return crfsuiteJNI.Attribute_value_get(this.swigCPtr, this);
   }

   public Attribute() {
      this(crfsuiteJNI.new_Attribute__SWIG_0(), true);
   }

   public Attribute(String name) {
      this(crfsuiteJNI.new_Attribute__SWIG_1(name), true);
   }

   public Attribute(String name, double val) {
      this(crfsuiteJNI.new_Attribute__SWIG_2(name, val), true);
   }
}
