package org.apache.log4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class Level extends Priority implements Serializable {
   public static final int TRACE_INT = 5000;
   public static final Level OFF = new Level(2147483647, "OFF", 0);
   public static final Level FATAL = new Level(50000, "FATAL", 0);
   public static final Level ERROR = new Level(40000, "ERROR", 3);
   public static final Level WARN = new Level(30000, "WARN", 4);
   public static final Level INFO = new Level(20000, "INFO", 6);
   public static final Level DEBUG = new Level(10000, "DEBUG", 7);
   public static final Level TRACE = new Level(5000, "TRACE", 7);
   public static final Level ALL = new Level(-2147483648, "ALL", 7);
   static final long serialVersionUID = 3491141966387921974L;

   protected Level(int level, String levelStr, int syslogEquivalent) {
      super(level, levelStr, syslogEquivalent);
   }

   public static Level toLevel(String sArg) {
      return toLevel(sArg, DEBUG);
   }

   public static Level toLevel(int val) {
      return toLevel(val, DEBUG);
   }

   public static Level toLevel(int val, Level defaultLevel) {
      switch(val) {
      case -2147483648:
         return ALL;
      case 5000:
         return TRACE;
      case 10000:
         return DEBUG;
      case 20000:
         return INFO;
      case 30000:
         return WARN;
      case 40000:
         return ERROR;
      case 50000:
         return FATAL;
      case 2147483647:
         return OFF;
      default:
         return defaultLevel;
      }
   }

   public static Level toLevel(String sArg, Level defaultLevel) {
      if (sArg == null) {
         return defaultLevel;
      } else {
         String s = sArg.toUpperCase();
         if (s.equals("ALL")) {
            return ALL;
         } else if (s.equals("DEBUG")) {
            return DEBUG;
         } else if (s.equals("INFO")) {
            return INFO;
         } else if (s.equals("WARN")) {
            return WARN;
         } else if (s.equals("ERROR")) {
            return ERROR;
         } else if (s.equals("FATAL")) {
            return FATAL;
         } else if (s.equals("OFF")) {
            return OFF;
         } else if (s.equals("TRACE")) {
            return TRACE;
         } else {
            return s.equals("İNFO") ? INFO : defaultLevel;
         }
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      super.level = s.readInt();
      super.syslogEquivalent = s.readInt();
      super.levelStr = s.readUTF();
      if (super.levelStr == null) {
         super.levelStr = "";
      }

   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      s.writeInt(super.level);
      s.writeInt(super.syslogEquivalent);
      s.writeUTF(super.levelStr);
   }

   private Object readResolve() throws ObjectStreamException {
      return this.getClass() == (class$org$apache$log4j$Level == null ? (class$org$apache$log4j$Level = class$("org.apache.log4j.Level")) : class$org$apache$log4j$Level) ? toLevel(super.level) : this;
   }
}
