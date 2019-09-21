package org.maltparser.core.helper;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public final class NoPrintStream extends PrintStream {
   public static final OutputStream NO_OUTPUTSTREAM;
   public static final PrintStream NO_PRINTSTREAM;

   private NoPrintStream() {
      super(NO_OUTPUTSTREAM);
   }

   public PrintStream append(char c) {
      return super.append(c);
   }

   public PrintStream append(CharSequence csq, int start, int end) {
      return super.append(csq, start, end);
   }

   public PrintStream append(CharSequence csq) {
      return super.append(csq);
   }

   public boolean checkError() {
      return super.checkError();
   }

   public void close() {
      super.close();
   }

   public void flush() {
      super.flush();
   }

   public PrintStream format(Locale l, String format, Object... args) {
      return super.format(l, format, args);
   }

   public PrintStream format(String format, Object... args) {
      return super.format(format, args);
   }

   public void print(boolean b) {
   }

   public void print(char c) {
   }

   public void print(char[] s) {
   }

   public void print(double d) {
   }

   public void print(float f) {
   }

   public void print(int i) {
   }

   public void print(long l) {
   }

   public void print(Object obj) {
   }

   public void print(String s) {
   }

   public PrintStream printf(Locale l, String format, Object... args) {
      return super.printf(l, format, args);
   }

   public PrintStream printf(String format, Object... args) {
      return super.printf(format, args);
   }

   public void println() {
   }

   public void println(boolean x) {
   }

   public void println(char x) {
   }

   public void println(char[] x) {
   }

   public void println(double x) {
   }

   public void println(float x) {
   }

   public void println(int x) {
   }

   public void println(long x) {
   }

   public void println(Object x) {
   }

   public void println(String x) {
   }

   protected void setError() {
      super.setError();
   }

   public void write(byte[] buf, int off, int len) {
   }

   public void write(int b) {
   }

   static {
      NO_OUTPUTSTREAM = NoOutputStream.DEVNULL;
      NO_PRINTSTREAM = new NoPrintStream();
   }
}
