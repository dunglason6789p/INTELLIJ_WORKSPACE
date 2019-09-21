package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

public class LookupTranslator extends CharSequenceTranslator {
   private final HashMap<CharSequence, CharSequence> lookupMap = new HashMap();
   private final int shortest;
   private final int longest;

   public LookupTranslator(CharSequence[]... lookup) {
      int _shortest = 2147483647;
      int _longest = 0;
      if (lookup != null) {
         CharSequence[][] arr$ = lookup;
         int len$ = lookup.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            CharSequence[] seq = arr$[i$];
            this.lookupMap.put(seq[0], seq[1]);
            int sz = seq[0].length();
            if (sz < _shortest) {
               _shortest = sz;
            }

            if (sz > _longest) {
               _longest = sz;
            }
         }
      }

      this.shortest = _shortest;
      this.longest = _longest;
   }

   public int translate(CharSequence input, int index, Writer out) throws IOException {
      int max = this.longest;
      if (index + this.longest > input.length()) {
         max = input.length() - index;
      }

      for(int i = max; i >= this.shortest; --i) {
         CharSequence subSeq = input.subSequence(index, index + i);
         CharSequence result = (CharSequence)this.lookupMap.get(subSeq);
         if (result != null) {
            out.write(result.toString());
            return i;
         }
      }

      return 0;
   }
}
