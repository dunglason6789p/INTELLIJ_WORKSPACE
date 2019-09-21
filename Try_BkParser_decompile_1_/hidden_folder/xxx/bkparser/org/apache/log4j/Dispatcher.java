package org.apache.log4j;

import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.helpers.BoundedFIFO;
import org.apache.log4j.spi.LoggingEvent;

/** @deprecated */
class Dispatcher extends Thread {
   /** @deprecated */
   private BoundedFIFO bf;
   private AppenderAttachableImpl aai;
   private boolean interrupted = false;
   AsyncAppender container;

   /** @deprecated */
   Dispatcher(BoundedFIFO bf, AsyncAppender container) {
      this.bf = bf;
      this.container = container;
      this.aai = container.aai;
      this.setDaemon(true);
      this.setPriority(1);
      this.setName("Dispatcher-" + this.getName());
   }

   void close() {
      synchronized(this.bf) {
         this.interrupted = true;
         if (this.bf.length() == 0) {
            this.bf.notify();
         }

      }
   }

   public void run() {
      while(true) {
         LoggingEvent event;
         label52: {
            synchronized(this.bf) {
               label57: {
                  if (this.bf.length() == 0) {
                     if (this.interrupted) {
                        break label57;
                     }

                     try {
                        this.bf.wait();
                     } catch (InterruptedException var7) {
                        break label57;
                     }
                  }

                  event = this.bf.get();
                  if (this.bf.wasFull()) {
                     this.bf.notify();
                  }
                  break label52;
               }
            }

            this.aai.removeAllAppenders();
            return;
         }

         synchronized(this.container.aai) {
            if (this.aai != null && event != null) {
               this.aai.appendLoopOnAppenders(event);
            }
         }
      }
   }
}
