package vn.edu.hust.nlp.preprocess;

import com.google.gson.JsonArray;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import vn.edu.hust.nlp.lexicon.LexiconRead;

public class FeaturesExtraction {
   static LexiconRead lexicon = new LexiconRead();

   public FeaturesExtraction() {
   }

   public ItemSequence sequence2itemsequence(String sequence) {
      int length = sequence.split(" ").length;
      ItemSequence itemSequence = new ItemSequence();

      for(int i = 0; i < length; ++i) {
         Item item = this.word2feature(sequence, i);
         itemSequence.add(item);
      }

      return itemSequence;
   }

   public Item word2feature(String sequence, int i) {
      String[] words = sequence.split(" ");
      String word = words[i];
      JsonArray possible_tags = lexicon.getTagsFromWordBinary(word.toLowerCase());
      if (possible_tags.size() == 0) {
         possible_tags = lexicon.getTagsFromWordBinary(word.toUpperCase());
      }

      Item item = new Item();
      String word_word = "w[0]=" + word;
      String num = "num[0]=" + StringUtils.isNumeric(word);
      String cap = "cap[0]=" + StringUtils.isCapitalized(word);
      String sym = "sym[0]=" + !StringUtils.isAlphanumeric(word);
      String punct = "punct[0]=" + StringUtils.isPunct(word);
      String all_cap = "all_cap[0]=false";
      if (word.equals(word.toUpperCase())) {
         all_cap = "all_cap[0]=true";
      }

      item.add(new Attribute(num));
      item.add(new Attribute(cap));
      item.add(new Attribute(sym));
      item.add(new Attribute(punct));
      item.add(new Attribute(all_cap));
      item.add(new Attribute(word_word));
      String word_post2;
      String word_post2_word;
      if (i > 0) {
         word_post2 = words[i - 1];
         word_post2_word = "w[-1]=" + word_post2;
         item.add(new Attribute(word_post2_word));
      }

      if (i > 1) {
         word_post2 = words[i - 2];
         word_post2_word = "w[-2]=" + word_post2;
         item.add(new Attribute(word_post2_word));
      }

      if (i < words.length - 1) {
         word_post2 = words[i + 1];
         word_post2_word = "w[1]=" + word_post2;
         item.add(new Attribute(word_post2_word));
      }

      if (i < words.length - 2) {
         word_post2 = words[i + 2];
         word_post2_word = "w[2]=" + word_post2;
         item.add(new Attribute(word_post2_word));
      }

      StringBuilder all_possible = new StringBuilder();

      for(int j = 0; j < possible_tags.size(); ++j) {
         String possible = "possible[" + j + "]=" + possible_tags.get(j).getAsString();
         all_possible.append(possible + "\t");
         item.add(new Attribute(possible));
      }

      if (i == 0) {
         item.add(new Attribute("__BOS__"));
      }

      if (i == words.length - 1) {
         item.add(new Attribute("\t__EOS__"));
      }

      return item;
   }
}
