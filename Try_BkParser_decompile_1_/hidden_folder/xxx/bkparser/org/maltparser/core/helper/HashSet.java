package org.maltparser.core.helper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class HashSet<E> extends AbstractSet<E> implements Serializable {
   private static final long serialVersionUID = 7526471155622776147L;
   private static final int INITIAL_TABLE_SIZE = 4;
   private static final Object NULL_ITEM = new Serializable() {
      Object readResolve() {
         return HashSet.NULL_ITEM;
      }
   };
   transient int size = 0;
   transient Object[] table;

   static Object maskNull(Object o) {
      return o == null ? NULL_ITEM : o;
   }

   static Object unmaskNull(Object o) {
      return o == NULL_ITEM ? null : o;
   }

   public HashSet() {
      this.table = new Object[4];
   }

   public HashSet(Collection<? extends E> c) {
      int newCapacity = 4;

      for(int expectedSize = c.size(); newCapacity * 3 < expectedSize * 4; newCapacity <<= 1) {
      }

      this.table = new Object[newCapacity];
      super.addAll(c);
   }

   public boolean add(E e) {
      this.ensureSizeFor(this.size + 1);
      int index = this.findOrEmpty(e);
      if (this.table[index] == null) {
         ++this.size;
         this.table[index] = maskNull(e);
         return true;
      } else {
         return false;
      }
   }

   public boolean addAll(Collection<? extends E> c) {
      this.ensureSizeFor(this.size + c.size());
      return super.addAll(c);
   }

   public void clear() {
      this.table = new Object[4];
      this.size = 0;
   }

   public boolean contains(Object o) {
      return this.find(o) >= 0;
   }

   public Iterator<E> iterator() {
      return new HashSet.SetIterator();
   }

   public boolean remove(Object o) {
      int index = this.find(o);
      if (index < 0) {
         return false;
      } else {
         this.internalRemove(index);
         return true;
      }
   }

   public int size() {
      return this.size;
   }

   public Object[] toArray() {
      return this.toArray(new Object[this.size]);
   }

   public <T> T[] toArray(T[] a) {
      if (a.length < this.size) {
         a = (Object[])((Object[])Array.newInstance(a.getClass().getComponentType(), this.size));
      }

      int index = 0;

      for(int i = 0; i < this.table.length; ++i) {
         Object e = this.table[i];
         if (e != null) {
            a[index++] = unmaskNull(e);
         }
      }

      while(index < a.length) {
         a[index++] = null;
      }

      return a;
   }

   protected void doReadObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      this.table = new Object[in.readInt()];
      int items = in.readInt();

      for(int i = 0; i < items; ++i) {
         this.add(in.readObject());
      }

   }

   protected void doWriteObject(ObjectOutputStream out) throws IOException {
      out.writeInt(this.table.length);
      out.writeInt(this.size);

      for(int i = 0; i < this.table.length; ++i) {
         Object e = this.table[i];
         if (e != null) {
            out.writeObject(unmaskNull(e));
         }
      }

   }

   protected boolean itemEquals(Object a, Object b) {
      return a == null ? b == null : a.equals(b);
   }

   protected int itemHashCode(Object o) {
      return o == null ? 0 : o.hashCode();
   }

   void addAll(E[] elements) {
      this.ensureSizeFor(this.size + elements.length);
      Object[] arr$ = elements;
      int len$ = elements.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         E e = arr$[i$];
         int index = this.findOrEmpty(e);
         if (this.table[index] == null) {
            ++this.size;
            this.table[index] = maskNull(e);
         }
      }

   }

   void internalRemove(int index) {
      this.table[index] = null;
      --this.size;
      this.plugHole(index);
   }

   private void ensureSizeFor(int expectedSize) {
      if (this.table.length * 3 < expectedSize * 4) {
         int newCapacity;
         for(newCapacity = this.table.length << 1; newCapacity * 3 < expectedSize * 4; newCapacity <<= 1) {
         }

         Object[] oldTable = this.table;
         this.table = new Object[newCapacity];
         Object[] arr$ = oldTable;
         int len$ = oldTable.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Object o = arr$[i$];
            if (o != null) {
               int newIndex = this.getIndex(unmaskNull(o));

               while(this.table[newIndex] != null) {
                  ++newIndex;
                  if (newIndex == this.table.length) {
                     newIndex = 0;
                  }
               }

               this.table[newIndex] = o;
            }
         }

      }
   }

   private int find(Object o) {
      int index = this.getIndex(o);

      while(true) {
         Object existing = this.table[index];
         if (existing == null) {
            return -1;
         }

         if (this.itemEquals(o, unmaskNull(existing))) {
            return index;
         }

         ++index;
         if (index == this.table.length) {
            index = 0;
         }
      }
   }

   private int findOrEmpty(Object o) {
      int index = this.getIndex(o);

      while(true) {
         Object existing = this.table[index];
         if (existing == null) {
            return index;
         }

         if (this.itemEquals(o, unmaskNull(existing))) {
            return index;
         }

         ++index;
         if (index == this.table.length) {
            index = 0;
         }
      }
   }

   private int getIndex(Object o) {
      int h = this.itemHashCode(o);
      h += ~(h << 9);
      h ^= h >>> 14;
      h += h << 4;
      h ^= h >>> 10;
      return h & this.table.length - 1;
   }

   private void plugHole(int hole) {
      int index = hole + 1;
      if (index == this.table.length) {
         index = 0;
      }

      while(this.table[index] != null) {
         int targetIndex = this.getIndex(unmaskNull(this.table[index]));
         if (hole < index) {
            if (hole >= targetIndex || targetIndex > index) {
               this.table[hole] = this.table[index];
               this.table[index] = null;
               hole = index;
            }
         } else if (index < targetIndex && targetIndex <= hole) {
            this.table[hole] = this.table[index];
            this.table[index] = null;
            hole = index;
         }

         ++index;
         if (index == this.table.length) {
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

   private class SetIterator implements Iterator<E> {
      private int index = 0;
      private int last = -1;

      public SetIterator() {
         this.advanceToItem();
      }

      public boolean hasNext() {
         return this.index < HashSet.this.table.length;
      }

      public E next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.last = this.index;
            E toReturn = HashSet.unmaskNull(HashSet.this.table[this.index++]);
            this.advanceToItem();
            return toReturn;
         }
      }

      public void remove() {
         if (this.last < 0) {
            throw new IllegalStateException();
         } else {
            HashSet.this.internalRemove(this.last);
            if (HashSet.this.table[this.last] != null) {
               this.index = this.last;
            }

            this.last = -1;
         }
      }

      private void advanceToItem() {
         while(this.index < HashSet.this.table.length) {
            if (HashSet.this.table[this.index] != null) {
               return;
            }

            ++this.index;
         }

      }
   }
}
