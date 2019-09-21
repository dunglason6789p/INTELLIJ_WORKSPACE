package org.maltparser.core.config;

import java.util.HashMap;

public class ConfigurationRegistry extends HashMap<Class<?>, Object> {
   public static final long serialVersionUID = 3256444702936019250L;

   public ConfigurationRegistry() {
   }

   public Object get(Object key) {
      return super.get(key);
   }

   public Object put(Class<?> key, Object value) {
      return super.put(key, value);
   }
}
