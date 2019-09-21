package com.google.gson;

import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class DefaultDateTypeAdapter extends TypeAdapter<Date> {
   private static final String SIMPLE_NAME = "DefaultDateTypeAdapter";
   private final Class<? extends Date> dateType;
   private final DateFormat enUsFormat;
   private final DateFormat localFormat;

   DefaultDateTypeAdapter(Class<? extends Date> dateType) {
      this(dateType, DateFormat.getDateTimeInstance(2, 2, Locale.US), DateFormat.getDateTimeInstance(2, 2));
   }

   DefaultDateTypeAdapter(Class<? extends Date> dateType, String datePattern) {
      this(dateType, new SimpleDateFormat(datePattern, Locale.US), new SimpleDateFormat(datePattern));
   }

   DefaultDateTypeAdapter(Class<? extends Date> dateType, int style) {
      this(dateType, DateFormat.getDateInstance(style, Locale.US), DateFormat.getDateInstance(style));
   }

   public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
      this(Date.class, DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US), DateFormat.getDateTimeInstance(dateStyle, timeStyle));
   }

   public DefaultDateTypeAdapter(Class<? extends Date> dateType, int dateStyle, int timeStyle) {
      this(dateType, DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US), DateFormat.getDateTimeInstance(dateStyle, timeStyle));
   }

   DefaultDateTypeAdapter(Class<? extends Date> dateType, DateFormat enUsFormat, DateFormat localFormat) {
      if (dateType != Date.class && dateType != java.sql.Date.class && dateType != Timestamp.class) {
         throw new IllegalArgumentException("Date type must be one of " + Date.class + ", " + Timestamp.class + ", or " + java.sql.Date.class + " but was " + dateType);
      } else {
         this.dateType = dateType;
         this.enUsFormat = enUsFormat;
         this.localFormat = localFormat;
      }
   }

   public void write(JsonWriter out, Date value) throws IOException {
      if (value == null) {
         out.nullValue();
      } else {
         synchronized(this.localFormat) {
            String dateFormatAsString = this.enUsFormat.format(value);
            out.value(dateFormatAsString);
         }
      }
   }

   public Date read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
         in.nextNull();
         return null;
      } else {
         Date date = this.deserializeToDate(in.nextString());
         if (this.dateType == Date.class) {
            return date;
         } else if (this.dateType == Timestamp.class) {
            return new Timestamp(date.getTime());
         } else if (this.dateType == java.sql.Date.class) {
            return new java.sql.Date(date.getTime());
         } else {
            throw new AssertionError();
         }
      }
   }

   private Date deserializeToDate(String s) {
      synchronized(this.localFormat) {
         Date var10000;
         try {
            var10000 = this.localFormat.parse(s);
         } catch (ParseException var7) {
            try {
               var10000 = this.enUsFormat.parse(s);
            } catch (ParseException var6) {
               try {
                  var10000 = ISO8601Utils.parse(s, new ParsePosition(0));
               } catch (ParseException var5) {
                  throw new JsonSyntaxException(s, var5);
               }

               return var10000;
            }

            return var10000;
         }

         return var10000;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("DefaultDateTypeAdapter");
      sb.append('(').append(this.localFormat.getClass().getSimpleName()).append(')');
      return sb.toString();
   }
}
