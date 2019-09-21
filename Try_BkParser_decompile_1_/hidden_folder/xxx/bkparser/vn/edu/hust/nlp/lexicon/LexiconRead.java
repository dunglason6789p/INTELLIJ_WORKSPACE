package vn.edu.hust.nlp.lexicon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LexiconRead {
   static JsonArray lexicon;

   public LexiconRead() {
   }

   public JsonArray getTagsFromWordBinary(String word) {
      int start = 0;
      int end = lexicon.size() - 1;

      while(start <= end) {
         int mid = (start + end) / 2;
         String word_lexicon = lexicon.get(mid).getAsJsonObject().get("word").getAsString();
         if (word_lexicon.equals(word)) {
            return lexicon.get(mid).getAsJsonObject().get("tags").getAsJsonArray();
         }

         if (word_lexicon.compareTo(word) < 0) {
            start = mid + 1;
         } else {
            end = mid - 1;
         }
      }

      return new JsonArray();
   }

   public static JsonArray readLexiconToJsonArray(String pathLexicon) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pathLexicon), "UTF-8"));
      JsonArray response_lexicon = new JsonArray();

      String line;
      while((line = reader.readLine()) != null) {
         String[] words = line.split("\t");
         String word = words[0];
         JsonArray tags = new JsonArray();

         for(int i = 1; i < words.length; ++i) {
            tags.add(words[i]);
         }

         JsonObject lexicon = new JsonObject();
         lexicon.addProperty("word", word);
         lexicon.add("tags", tags);
         response_lexicon.add((JsonElement)lexicon);
      }

      reader.close();
      return response_lexicon;
   }

   static {
      try {
         lexicon = readLexiconToJsonArray("dictionary/bktb-vtb-lexicon-final");
      } catch (IOException var1) {
         var1.printStackTrace();
      }

   }
}
