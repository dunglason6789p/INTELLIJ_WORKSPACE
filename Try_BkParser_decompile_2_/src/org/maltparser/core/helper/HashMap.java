/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class HashMap<K, V>
implements Map<K, V>,
Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    private static final int INITIAL_TABLE_SIZE = 4;
    private static final Object NULL_KEY = new Serializable(){

        Object readResolve() {
            return NULL_KEY;
        }
    };
    transient Object[] keys;
    transient int size = 0;
    transient Object[] values;

    static Object maskNullKey(Object k) {
        return k == null ? NULL_KEY : k;
    }

    static Object unmaskNullKey(Object k) {
        return k == NULL_KEY ? null : k;
    }

    public HashMap() {
        this.initTable(4);
    }

    public HashMap(Map<? extends K, ? extends V> m) {
        int newCapacity = 4;
        int expectedSize = m.size();
        while (newCapacity * 3 < expectedSize * 4) {
            newCapacity <<= 1;
        }
        this.initTable(newCapacity);
        this.internalPutAll(m);
    }

    @Override
    public void clear() {
        this.initTable(4);
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.findKey(key) >= 0;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            for (int i = 0; i < this.keys.length; ++i) {
                if (this.keys[i] == null || this.values[i] != null) continue;
                return true;
            }
        } else {
            for (Object existing : this.values) {
                if (!this.valueEquals(existing, value)) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Map)) {
            return false;
        }
        Map other = (Map)o;
        return this.entrySet().equals(other.entrySet());
    }

    @Override
    public V get(Object key) {
        int index = this.findKey(key);
        return (V)(index < 0 ? null : this.values[index]);
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int i = 0; i < this.keys.length; ++i) {
            Object key = this.keys[i];
            if (key == null) continue;
            result += this.keyHashCode(HashMap.unmaskNullKey(key)) ^ this.valueHashCode(this.values[i]);
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public V put(K key, V value) {
        this.ensureSizeFor(this.size + 1);
        int index = this.findKeyOrEmpty(key);
        if (this.keys[index] == null) {
            ++this.size;
            this.keys[index] = HashMap.maskNullKey(key);
            this.values[index] = value;
            return null;
        }
        Object previousValue = this.values[index];
        this.values[index] = value;
        return (V)previousValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.ensureSizeFor(this.size + m.size());
        this.internalPutAll(m);
    }

    @Override
    public V remove(Object key) {
        int index = this.findKey(key);
        if (index < 0) {
            return null;
        }
        Object previousValue = this.values[index];
        this.internalRemove(index);
        return (V)previousValue;
    }

    @Override
    public int size() {
        return this.size;
    }

    public String toString() {
        if (this.size == 0) {
            return "{}";
        }
        StringBuilder buf = new StringBuilder(32 * this.size());
        buf.append('{');
        boolean needComma = false;
        for (int i = 0; i < this.keys.length; ++i) {
            Object key = this.keys[i];
            if (key == null) continue;
            if (needComma) {
                buf.append(',').append(' ');
            }
            key = HashMap.unmaskNullKey(key);
            Object value = this.values[i];
            buf.append(key == this ? "(this Map)" : key).append('=').append(value == this ? "(this Map)" : value);
            needComma = true;
        }
        buf.append('}');
        return buf.toString();
    }

    @Override
    public Collection<V> values() {
        return new Values();
    }

    protected void doReadObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int capacity = in.readInt();
        this.initTable(capacity);
        int items = in.readInt();
        for (int i = 0; i < items; ++i) {
            Object key = in.readObject();
            Object value = in.readObject();
            this.put(key, value);
        }
    }

    protected void doWriteObject(ObjectOutputStream out) throws IOException {
        out.writeInt(this.keys.length);
        out.writeInt(this.size);
        for (int i = 0; i < this.keys.length; ++i) {
            Object key = this.keys[i];
            if (key == null) continue;
            out.writeObject(HashMap.unmaskNullKey(key));
            out.writeObject(this.values[i]);
        }
    }

    protected boolean keyEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    protected int keyHashCode(Object k) {
        return k == null ? 0 : k.hashCode();
    }

    protected boolean valueEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    protected int valueHashCode(Object v) {
        return v == null ? 0 : v.hashCode();
    }

    void ensureSizeFor(int expectedSize) {
        if (this.keys.length * 3 >= expectedSize * 4) {
            return;
        }
        int newCapacity = this.keys.length << 1;
        while (newCapacity * 3 < expectedSize * 4) {
            newCapacity <<= 1;
        }
        Object[] oldKeys = this.keys;
        Object[] oldValues = this.values;
        this.initTable(newCapacity);
        for (int i = 0; i < oldKeys.length; ++i) {
            Object k = oldKeys[i];
            if (k == null) continue;
            int newIndex = this.getKeyIndex(HashMap.unmaskNullKey(k));
            while (this.keys[newIndex] != null) {
                if (++newIndex != this.keys.length) continue;
                newIndex = 0;
            }
            this.keys[newIndex] = k;
            this.values[newIndex] = oldValues[i];
        }
    }

    int findKey(Object k) {
        int index = this.getKeyIndex(k);
        Object existing;
        while ((existing = this.keys[index]) != null) {
            if (this.keyEquals(k, HashMap.unmaskNullKey(existing))) {
                return index;
            }
            if (++index != this.keys.length) continue;
            index = 0;
        }
        return -1;
    }

    int findKeyOrEmpty(Object k) {
        int index = this.getKeyIndex(k);
        Object existing;
        while ((existing = this.keys[index]) != null) {
            if (this.keyEquals(k, HashMap.unmaskNullKey(existing))) {
                return index;
            }
            if (++index != this.keys.length) continue;
            index = 0;
        }
        return index;
    }

    void internalRemove(int index) {
        this.keys[index] = null;
        this.values[index] = null;
        --this.size;
        this.plugHole(index);
    }

    private int getKeyIndex(Object k) {
        int h = this.keyHashCode(k);
        h += ~(h << 9);
        h ^= h >>> 14;
        h += h << 4;
        h ^= h >>> 10;
        return h & this.keys.length - 1;
    }

    private void initTable(int capacity) {
        this.keys = new Object[capacity];
        this.values = new Object[capacity];
    }

    private void internalPutAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<K, V> entry : m.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            int index = this.findKeyOrEmpty(key);
            if (this.keys[index] == null) {
                ++this.size;
                this.keys[index] = HashMap.maskNullKey(key);
                this.values[index] = value;
                continue;
            }
            this.values[index] = value;
        }
    }

    private void plugHole(int hole) {
        int index = hole + 1;
        if (index == this.keys.length) {
            index = 0;
        }
        while (this.keys[index] != null) {
            int targetIndex = this.getKeyIndex(HashMap.unmaskNullKey(this.keys[index]));
            if (hole < index) {
                if (hole >= targetIndex || targetIndex > index) {
                    this.keys[hole] = this.keys[index];
                    this.values[hole] = this.values[index];
                    this.keys[index] = null;
                    this.values[index] = null;
                    hole = index;
                }
            } else if (index < targetIndex && targetIndex <= hole) {
                this.keys[hole] = this.keys[index];
                this.values[hole] = this.values[index];
                this.keys[index] = null;
                this.values[index] = null;
                hole = index;
            }
            if (++index != this.keys.length) continue;
            index = 0;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.doReadObject(in);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        this.doWriteObject(out);
    }

    private class Values
    extends AbstractCollection<V> {
        private Values() {
        }

        @Override
        public void clear() {
            HashMap.this.clear();
        }

        @Override
        public boolean contains(Object o) {
            return HashMap.this.containsValue(o);
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (int i = 0; i < HashMap.this.keys.length; ++i) {
                if (HashMap.this.keys[i] == null) continue;
                result += HashMap.this.valueHashCode(HashMap.this.values[i]);
            }
            return result;
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public boolean remove(Object o) {
            if (o == null) {
                for (int i = 0; i < HashMap.this.keys.length; ++i) {
                    if (HashMap.this.keys[i] == null || HashMap.this.values[i] != null) continue;
                    HashMap.this.internalRemove(i);
                    return true;
                }
            } else {
                for (int i = 0; i < HashMap.this.keys.length; ++i) {
                    if (!HashMap.this.valueEquals(HashMap.this.values[i], o)) continue;
                    HashMap.this.internalRemove(i);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean didRemove = false;
            for (Object o : c) {
                didRemove |= this.remove(o);
            }
            return didRemove;
        }

        @Override
        public int size() {
            return HashMap.this.size;
        }
    }

    private class ValueIterator
    implements Iterator<V> {
        private int index = 0;
        private int last = -1;

        private ValueIterator() {
            this.advanceToItem();
        }

        @Override
        public boolean hasNext() {
            return this.index < HashMap.this.keys.length;
        }

        @Override
        public V next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.last = this.index;
            Object toReturn = HashMap.this.values[this.index++];
            this.advanceToItem();
            return (V)toReturn;
        }

        @Override
        public void remove() {
            if (this.last < 0) {
                throw new IllegalStateException();
            }
            HashMap.this.internalRemove(this.last);
            if (HashMap.this.keys[this.last] != null) {
                this.index = this.last;
            }
            this.last = -1;
        }

        private void advanceToItem() {
            while (this.index < HashMap.this.keys.length) {
                if (HashMap.this.keys[this.index] != null) {
                    return;
                }
                ++this.index;
            }
        }
    }

    private class KeySet
    extends AbstractSet<K> {
        private KeySet() {
        }

        @Override
        public void clear() {
            HashMap.this.clear();
        }

        @Override
        public boolean contains(Object o) {
            return HashMap.this.containsKey(o);
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (int i = 0; i < HashMap.this.keys.length; ++i) {
                Object key = HashMap.this.keys[i];
                if (key == null) continue;
                result += HashMap.this.keyHashCode(HashMap.unmaskNullKey(key));
            }
            return result;
        }

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public boolean remove(Object o) {
            int index = HashMap.this.findKey(o);
            if (index >= 0) {
                HashMap.this.internalRemove(index);
                return true;
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean didRemove = false;
            for (Object o : c) {
                didRemove |= this.remove(o);
            }
            return didRemove;
        }

        @Override
        public int size() {
            return HashMap.this.size;
        }
    }

    private class KeyIterator
    implements Iterator<K> {
        private int index = 0;
        private int last = -1;

        private KeyIterator() {
            this.advanceToItem();
        }

        @Override
        public boolean hasNext() {
            return this.index < HashMap.this.keys.length;
        }

        @Override
        public K next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.last = this.index;
            Object toReturn = HashMap.unmaskNullKey(HashMap.this.keys[this.index++]);
            this.advanceToItem();
            return (K)toReturn;
        }

        @Override
        public void remove() {
            if (this.last < 0) {
                throw new IllegalStateException();
            }
            HashMap.this.internalRemove(this.last);
            if (HashMap.this.keys[this.last] != null) {
                this.index = this.last;
            }
            this.last = -1;
        }

        private void advanceToItem() {
            while (this.index < HashMap.this.keys.length) {
                if (HashMap.this.keys[this.index] != null) {
                    return;
                }
                ++this.index;
            }
        }
    }

    private class HashEntry
    implements Map.Entry<K, V> {
        private final int index;

        public HashEntry(int index) {
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)o;
            return HashMap.this.keyEquals(this.getKey(), entry.getKey()) && HashMap.this.valueEquals(this.getValue(), entry.getValue());
        }

        @Override
        public K getKey() {
            return (K)HashMap.unmaskNullKey(HashMap.this.keys[this.index]);
        }

        @Override
        public V getValue() {
            return (V)HashMap.this.values[this.index];
        }

        @Override
        public int hashCode() {
            return HashMap.this.keyHashCode(this.getKey()) ^ HashMap.this.valueHashCode(this.getValue());
        }

        @Override
        public V setValue(V value) {
            Object previous = HashMap.this.values[this.index];
            HashMap.this.values[this.index] = value;
            return (V)previous;
        }

        public String toString() {
            return this.getKey() + "=" + this.getValue();
        }
    }

    private class EntrySet
    extends AbstractSet<Map.Entry<K, V>> {
        private EntrySet() {
        }

        @Override
        public boolean add(Map.Entry<K, V> entry) {
            boolean result = !HashMap.this.containsKey(entry.getKey());
            HashMap.this.put(entry.getKey(), entry.getValue());
            return result;
        }

        @Override
        public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
            HashMap.this.ensureSizeFor(this.size() + c.size());
            return super.addAll(c);
        }

        @Override
        public void clear() {
            HashMap.this.clear();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)o;
            Object value = HashMap.this.get(entry.getKey());
            return HashMap.this.valueEquals(value, entry.getValue());
        }

        @Override
        public int hashCode() {
            return HashMap.this.hashCode();
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)o;
            int index = HashMap.this.findKey(entry.getKey());
            if (index >= 0 && HashMap.this.valueEquals(HashMap.this.values[index], entry.getValue())) {
                HashMap.this.internalRemove(index);
                return true;
            }
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean didRemove = false;
            for (Object o : c) {
                didRemove |= this.remove(o);
            }
            return didRemove;
        }

        @Override
        public int size() {
            return HashMap.this.size;
        }
    }

    private class EntryIterator
    implements Iterator<Map.Entry<K, V>> {
        private int index = 0;
        private int last = -1;

        private EntryIterator() {
            this.advanceToItem();
        }

        @Override
        public boolean hasNext() {
            return this.index < HashMap.this.keys.length;
        }

        @Override
        public Map.Entry<K, V> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.last = this.index;
            HashEntry toReturn = new HashEntry(this.index++);
            this.advanceToItem();
            return toReturn;
        }

        @Override
        public void remove() {
            if (this.last < 0) {
                throw new IllegalStateException();
            }
            HashMap.this.internalRemove(this.last);
            if (HashMap.this.keys[this.last] != null) {
                this.index = this.last;
            }
            this.last = -1;
        }

        private void advanceToItem() {
            while (this.index < HashMap.this.keys.length) {
                if (HashMap.this.keys[this.index] != null) {
                    return;
                }
                ++this.index;
            }
        }
    }

}

