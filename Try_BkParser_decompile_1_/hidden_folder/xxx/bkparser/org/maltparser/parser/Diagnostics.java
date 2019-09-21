package org.maltparser.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.maltparser.core.exception.MaltChainedException;

public class Diagnostics {
   protected final BufferedWriter diaWriter;

   public Diagnostics(String fileName) throws MaltChainedException {
      try {
         if (fileName.equals("stdout")) {
            this.diaWriter = new BufferedWriter(new OutputStreamWriter(System.out));
         } else if (fileName.equals("stderr")) {
            this.diaWriter = new BufferedWriter(new OutputStreamWriter(System.err));
         } else {
            this.diaWriter = new BufferedWriter(new FileWriter(fileName));
         }

      } catch (IOException var3) {
         throw new MaltChainedException("Could not open the diagnostic file. ", var3);
      }
   }

   public BufferedWriter getDiaWriter() {
      return this.diaWriter;
   }

   public void writeToDiaFile(String message) throws MaltChainedException {
      try {
         this.getDiaWriter().write(message);
      } catch (IOException var3) {
         throw new MaltChainedException("Could not write to the diagnostic file. ", var3);
      }
   }

   public void closeDiaWriter() throws MaltChainedException {
      if (this.diaWriter != null) {
         try {
            this.diaWriter.flush();
            this.diaWriter.close();
         } catch (IOException var2) {
            throw new MaltChainedException("Could not close the diagnostic file. ", var2);
         }
      }

   }
}
