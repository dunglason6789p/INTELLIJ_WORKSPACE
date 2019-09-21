package org.apache.log4j.pattern;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.LogLog;

public final class PatternParser {
   private static final char ESCAPE_CHAR = '%';
   private static final int LITERAL_STATE = 0;
   private static final int CONVERTER_STATE = 1;
   private static final int DOT_STATE = 3;
   private static final int MIN_STATE = 4;
   private static final int MAX_STATE = 5;
   private static final Map PATTERN_LAYOUT_RULES;
   private static final Map FILENAME_PATTERN_RULES;

   private PatternParser() {
   }

   public static Map getPatternLayoutRules() {
      return PATTERN_LAYOUT_RULES;
   }

   public static Map getFileNamePatternRules() {
      return FILENAME_PATTERN_RULES;
   }

   private static int extractConverter(char lastChar, String pattern, int i, StringBuffer convBuf, StringBuffer currentLiteral) {
      convBuf.setLength(0);
      if (!Character.isUnicodeIdentifierStart(lastChar)) {
         return i;
      } else {
         convBuf.append(lastChar);

         while(i < pattern.length() && Character.isUnicodeIdentifierPart(pattern.charAt(i))) {
            convBuf.append(pattern.charAt(i));
            currentLiteral.append(pattern.charAt(i));
            ++i;
         }

         return i;
      }
   }

   private static int extractOptions(String pattern, int i, List options) {
      while(true) {
         if (i < pattern.length() && pattern.charAt(i) == '{') {
            int end = pattern.indexOf(125, i);
            if (end != -1) {
               String r = pattern.substring(i + 1, end);
               options.add(r);
               i = end + 1;
               continue;
            }
         }

         return i;
      }
   }

   public static void parse(String pattern, List patternConverters, List formattingInfos, Map converterRegistry, Map rules) {
      if (pattern == null) {
         throw new NullPointerException("pattern");
      } else {
         StringBuffer currentLiteral = new StringBuffer(32);
         int patternLength = pattern.length();
         int state = 0;
         int i = 0;
         FormattingInfo formattingInfo = FormattingInfo.getDefault();

         while(true) {
            while(true) {
               while(i < patternLength) {
                  char c = pattern.charAt(i++);
                  switch(state) {
                  case 0:
                     if (i == patternLength) {
                        currentLiteral.append(c);
                     } else if (c == '%') {
                        switch(pattern.charAt(i)) {
                        case '%':
                           currentLiteral.append(c);
                           ++i;
                           break;
                        default:
                           if (currentLiteral.length() != 0) {
                              patternConverters.add(new LiteralPatternConverter(currentLiteral.toString()));
                              formattingInfos.add(FormattingInfo.getDefault());
                           }

                           currentLiteral.setLength(0);
                           currentLiteral.append(c);
                           state = 1;
                           formattingInfo = FormattingInfo.getDefault();
                        }
                     } else {
                        currentLiteral.append(c);
                     }
                     break;
                  case 1:
                     currentLiteral.append(c);
                     switch(c) {
                     case '-':
                        formattingInfo = new FormattingInfo(true, formattingInfo.getMinLength(), formattingInfo.getMaxLength());
                        break;
                     case '.':
                        state = 3;
                        break;
                     default:
                        if (c >= '0' && c <= '9') {
                           formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), c - 48, formattingInfo.getMaxLength());
                           state = 4;
                        } else {
                           i = finalizeConverter(c, pattern, i, currentLiteral, formattingInfo, converterRegistry, rules, patternConverters, formattingInfos);
                           state = 0;
                           formattingInfo = FormattingInfo.getDefault();
                           currentLiteral.setLength(0);
                        }
                     }
                  case 2:
                  default:
                     break;
                  case 3:
                     currentLiteral.append(c);
                     if (c >= '0' && c <= '9') {
                        formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), c - 48);
                        state = 5;
                        break;
                     }

                     LogLog.error("Error occured in position " + i + ".\n Was expecting digit, instead got char \"" + c + "\".");
                     state = 0;
                     break;
                  case 4:
                     currentLiteral.append(c);
                     if (c >= '0' && c <= '9') {
                        formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength() * 10 + (c - 48), formattingInfo.getMaxLength());
                     } else if (c == '.') {
                        state = 3;
                     } else {
                        i = finalizeConverter(c, pattern, i, currentLiteral, formattingInfo, converterRegistry, rules, patternConverters, formattingInfos);
                        state = 0;
                        formattingInfo = FormattingInfo.getDefault();
                        currentLiteral.setLength(0);
                     }
                     break;
                  case 5:
                     currentLiteral.append(c);
                     if (c >= '0' && c <= '9') {
                        formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), formattingInfo.getMaxLength() * 10 + (c - 48));
                     } else {
                        i = finalizeConverter(c, pattern, i, currentLiteral, formattingInfo, converterRegistry, rules, patternConverters, formattingInfos);
                        state = 0;
                        formattingInfo = FormattingInfo.getDefault();
                        currentLiteral.setLength(0);
                     }
                  }
               }

               if (currentLiteral.length() != 0) {
                  patternConverters.add(new LiteralPatternConverter(currentLiteral.toString()));
                  formattingInfos.add(FormattingInfo.getDefault());
               }

               return;
            }
         }
      }
   }

   private static PatternConverter createConverter(String converterId, StringBuffer currentLiteral, Map converterRegistry, Map rules, List options) {
      String converterName = converterId;
      Object converterObj = null;

      for(int i = converterId.length(); i > 0 && converterObj == null; --i) {
         converterName = converterName.substring(0, i);
         if (converterRegistry != null) {
            converterObj = converterRegistry.get(converterName);
         }

         if (converterObj == null && rules != null) {
            converterObj = rules.get(converterName);
         }
      }

      if (converterObj == null) {
         LogLog.error("Unrecognized format specifier [" + converterId + "]");
         return null;
      } else {
         Class converterClass = null;
         if (converterObj instanceof Class) {
            converterClass = (Class)converterObj;
         } else {
            if (!(converterObj instanceof String)) {
               LogLog.warn("Bad map entry for conversion pattern %" + converterName + ".");
               return null;
            }

            try {
               converterClass = Loader.loadClass((String)converterObj);
            } catch (ClassNotFoundException var11) {
               LogLog.warn("Class for conversion pattern %" + converterName + " not found", var11);
               return null;
            }
         }

         try {
            Method factory = converterClass.getMethod("newInstance", Class.forName("[Ljava.lang.String;"));
            String[] optionsArray = new String[options.size()];
            optionsArray = (String[])((String[])options.toArray(optionsArray));
            Object newObj = factory.invoke((Object)null, optionsArray);
            if (newObj instanceof PatternConverter) {
               currentLiteral.delete(0, currentLiteral.length() - (converterId.length() - converterName.length()));
               return (PatternConverter)newObj;
            }

            LogLog.warn("Class " + converterClass.getName() + " does not extend PatternConverter.");
         } catch (Exception var13) {
            LogLog.error("Error creating converter for " + converterId, var13);

            try {
               PatternConverter pc = (PatternConverter)converterClass.newInstance();
               currentLiteral.delete(0, currentLiteral.length() - (converterId.length() - converterName.length()));
               return pc;
            } catch (Exception var12) {
               LogLog.error("Error creating converter for " + converterId, var12);
            }
         }

         return null;
      }
   }

   private static int finalizeConverter(char c, String pattern, int i, StringBuffer currentLiteral, FormattingInfo formattingInfo, Map converterRegistry, Map rules, List patternConverters, List formattingInfos) {
      StringBuffer convBuf = new StringBuffer();
      i = extractConverter(c, pattern, i, convBuf, currentLiteral);
      String converterId = convBuf.toString();
      List options = new ArrayList();
      i = extractOptions(pattern, i, options);
      PatternConverter pc = createConverter(converterId, currentLiteral, converterRegistry, rules, options);
      if (pc == null) {
         StringBuffer msg;
         if (converterId != null && converterId.length() != 0) {
            msg = new StringBuffer("Unrecognized conversion specifier [");
            msg.append(converterId);
            msg.append("] starting at position ");
         } else {
            msg = new StringBuffer("Empty conversion specifier starting at position ");
         }

         msg.append(Integer.toString(i));
         msg.append(" in conversion pattern.");
         LogLog.error(msg.toString());
         patternConverters.add(new LiteralPatternConverter(currentLiteral.toString()));
         formattingInfos.add(FormattingInfo.getDefault());
      } else {
         patternConverters.add(pc);
         formattingInfos.add(formattingInfo);
         if (currentLiteral.length() > 0) {
            patternConverters.add(new LiteralPatternConverter(currentLiteral.toString()));
            formattingInfos.add(FormattingInfo.getDefault());
         }
      }

      currentLiteral.setLength(0);
      return i;
   }

   static {
      Map rules = new HashMap(17);
      rules.put("c", class$org$apache$log4j$pattern$LoggerPatternConverter == null ? (class$org$apache$log4j$pattern$LoggerPatternConverter = class$("org.apache.log4j.pattern.LoggerPatternConverter")) : class$org$apache$log4j$pattern$LoggerPatternConverter);
      rules.put("logger", class$org$apache$log4j$pattern$LoggerPatternConverter == null ? (class$org$apache$log4j$pattern$LoggerPatternConverter = class$("org.apache.log4j.pattern.LoggerPatternConverter")) : class$org$apache$log4j$pattern$LoggerPatternConverter);
      rules.put("C", class$org$apache$log4j$pattern$ClassNamePatternConverter == null ? (class$org$apache$log4j$pattern$ClassNamePatternConverter = class$("org.apache.log4j.pattern.ClassNamePatternConverter")) : class$org$apache$log4j$pattern$ClassNamePatternConverter);
      rules.put("class", class$org$apache$log4j$pattern$ClassNamePatternConverter == null ? (class$org$apache$log4j$pattern$ClassNamePatternConverter = class$("org.apache.log4j.pattern.ClassNamePatternConverter")) : class$org$apache$log4j$pattern$ClassNamePatternConverter);
      rules.put("d", class$org$apache$log4j$pattern$DatePatternConverter == null ? (class$org$apache$log4j$pattern$DatePatternConverter = class$("org.apache.log4j.pattern.DatePatternConverter")) : class$org$apache$log4j$pattern$DatePatternConverter);
      rules.put("date", class$org$apache$log4j$pattern$DatePatternConverter == null ? (class$org$apache$log4j$pattern$DatePatternConverter = class$("org.apache.log4j.pattern.DatePatternConverter")) : class$org$apache$log4j$pattern$DatePatternConverter);
      rules.put("F", class$org$apache$log4j$pattern$FileLocationPatternConverter == null ? (class$org$apache$log4j$pattern$FileLocationPatternConverter = class$("org.apache.log4j.pattern.FileLocationPatternConverter")) : class$org$apache$log4j$pattern$FileLocationPatternConverter);
      rules.put("file", class$org$apache$log4j$pattern$FileLocationPatternConverter == null ? (class$org$apache$log4j$pattern$FileLocationPatternConverter = class$("org.apache.log4j.pattern.FileLocationPatternConverter")) : class$org$apache$log4j$pattern$FileLocationPatternConverter);
      rules.put("l", class$org$apache$log4j$pattern$FullLocationPatternConverter == null ? (class$org$apache$log4j$pattern$FullLocationPatternConverter = class$("org.apache.log4j.pattern.FullLocationPatternConverter")) : class$org$apache$log4j$pattern$FullLocationPatternConverter);
      rules.put("L", class$org$apache$log4j$pattern$LineLocationPatternConverter == null ? (class$org$apache$log4j$pattern$LineLocationPatternConverter = class$("org.apache.log4j.pattern.LineLocationPatternConverter")) : class$org$apache$log4j$pattern$LineLocationPatternConverter);
      rules.put("line", class$org$apache$log4j$pattern$LineLocationPatternConverter == null ? (class$org$apache$log4j$pattern$LineLocationPatternConverter = class$("org.apache.log4j.pattern.LineLocationPatternConverter")) : class$org$apache$log4j$pattern$LineLocationPatternConverter);
      rules.put("m", class$org$apache$log4j$pattern$MessagePatternConverter == null ? (class$org$apache$log4j$pattern$MessagePatternConverter = class$("org.apache.log4j.pattern.MessagePatternConverter")) : class$org$apache$log4j$pattern$MessagePatternConverter);
      rules.put("message", class$org$apache$log4j$pattern$MessagePatternConverter == null ? (class$org$apache$log4j$pattern$MessagePatternConverter = class$("org.apache.log4j.pattern.MessagePatternConverter")) : class$org$apache$log4j$pattern$MessagePatternConverter);
      rules.put("n", class$org$apache$log4j$pattern$LineSeparatorPatternConverter == null ? (class$org$apache$log4j$pattern$LineSeparatorPatternConverter = class$("org.apache.log4j.pattern.LineSeparatorPatternConverter")) : class$org$apache$log4j$pattern$LineSeparatorPatternConverter);
      rules.put("M", class$org$apache$log4j$pattern$MethodLocationPatternConverter == null ? (class$org$apache$log4j$pattern$MethodLocationPatternConverter = class$("org.apache.log4j.pattern.MethodLocationPatternConverter")) : class$org$apache$log4j$pattern$MethodLocationPatternConverter);
      rules.put("method", class$org$apache$log4j$pattern$MethodLocationPatternConverter == null ? (class$org$apache$log4j$pattern$MethodLocationPatternConverter = class$("org.apache.log4j.pattern.MethodLocationPatternConverter")) : class$org$apache$log4j$pattern$MethodLocationPatternConverter);
      rules.put("p", class$org$apache$log4j$pattern$LevelPatternConverter == null ? (class$org$apache$log4j$pattern$LevelPatternConverter = class$("org.apache.log4j.pattern.LevelPatternConverter")) : class$org$apache$log4j$pattern$LevelPatternConverter);
      rules.put("level", class$org$apache$log4j$pattern$LevelPatternConverter == null ? (class$org$apache$log4j$pattern$LevelPatternConverter = class$("org.apache.log4j.pattern.LevelPatternConverter")) : class$org$apache$log4j$pattern$LevelPatternConverter);
      rules.put("r", class$org$apache$log4j$pattern$RelativeTimePatternConverter == null ? (class$org$apache$log4j$pattern$RelativeTimePatternConverter = class$("org.apache.log4j.pattern.RelativeTimePatternConverter")) : class$org$apache$log4j$pattern$RelativeTimePatternConverter);
      rules.put("relative", class$org$apache$log4j$pattern$RelativeTimePatternConverter == null ? (class$org$apache$log4j$pattern$RelativeTimePatternConverter = class$("org.apache.log4j.pattern.RelativeTimePatternConverter")) : class$org$apache$log4j$pattern$RelativeTimePatternConverter);
      rules.put("t", class$org$apache$log4j$pattern$ThreadPatternConverter == null ? (class$org$apache$log4j$pattern$ThreadPatternConverter = class$("org.apache.log4j.pattern.ThreadPatternConverter")) : class$org$apache$log4j$pattern$ThreadPatternConverter);
      rules.put("thread", class$org$apache$log4j$pattern$ThreadPatternConverter == null ? (class$org$apache$log4j$pattern$ThreadPatternConverter = class$("org.apache.log4j.pattern.ThreadPatternConverter")) : class$org$apache$log4j$pattern$ThreadPatternConverter);
      rules.put("x", class$org$apache$log4j$pattern$NDCPatternConverter == null ? (class$org$apache$log4j$pattern$NDCPatternConverter = class$("org.apache.log4j.pattern.NDCPatternConverter")) : class$org$apache$log4j$pattern$NDCPatternConverter);
      rules.put("ndc", class$org$apache$log4j$pattern$NDCPatternConverter == null ? (class$org$apache$log4j$pattern$NDCPatternConverter = class$("org.apache.log4j.pattern.NDCPatternConverter")) : class$org$apache$log4j$pattern$NDCPatternConverter);
      rules.put("X", class$org$apache$log4j$pattern$PropertiesPatternConverter == null ? (class$org$apache$log4j$pattern$PropertiesPatternConverter = class$("org.apache.log4j.pattern.PropertiesPatternConverter")) : class$org$apache$log4j$pattern$PropertiesPatternConverter);
      rules.put("properties", class$org$apache$log4j$pattern$PropertiesPatternConverter == null ? (class$org$apache$log4j$pattern$PropertiesPatternConverter = class$("org.apache.log4j.pattern.PropertiesPatternConverter")) : class$org$apache$log4j$pattern$PropertiesPatternConverter);
      rules.put("sn", class$org$apache$log4j$pattern$SequenceNumberPatternConverter == null ? (class$org$apache$log4j$pattern$SequenceNumberPatternConverter = class$("org.apache.log4j.pattern.SequenceNumberPatternConverter")) : class$org$apache$log4j$pattern$SequenceNumberPatternConverter);
      rules.put("sequenceNumber", class$org$apache$log4j$pattern$SequenceNumberPatternConverter == null ? (class$org$apache$log4j$pattern$SequenceNumberPatternConverter = class$("org.apache.log4j.pattern.SequenceNumberPatternConverter")) : class$org$apache$log4j$pattern$SequenceNumberPatternConverter);
      rules.put("throwable", class$org$apache$log4j$pattern$ThrowableInformationPatternConverter == null ? (class$org$apache$log4j$pattern$ThrowableInformationPatternConverter = class$("org.apache.log4j.pattern.ThrowableInformationPatternConverter")) : class$org$apache$log4j$pattern$ThrowableInformationPatternConverter);
      PATTERN_LAYOUT_RULES = new PatternParser.ReadOnlyMap(rules);
      Map fnameRules = new HashMap(4);
      fnameRules.put("d", class$org$apache$log4j$pattern$FileDatePatternConverter == null ? (class$org$apache$log4j$pattern$FileDatePatternConverter = class$("org.apache.log4j.pattern.FileDatePatternConverter")) : class$org$apache$log4j$pattern$FileDatePatternConverter);
      fnameRules.put("date", class$org$apache$log4j$pattern$FileDatePatternConverter == null ? (class$org$apache$log4j$pattern$FileDatePatternConverter = class$("org.apache.log4j.pattern.FileDatePatternConverter")) : class$org$apache$log4j$pattern$FileDatePatternConverter);
      fnameRules.put("i", class$org$apache$log4j$pattern$IntegerPatternConverter == null ? (class$org$apache$log4j$pattern$IntegerPatternConverter = class$("org.apache.log4j.pattern.IntegerPatternConverter")) : class$org$apache$log4j$pattern$IntegerPatternConverter);
      fnameRules.put("index", class$org$apache$log4j$pattern$IntegerPatternConverter == null ? (class$org$apache$log4j$pattern$IntegerPatternConverter = class$("org.apache.log4j.pattern.IntegerPatternConverter")) : class$org$apache$log4j$pattern$IntegerPatternConverter);
      FILENAME_PATTERN_RULES = new PatternParser.ReadOnlyMap(fnameRules);
   }

   private static class ReadOnlyMap implements Map {
      private final Map map;

      public ReadOnlyMap(Map src) {
         this.map = src;
      }

      public void clear() {
         throw new UnsupportedOperationException();
      }

      public boolean containsKey(Object key) {
         return this.map.containsKey(key);
      }

      public boolean containsValue(Object value) {
         return this.map.containsValue(value);
      }

      public Set entrySet() {
         return this.map.entrySet();
      }

      public Object get(Object key) {
         return this.map.get(key);
      }

      public boolean isEmpty() {
         return this.map.isEmpty();
      }

      public Set keySet() {
         return this.map.keySet();
      }

      public Object put(Object key, Object value) {
         throw new UnsupportedOperationException();
      }

      public void putAll(Map t) {
         throw new UnsupportedOperationException();
      }

      public Object remove(Object key) {
         throw new UnsupportedOperationException();
      }

      public int size() {
         return this.map.size();
      }

      public Collection values() {
         return this.map.values();
      }
   }
}
