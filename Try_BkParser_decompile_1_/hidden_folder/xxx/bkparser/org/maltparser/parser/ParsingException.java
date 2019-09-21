package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;

public class ParsingException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public ParsingException(String message) {
      super(message);
   }

   public ParsingException(String message, Throwable cause) {
      super(message, cause);
   }
}
