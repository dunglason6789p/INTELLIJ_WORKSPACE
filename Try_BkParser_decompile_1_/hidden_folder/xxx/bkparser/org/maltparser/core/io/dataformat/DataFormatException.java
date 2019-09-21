package org.maltparser.core.io.dataformat;

import org.maltparser.core.exception.MaltChainedException;

public class DataFormatException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public DataFormatException(String message) {
      super(message);
   }

   public DataFormatException(String message, Throwable cause) {
      super(message, cause);
   }
}
