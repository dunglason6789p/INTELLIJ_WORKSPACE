/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

class Cache {
    private final int l;
    private long size;
    private final head_t[] head;
    private head_t lru_head;

    Cache(int l_, long size_) {
        this.l = l_;
        this.size = size_;
        this.head = new head_t[this.l];
        for (int i = 0; i < this.l; ++i) {
            this.head[i] = new head_t();
        }
        this.size /= 4L;
        this.size -= (long)(this.l * 4);
        this.size = Math.max(this.size, 2L * (long)this.l);
        this.lru_head.next = this.lru_head.prev = (this.lru_head = new head_t());
    }

    private void lru_delete(head_t h) {
        h.prev.next = h.next;
        h.next.prev = h.prev;
    }

    private void lru_insert(head_t h) {
        h.next = this.lru_head;
        h.prev = this.lru_head.prev;
        h.prev.next = h;
        h.next.prev = h;
    }

    int get_data(int index, float[][] data, int len) {
        int more;
        head_t h = this.head[index];
        if (h.len > 0) {
            this.lru_delete(h);
        }
        if ((more = len - h.len) > 0) {
            while (this.size < (long)more) {
                head_t old = this.lru_head.next;
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
        if (i == j) {
            return;
        }
        if (this.head[i].len > 0) {
            this.lru_delete(this.head[i]);
        }
        if (this.head[j].len > 0) {
            this.lru_delete(this.head[j]);
        }
        float[] _ = this.head[i].data;
        this.head[i].data = this.head[j].data;
        this.head[j].data = _;
        int _2 = this.head[i].len;
        this.head[i].len = this.head[j].len;
        this.head[j].len = _2;
        if (this.head[i].len > 0) {
            this.lru_insert(this.head[i]);
        }
        if (this.head[j].len > 0) {
            this.lru_insert(this.head[j]);
        }
        if (i > j) {
            _2 = i;
            i = j;
            j = _2;
        }
        head_t h = this.lru_head.next;
        while (h != this.lru_head) {
            if (h.len > i) {
                if (h.len > j) {
                    float _3 = h.data[i];
                    h.data[i] = h.data[j];
                    h.data[j] = _3;
                } else {
                    this.lru_delete(h);
                    this.size += (long)h.len;
                    h.data = null;
                    h.len = 0;
                }
            }
            h = h.next;
        }
    }

    private final class head_t {
        head_t prev;
        head_t next;
        float[] data;
        int len;

        private head_t() {
        }
    }

}

