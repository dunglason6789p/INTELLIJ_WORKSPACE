package libsvm;

class Cache {
   private final int l;
   private long size;
   private final Cache.head_t[] head;
   private Cache.head_t lru_head;

   Cache(int l_, long size_) {
      this.l = l_;
      this.size = size_;
      this.head = new Cache.head_t[this.l];

      for(int i = 0; i < this.l; ++i) {
         this.head[i] = new Cache.head_t();
      }

      this.size /= 4L;
      this.size -= (long)(this.l * 4);
      this.size = Math.max(this.size, 2L * (long)this.l);
      this.lru_head = new Cache.head_t();
      this.lru_head.next = this.lru_head.prev = this.lru_head;
   }

   private void lru_delete(Cache.head_t h) {
      h.prev.next = h.next;
      h.next.prev = h.prev;
   }

   private void lru_insert(Cache.head_t h) {
      h.next = this.lru_head;
      h.prev = this.lru_head.prev;
      h.prev.next = h;
      h.next.prev = h;
   }

   int get_data(int index, float[][] data, int len) {
      Cache.head_t h = this.head[index];
      if (h.len > 0) {
         this.lru_delete(h);
      }

      int more = len - h.len;
      if (more > 0) {
         while(this.size < (long)more) {
            Cache.head_t old = this.lru_head.next;
            this.lru_delete(old);
            this.size += (long)old.len;
            old.data = null;
            old.len = 0;
         }

         float[] new_data = new float[len];
         if (h.data != null) {
            System.arraycopy(h.data, 0, new_data, 0, h.len);
         }

         h.data = new_data;
         this.size -= (long)more;
         int _ = h.len;
         h.len = len;
         len = _;
      }

      this.lru_insert(h);
      data[0] = h.data;
      return len;
   }

   void swap_index(int i, int j) {
      if (i != j) {
         if (this.head[i].len > 0) {
            this.lru_delete(this.head[i]);
         }

         if (this.head[j].len > 0) {
            this.lru_delete(this.head[j]);
         }

         float[] _ = this.head[i].data;
         this.head[i].data = this.head[j].data;
         this.head[j].data = _;
         int _ = this.head[i].len;
         this.head[i].len = this.head[j].len;
         this.head[j].len = _;
         if (this.head[i].len > 0) {
            this.lru_insert(this.head[i]);
         }

         if (this.head[j].len > 0) {
            this.lru_insert(this.head[j]);
         }

         if (i > j) {
            _ = i;
            i = j;
            j = _;
         }

         for(Cache.head_t h = this.lru_head.next; h != this.lru_head; h = h.next) {
            if (h.len > i) {
               if (h.len > j) {
                  float _ = h.data[i];
                  h.data[i] = h.data[j];
                  h.data[j] = _;
               } else {
                  this.lru_delete(h);
                  this.size += (long)h.len;
                  h.data = null;
                  h.len = 0;
               }
            }
         }

      }
   }

   private final class head_t {
      Cache.head_t prev;
      Cache.head_t next;
      float[] data;
      int len;

      private head_t() {
      }
   }
}
