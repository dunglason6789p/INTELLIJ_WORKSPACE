package org.maltparser.core.config;

import org.maltparser.core.exception.MaltChainedException;

public class ConfigurationException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public ConfigurationException(String message) {
      super(message);
   }

   public ConfigurationException(String message, Throwable cause) {
      super(message, cause);
   }
}
