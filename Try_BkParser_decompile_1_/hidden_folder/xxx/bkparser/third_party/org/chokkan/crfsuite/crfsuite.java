package third_party.org.chokkan.crfsuite;

public class crfsuite {
   public crfsuite() {
   }

   public static String version() {
      return crfsuiteJNI.version();
   }
}
