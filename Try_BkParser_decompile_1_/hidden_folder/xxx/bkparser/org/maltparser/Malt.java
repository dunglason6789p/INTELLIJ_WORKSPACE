package org.maltparser;

public class Malt {
   public Malt() {
   }

   public static void main(String[] args) {
      MaltConsoleEngine engine = new MaltConsoleEngine();
      engine.startEngine(args);
   }
}
