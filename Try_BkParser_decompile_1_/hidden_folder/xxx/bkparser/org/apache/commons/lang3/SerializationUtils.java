package org.apache.commons.lang3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class SerializationUtils {
   public SerializationUtils() {
   }

   public static <T extends Serializable> T clone(T object) {
      T result = (Serializable)deserialize(serialize(object));
      return result;
   }

   public static void serialize(Serializable obj, OutputStream outputStream) {
      if (outputStream == null) {
         throw new IllegalArgumentException("The OutputStream must not be null");
      } else {
         ObjectOutputStream out = null;

         try {
            out = new ObjectOutputStream(outputStream);
            out.writeObject(obj);
         } catch (IOException var11) {
            throw new SerializationException(var11);
         } finally {
            try {
               if (out != null) {
                  out.close();
               }
            } catch (IOException var10) {
            }

         }

      }
   }

   public static byte[] serialize(Serializable obj) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
      serialize(obj, baos);
      return baos.toByteArray();
   }

   public static Object deserialize(InputStream inputStream) {
      if (inputStream == null) {
         throw new IllegalArgumentException("The InputStream must not be null");
      } else {
         ObjectInputStream in = null;

         Object var2;
         try {
            in = new ObjectInputStream(inputStream);
            var2 = in.readObject();
         } catch (ClassNotFoundException var12) {
            throw new SerializationException(var12);
         } catch (IOException var13) {
            throw new SerializationException(var13);
         } finally {
            try {
               if (in != null) {
                  in.close();
               }
            } catch (IOException var11) {
            }

         }

         return var2;
      }
   }

   public static Object deserialize(byte[] objectData) {
      if (objectData == null) {
         throw new IllegalArgumentException("The byte[] must not be null");
      } else {
         ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
         return deserialize((InputStream)bais);
      }
   }
}
