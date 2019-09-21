package third_party.org.chokkan.crfsuite;

public class StringList {
   private long swigCPtr;
   protected boolean swigCMemOwn;

   public StringList(long cPtr, boolean cMemoryOwn) {
      this.swigCMemOwn = cMemoryOwn;
      this.swigCPtr = cPtr;
   }

   public static long getCPtr(StringList obj) {
      return obj == null ? 0L : obj.swigCPtr;
   }

   protected void finalize() {
      this.delete();
   }

   public synchronized void delete() {
      if (this.swigCPtr != 0L) {
         if (this.swigCMemOwn) {
            this.swigCMemOwn = false;
            crfsuiteJNI.delete_StringList(this.swigCPtr);
         }

         this.swigCPtr = 0L;
      }

   }

   public StringList() {
      this(crfsuiteJNI.new_StringList__SWIG_0(), true);
   }

   public StringList(long n) {
      this(crfsuiteJNI.new_StringList__SWIG_1(n), true);
   }

   public long size() {
      return crfsuiteJNI.StringList_size(this.swigCPtr, this);
   }

   public long capacity() {
      return crfsuiteJNI.StringList_capacity(this.swigCPtr, this);
   }

   public void reserve(long n) {
      crfsuiteJNI.StringList_reserve(this.swigCPtr, this, n);
   }

   public boolean isEmpty() {
      return crfsuiteJNI.StringList_isEmpty(this.swigCPtr, this);
   }

   public void clear() {
      crfsuiteJNI.StringList_clear(this.swigCPtr, this);
   }

   public void add(String x) {
      crfsuiteJNI.StringList_add(this.swigCPtr, this, x);
   }

   public String get(int i) {
      return crfsuiteJNI.StringList_get(this.swigCPtr, this, i);
   }

   public void set(int i, String val) {
      crfsuiteJNI.StringList_set(this.swigCPtr, this, i, val);
   }
}
