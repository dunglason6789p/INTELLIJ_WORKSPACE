package org.maltparser.parser.history;

import org.maltparser.core.exception.MaltChainedException;

public class HistoryException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public HistoryException(String message) {
      super(message);
   }

   public HistoryException(String message, Throwable cause) {
      super(message, cause);
   }
}
