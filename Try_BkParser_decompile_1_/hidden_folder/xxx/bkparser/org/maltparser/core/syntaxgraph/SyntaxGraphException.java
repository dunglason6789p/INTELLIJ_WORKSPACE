package org.maltparser.core.syntaxgraph;

import org.maltparser.core.exception.MaltChainedException;

public class SyntaxGraphException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public SyntaxGraphException(String message) {
      super(message);
   }

   public SyntaxGraphException(String message, Throwable cause) {
      super(message, cause);
   }
}
