package org.apache.log4j;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

public class RollingFileAppender extends FileAppender {
   protected long maxFileSize = 10485760L;
   protected int maxBackupIndex = 1;
   private long nextRollover = 0L;

   public RollingFileAppender() {
   }

   public RollingFileAppender(Layout layout, String filename, boolean append) throws IOException {
      super(layout, filename, append);
   }

   public RollingFileAppender(Layout layout, String filename) throws IOException {
      super(layout, filename);
   }

   public int getMaxBackupIndex() {
      return this.maxBackupIndex;
   }

   public long getMaximumFileSize() {
      return this.maxFileSize;
   }

   public void rollOver() {
      if (super.qw != null) {
         long size = ((CountingQuietWriter)super.qw).getCount();
         LogLog.debug("rolling over count=" + size);
         this.nextRollover = size + this.maxFileSize;
      }

      LogLog.debug("maxBackupIndex=" + this.maxBackupIndex);
      boolean renameSucceeded = true;
      if (this.maxBackupIndex > 0) {
         File file = new File(super.fileName + '.' + this.maxBackupIndex);
         if (file.exists()) {
            renameSucceeded = file.delete();
         }

         File target;
         for(int i = this.maxBackupIndex - 1; i >= 1 && renameSucceeded; --i) {
            file = new File(super.fileName + "." + i);
            if (file.exists()) {
               target = new File(super.fileName + '.' + (i + 1));
               LogLog.debug("Renaming file " + file + " to " + target);
               renameSucceeded = file.renameTo(target);
            }
         }

         if (renameSucceeded) {
            target = new File(super.fileName + "." + 1);
            this.closeFile();
            file = new File(super.fileName);
            LogLog.debug("Renaming file " + file + " to " + target);
            renameSucceeded = file.renameTo(target);
            if (!renameSucceeded) {
               try {
                  this.setFile(super.fileName, true, super.bufferedIO, super.bufferSize);
               } catch (IOException var6) {
                  if (var6 instanceof InterruptedIOException) {
                     Thread.currentThread().interrupt();
                  }

                  LogLog.error("setFile(" + super.fileName + ", true) call failed.", var6);
               }
            }
         }
      }

      if (renameSucceeded) {
         try {
            this.setFile(super.fileName, false, super.bufferedIO, super.bufferSize);
            this.nextRollover = 0L;
         } catch (IOException var5) {
            if (var5 instanceof InterruptedIOException) {
               Thread.currentThread().interrupt();
            }

            LogLog.error("setFile(" + super.fileName + ", false) call failed.", var5);
         }
      }

   }

   public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
      super.setFile(fileName, append, super.bufferedIO, super.bufferSize);
      if (append) {
         File f = new File(fileName);
         ((CountingQuietWriter)super.qw).setCount(f.length());
      }

   }

   public void setMaxBackupIndex(int maxBackups) {
      this.maxBackupIndex = maxBackups;
   }

   public void setMaximumFileSize(long maxFileSize) {
      this.maxFileSize = maxFileSize;
   }

   public void setMaxFileSize(String value) {
      this.maxFileSize = OptionConverter.toFileSize(value, this.maxFileSize + 1L);
   }

   protected void setQWForFiles(Writer writer) {
      super.qw = new CountingQuietWriter(writer, super.errorHandler);
   }

   protected void subAppend(LoggingEvent event) {
      super.subAppend(event);
      if (super.fileName != null && super.qw != null) {
         long size = ((CountingQuietWriter)super.qw).getCount();
         if (size >= this.maxFileSize && size >= this.nextRollover) {
            this.rollOver();
         }
      }

   }
}
