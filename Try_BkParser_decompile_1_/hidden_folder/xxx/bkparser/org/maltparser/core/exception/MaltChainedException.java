package org.maltparser.core.exception;

public class MaltChainedException extends Exception {
   public static final long serialVersionUID = 8045568022124816379L;
   private final Throwable cause;

   public MaltChainedException(String message) {
      this(message, (Throwable)null);
   }

   public MaltChainedException(String message, Throwable cause) {
      super(message);
      this.cause = cause;
   }

   public Throwable getCause() {
      return this.cause;
   }

   public String getMessageChain() {
      StringBuilder sb = new StringBuilder();

      for(Object t = this; t != null; t = ((Throwable)t).getCause()) {
         if (((Throwable)t).getMessage() != null && t instanceof MaltChainedException) {
            sb.append(((Throwable)t).getMessage() + "\n");
         }
      }

      return sb.toString();
   }

   public void printStackTrace() {
      super.printStackTrace();
      if (this.cause != null) {
         this.cause.printStackTrace();
      }

   }
}
