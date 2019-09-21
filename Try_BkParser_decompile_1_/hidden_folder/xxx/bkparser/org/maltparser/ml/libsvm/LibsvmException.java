package org.maltparser.ml.libsvm;

import org.maltparser.core.exception.MaltChainedException;

public class LibsvmException extends MaltChainedException {
   public static final long serialVersionUID = 8045568022124816379L;

   public LibsvmException(String message) {
      super(message);
   }

   public LibsvmException(String message, Throwable cause) {
      super(message, cause);
   }
}
