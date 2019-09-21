package third_party.org.chokkan.crfsuite;

public class ItemSequence {
   private long swigCPtr;
   protected boolean swigCMemOwn;

   public ItemSequence(long cPtr, boolean cMemoryOwn) {
      this.swigCMemOwn = cMemoryOwn;
      this.swigCPtr = cPtr;
   }

   public static long getCPtr(ItemSequence obj) {
      return obj == null ? 0L : obj.swigCPtr;
   }

   protected void finalize() {
      this.delete();
   }

   public synchronized void delete() {
      if (this.swigCPtr != 0L) {
         if (this.swigCMemOwn) {
            this.swigCMemOwn = false;
            crfsuiteJNI.delete_ItemSequence(this.swigCPtr);
         }

         this.swigCPtr = 0L;
      }

   }

   public ItemSequence() {
      this(crfsuiteJNI.new_ItemSequence__SWIG_0(), true);
   }

   public ItemSequence(long n) {
      this(crfsuiteJNI.new_ItemSequence__SWIG_1(n), true);
   }

   public long size() {
      return crfsuiteJNI.ItemSequence_size(this.swigCPtr, this);
   }

   public long capacity() {
      return crfsuiteJNI.ItemSequence_capacity(this.swigCPtr, this);
   }

   public void reserve(long n) {
      crfsuiteJNI.ItemSequence_reserve(this.swigCPtr, this, n);
   }

   public boolean isEmpty() {
      return crfsuiteJNI.ItemSequence_isEmpty(this.swigCPtr, this);
   }

   public void clear() {
      crfsuiteJNI.ItemSequence_clear(this.swigCPtr, this);
   }

   public void add(Item x) {
      crfsuiteJNI.ItemSequence_add(this.swigCPtr, this, Item.getCPtr(x), x);
   }

   public Item get(int i) {
      return new Item(crfsuiteJNI.ItemSequence_get(this.swigCPtr, this, i), false);
   }

   public void set(int i, Item val) {
      crfsuiteJNI.ItemSequence_set(this.swigCPtr, this, i, Item.getCPtr(val), val);
   }
}
