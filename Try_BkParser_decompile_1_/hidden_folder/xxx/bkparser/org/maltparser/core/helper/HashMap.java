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
import java.util.Map.Entry;

public class HashMap<K, V> implements Map<K, V>, Serializable {
   private static final long serialVersionUID = 7526471155622776147L;
   private static final int INITIAL_TABLE_SIZE = 4;
   private static final Object NULL_KEY = new Serializable() {
      Object readResolve() {
         return HashMap.NULL_KEY;
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

      for(int expectedSize = m.size(); newCapacity * 3 < expectedSize * 4; newCapacity <<= 1) {
      }

      this.initTable(newCapacity);
      this.internalPutAll(m);
   }

   public void clear() {
      this.initTable(4);
      this.size = 0;
   }

   public boolean containsKey(Object key) {
      return this.findKey(key) >= 0;
   }

   public boolean containsValue(Object value) {
      if (value == null) {
         for(int i = 0; i < this.keys.length; ++i) {
            if (this.keys[i] != null && this.values[i] == null) {
               return true;
            }
         }
      } else {
         Object[] arr$ = this.values;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Object existing = arr$[i$];
            if (this.valueEquals(existing, value)) {
               return true;
            }
         }
      }

      return false;
   }

   public Set<Entry<K, V>> entrySet() {
      return new HashMap.EntrySet();
   }

   public boolean equals(Object o) {
      if (!(o instanceof Map)) {
         return false;
      } else {
         Map<K, V> other = (Map)o;
         return this.entrySet().equals(other.entrySet());
      }
   }

   public V get(Object key) {
      int index = this.findKey(key);
      return index < 0 ? null : this.values[index];
   }

   public int hashCode() {
      int result = 0;

      for(int i = 0; i < this.keys.length; ++i) {
         Object key = this.keys[i];
         if (key != null) {
            result += this.keyHashCode(unmaskNullKey(key)) ^ this.valueHashCode(this.values[i]);
         }
      }

      return result;
   }

   public boolean isEmpty() {
      return this.size == 0;
   }

   public Set<K> keySet() {
      return new HashMap.KeySet();
   }

   public V put(K key, V value) {
      this.ensureSizeFor(this.size + 1);
      int index = this.findKeyOrEmpty(key);
      if (this.keys[index] == null) {
         ++this.size;
         this.keys[index] = maskNullKey(key);
         this.values[index] = value;
         return null;
      } else {
         Object previousValue = this.values[index];
         this.values[index] = value;
         return previousValue;
      }
   }

   public void putAll(Map<? extends K, ? extends V> m) {
      this.ensureSizeFor(this.size + m.size());
      this.internalPutAll(m);
   }

   public V remove(Object key) {
      int index = this.findKey(key);
      if (index < 0) {
         return null;
      } else {
         Object previousValue = this.values[index];
         this.internalRemove(index);
         return previousValue;
      }
   }

   public int size() {
      return this.size;
   }

   public String toString() {
      if (this.size == 0) {
         return "{}";
      } else {
         StringBuilder buf = new StringBuilder(32 * this.size());
         buf.append('{');
         boolean needComma = false;

         for(int i = 0; i < this.keys.length; ++i) {
            Object key = this.keys[i];
            if (key != null) {
               if (needComma) {
                  buf.append(',').append(' ');
               }

               key = unmaskNullKey(key);
               Object value = this.values[i];
               buf.append(key == this ? "(this Map)" : key).append('=').append(value == this ? "(this Map)" : value);
               needComma = true;
            }
         }

         buf.append('}');
         return buf.toString();
      }
   }

   public Collection<V> values() {
      return new HashMap.Values();
   }

   protected void doReadObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int capacity = in.readInt();
      this.initTable(capacity);
      int items = in.readInt();

      for(int i = 0; i < items; ++i) {
         Object key = in.readObject();
         Object value = in.readObject();
         this.put(key, value);
      }

   }

   protected void doWriteObject(ObjectOutputStream out) throws IOException {
      out.writeInt(this.keys.length);
      out.writeInt(this.size);

      for(int i = 0; i < this.keys.length; ++i) {
         Object key = this.keys[i];
         if (key != null) {
            out.writeObject(unmaskNullKey(key));
            out.writeObject(this.values[i]);
         }
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
      if (this.keys.length * 3 < expectedSize * 4) {
         int newCapacity;
         for(newCapacity = this.keys.length << 1; newCapacity * 3 < expectedSize * 4; newCapacity <<= 1) {
         }

         Object[] oldKeys = this.keys;
         Object[] oldValues = this.values;
         this.initTable(newCapacity);

         for(int i = 0; i < oldKeys.length; ++i) {
            Object k = oldKeys[i];
            if (k != null) {
               int newIndex = this.getKeyIndex(unmaskNullKey(k));

               while(this.keys[newIndex] != null) {
                  ++newIndex;
                  if (newIndex == this.keys.length) {
                     newIndex = 0;
                  }
               }

               this.keys[newIndex] = k;
               this.values[newIndex] = oldValues[i];
            }
         }

      }
   }

   int findKey(Object k) {
      int index = this.getKeyIndex(k);

      while(true) {
         Object existing = this.keys[index];
         if (existing == null) {
            return -1;
         }

         if (this.keyEquals(k, unmaskNullKey(existing))) {
            return index;
         }

         ++index;
         if (index == this.keys.length) {
            index = 0;
         }
      }
   }

   int findKeyOrEmpty(Object k) {
      int index = this.getKeyIndex(k);

      while(true) {
         Object existing = this.keys[index];
         if (existing == null) {
            return index;
         }

         if (this.keyEquals(k, unmaskNullKey(existing))) {
            return index;
         }

         ++index;
         if (index == this.keys.length) {
            index = 0;
         }
      }
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
      Iterator i$ = m.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<? extends K, ? extends V> entry = (Entry)i$.next();
         K key = entry.getKey();
         V value = entry.getValue();
         int index = this.findKeyOrEmpty(key);
         if (this.keys[index] == null) {
            ++this.size;
            this.keys[index] = maskNullKey(key);
            this.values[index] = value;
         } else {
            this.values[index] = value;
         }
      }

   }

   private void plugHole(int hole) {
      int index = hole + 1;
      if (index == this.keys.length) {
         index = 0;
      }

      while(this.keys[index] != null) {
         int targetIndex = this.getKeyIndex(unmaskNullKey(this.keys[index]));
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

         ++index;
         if (index == this.keys.length) {
            index = 0;
         }
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

   private class Values extends AbstractCollection<V> {
      private Values() {
      }

      public void clear() {
         HashMap.this.clear();
      }

      public boolean contains(Object o) {
         return HashMap.this.containsValue(o);
      }

      public int hashCode() {
         int result = 0;

         for(int i = 0; i < HashMap.this.keys.length; ++i) {
            if (HashMap.this.keys[i] != null) {
               result += HashMap.this.valueHashCode(HashMap.this.values[i]);
            }
         }

         return result;
      }

      public Iterator<V> iterator() {
         return HashMap.this.new ValueIterator();
      }

      public boolean remove(Object o) {
         int i;
         if (o == null) {
            for(i = 0; i < HashMap.this.keys.length; ++i) {
               if (HashMap.this.keys[i] != null && HashMap.this.values[i] == null) {
                  HashMap.this.internalRemove(i);
                  return true;
               }
            }
         } else {
            for(i = 0; i < HashMap.this.keys.length; ++i) {
               if (HashMap.this.valueEquals(HashMap.this.values[i], o)) {
                  HashMap.this.internalRemove(i);
                  return true;
               }
            }
         }

         return false;
      }

      public boolean removeAll(Collection<?> c) {
         boolean didRemove = false;

         Object o;
         for(Iterator i$ = c.iterator(); i$.hasNext(); didRemove |= this.remove(o)) {
            o = i$.next();
         }

         return didRemove;
      }

      public int size() {
         return HashMap.this.size;
      }
   }

   private class ValueIterator implements Iterator<V> {
      private int index;
      private int last;

      private ValueIterator() {
         this.index = 0;
         this.last = -1;
         this.advanceToItem();
      }

      public boolean hasNext() {
         return this.index < HashMap.this.keys.length;
      }

      public V next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.last = this.index;
            Object toReturn = HashMap.this.values[this.index++];
            this.advanceToItem();
            return toReturn;
         }
      }

      public void remove() {
         if (this.last < 0) {
            throw new IllegalStateException();
         } else {
            HashMap.this.internalRemove(this.last);
            if (HashMap.this.keys[this.last] != null) {
               this.index = this.last;
            }

            this.last = -1;
         }
      }

      private void advanceToItem() {
         while(this.index < HashMap.this.keys.length) {
            if (HashMap.this.keys[this.index] != null) {
               return;
            }

            ++this.index;
         }

      }
   }

   private class KeySet extends AbstractSet<K> {
      private KeySet() {
      }

      public void clear() {
         HashMap.this.clear();
      }

      public boolean contains(Object o) {
         return HashMap.this.containsKey(o);
      }

      public int hashCode() {
         int result = 0;

         for(int i = 0; i < HashMap.this.keys.length; ++i) {
            Object key = HashMap.this.keys[i];
            if (key != null) {
               result += HashMap.this.keyHashCode(HashMap.unmaskNullKey(key));
            }
         }

         return result;
      }

      public Iterator<K> iterator() {
         return HashMap.this.new KeyIterator();
      }

      public boolean remove(Object o) {
         int index = HashMap.this.findKey(o);
         if (index >= 0) {
            HashMap.this.internalRemove(index);
            return true;
         } else {
            return false;
         }
      }

      public boolean removeAll(Collection<?> c) {
         boolean didRemove = false;

         Object o;
         for(Iterator i$ = c.iterator(); i$.hasNext(); didRemove |= this.remove(o)) {
            o = i$.next();
         }

         return didRemove;
      }

      public int size() {
         return HashMap.this.size;
      }
   }

   private class KeyIterator implements Iterator<K> {
      private int index;
      private int last;

      private KeyIterator() {
         this.index = 0;
         this.last = -1;
         this.advanceToItem();
      }

      public boolean hasNext() {
         return this.index < HashMap.this.keys.length;
      }

      public K next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.last = this.index;
            Object toReturn = HashMap.unmaskNullKey(HashMap.this.keys[this.index++]);
            this.advanceToItem();
            return toReturn;
         }
      }

      public void remove() {
         if (this.last < 0) {
            throw new IllegalStateException();
         } else {
            HashMap.this.internalRemove(this.last);
            if (HashMap.this.keys[this.last] != null) {
               this.index = this.last;
            }

            this.last = -1;
         }
      }

      private void advanceToItem() {
         while(this.index < HashMap.this.keys.length) {
            if (HashMap.this.keys[this.index] != null) {
               return;
            }

            ++this.index;
         }

      }
   }

   private class HashEntry implements Entry<K, V> {
      private final int index;

      public HashEntry(int index) {
         this.index = index;
      }

      public boolean equals(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<K, V> entry = (Entry)o;
            return HashMap.this.keyEquals(this.getKey(), entry.getKey()) && HashMap.this.valueEquals(this.getValue(), entry.getValue());
         }
      }

      public K getKey() {
         return HashMap.unmaskNullKey(HashMap.this.keys[this.index]);
      }

      public V getValue() {
         return HashMap.this.values[this.index];
      }

      public int hashCode() {
         return HashMap.this.keyHashCode(this.getKey()) ^ HashMap.this.valueHashCode(this.getValue());
      }

      public V setValue(V value) {
         V previous = HashMap.this.values[this.index];
         HashMap.this.values[this.index] = value;
         return previous;
      }

      public String toString() {
         return this.getKey() + "=" + this.getValue();
      }
   }

   private class EntrySet extends AbstractSet<Entry<K, V>> {
      private EntrySet() {
      }

      public boolean add(Entry<K, V> entry) {
         boolean result = !HashMap.this.containsKey(entry.getKey());
         HashMap.this.put(entry.getKey(), entry.getValue());
         return result;
      }

      public boolean addAll(Collection<? extends Entry<K, V>> c) {
         HashMap.this.ensureSizeFor(this.size() + c.size());
         return super.addAll(c);
      }

      public void clear() {
         HashMap.this.clear();
      }

      public boolean contains(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<K, V> entry = (Entry)o;
            V value = HashMap.this.get(entry.getKey());
            return HashMap.this.valueEquals(value, entry.getValue());
         }
      }

      public int hashCode() {
         return HashMap.this.hashCode();
      }

      public Iterator<Entry<K, V>> iterator() {
         return HashMap.this.new EntryIterator();
      }

      public boolean remove(Object o) {
         if (!(o instanceof Entry)) {
            return false;
         } else {
            Entry<K, V> entry = (Entry)o;
            int index = HashMap.this.findKey(entry.getKey());
            if (index >= 0 && HashMap.this.valueEquals(HashMap.this.values[index], entry.getValue())) {
               HashMap.this.internalRemove(index);
               return true;
            } else {
               return false;
            }
         }
      }

      public boolean removeAll(Collection<?> c) {
         boolean didRemove = false;

         Object o;
         for(Iterator i$ = c.iterator(); i$.hasNext(); didRemove |= this.remove(o)) {
            o = i$.next();
         }

         return didRemove;
      }

      public int size() {
         return HashMap.this.size;
      }
   }

   private class EntryIterator implements Iterator<Entry<K, V>> {
      private int index;
      private int last;

      private EntryIterator() {
         this.index = 0;
         this.last = -1;
         this.advanceToItem();
      }

      public boolean hasNext() {
         return this.index < HashMap.this.keys.length;
      }

      public Entry<K, V> next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.last = this.index;
            Entry<K, V> toReturn = HashMap.this.new HashEntry(this.index++);
            this.advanceToItem();
            return toReturn;
         }
      }

      public void remove() {
         if (this.last < 0) {
            throw new IllegalStateException();
         } else {
            HashMap.this.internalRemove(this.last);
            if (HashMap.this.keys[this.last] != null) {
               this.index = this.last;
            }

            this.last = -1;
         }
      }

      private void advanceToItem() {
         while(this.index < HashMap.this.keys.length) {
            if (HashMap.this.keys[this.index] != null) {
               return;
            }

            ++this.index;
         }

      }
   }
}
