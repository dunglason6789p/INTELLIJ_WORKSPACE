package org.maltparser.core.pool;

import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashSet;

public abstract class ObjectPoolSet<T> extends ObjectPool<T> {
   private final HashSet<T> available;
   private final HashSet<T> inuse;

   public ObjectPoolSet() {
      this(2147483647);
   }

   public ObjectPoolSet(int keepThreshold) {
      super(keepThreshold);
      this.available = new HashSet();
      this.inuse = new HashSet();
   }

   protected abstract T create() throws MaltChainedException;

   public abstract void resetObject(T var1) throws MaltChainedException;

   public synchronized T checkOut() throws MaltChainedException {
      if (this.available.isEmpty()) {
         T t = this.create();
         this.inuse.add(t);
         return t;
      } else {
         Iterator i$ = this.available.iterator();
         if (i$.hasNext()) {
            T t = i$.next();
            this.inuse.add(t);
            this.available.remove(t);
            return t;
         } else {
            return null;
         }
      }
   }

   public synchronized void checkIn(T t) throws MaltChainedException {
      this.resetObject(t);
      this.inuse.remove(t);
      if (this.available.size() < this.keepThreshold) {
         this.available.add(t);
      }

   }

   public synchronized void checkInAll() throws MaltChainedException {
      Iterator i$ = this.inuse.iterator();

      while(i$.hasNext()) {
         T t = i$.next();
         this.resetObject(t);
         if (this.available.size() < this.keepThreshold) {
            this.available.add(t);
         }
      }

      this.inuse.clear();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.inuse.iterator();

      while(i$.hasNext()) {
         T t = i$.next();
         sb.append(t);
         sb.append(", ");
      }

      return sb.toString();
   }
}
