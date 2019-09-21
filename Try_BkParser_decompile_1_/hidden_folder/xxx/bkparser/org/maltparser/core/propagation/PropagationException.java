package org.maltparser.core.propagation;

import org.maltparser.core.exception.MaltChainedException;

public class PropagationException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public PropagationException(String message) {
      super(message);
   }

   public PropagationException(String message, Throwable cause) {
      super(message, cause);
   }
}
