package com.github.jcrfsuite.util;

public class Pair<T1, T2> {
   public T1 first;
   public T2 second;

   public Pair(T1 x, T2 y) {
      this.first = x;
      this.second = y;
   }

   public T1 getFirst() {
      return this.first;
   }

   public T2 getSecond() {
      return this.second;
   }

   public String toString() {
      return String.format("{%s, %s}", this.first, this.second);
   }
}
