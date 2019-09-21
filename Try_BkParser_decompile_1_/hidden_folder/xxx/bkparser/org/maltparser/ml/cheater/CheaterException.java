package org.maltparser.ml.cheater;

import org.maltparser.core.exception.MaltChainedException;

public class CheaterException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public CheaterException(String message) {
      super(message);
   }

   public CheaterException(String message, Throwable cause) {
      super(message, cause);
   }
}
