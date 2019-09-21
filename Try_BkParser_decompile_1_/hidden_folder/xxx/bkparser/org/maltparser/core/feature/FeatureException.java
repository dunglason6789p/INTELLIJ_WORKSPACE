package org.maltparser.core.feature;

import org.maltparser.core.exception.MaltChainedException;

public class FeatureException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public FeatureException(String message) {
      super(message);
   }

   public FeatureException(String message, Throwable cause) {
      super(message, cause);
   }
}
