package org.maltparser.concurrent.test;

import org.maltparser.core.exception.MaltChainedException;

public class ExperimentException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public ExperimentException(String message) {
      super(message);
   }

   public ExperimentException(String message, Throwable cause) {
      super(message, cause);
   }
}
