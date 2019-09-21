package org.maltparser.core.lw.graph;

import org.maltparser.core.exception.MaltChainedException;

public class LWGraphException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public LWGraphException(String message) {
      super(message);
   }

   public LWGraphException(String message, Throwable cause) {
      super(message, cause);
   }
}
