package org.maltparser.concurrent.graph;

import org.maltparser.core.exception.MaltChainedException;

public class ConcurrentGraphException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public ConcurrentGraphException(String message) {
      super(message);
   }

   public ConcurrentGraphException(String message, Throwable cause) {
      super(message, cause);
   }
}
