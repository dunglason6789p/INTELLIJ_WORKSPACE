package org.maltparser.core.symbol.trie;

import org.maltparser.core.symbol.SymbolException;

public class Trie {
   private final TrieNode root = new TrieNode(' ', (TrieNode)null);

   public Trie() {
   }

   public TrieNode addValue(String value, TrieSymbolTable table, int code) throws SymbolException {
      TrieNode node = this.root;
      char[] chars = value.toCharArray();

      for(int i = chars.length - 1; i >= 0; --i) {
         if (i == 0) {
            node = node.getOrAddChild(true, chars[i], table, code);
         } else {
            node = node.getOrAddChild(false, chars[i], table, code);
         }
      }

      return node;
   }

   public TrieNode addValue(StringBuilder symbol, TrieSymbolTable table, int code) throws SymbolException {
      TrieNode node = this.root;

      for(int i = symbol.length() - 1; i >= 0; --i) {
         if (i == 0) {
            node = node.getOrAddChild(true, symbol.charAt(i), table, code);
         } else {
            node = node.getOrAddChild(false, symbol.charAt(i), table, code);
         }
      }

      return node;
   }

   public String getValue(TrieNode node, TrieSymbolTable table) {
      StringBuilder sb = new StringBuilder();

      for(TrieNode tmp = node; tmp != this.root; tmp = tmp.getParent()) {
         sb.append(tmp.getCharacter());
      }

      return sb.toString();
   }

   public Integer getEntry(String value, TrieSymbolTable table) {
      TrieNode node = this.root;
      char[] chars = value.toCharArray();

      int i;
      for(i = chars.length - 1; i >= 0 && node != null; --i) {
         node = node.getChild(chars[i]);
      }

      return i < 0 && node != null ? node.getEntry(table) : null;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         return this.root == null ? ((Trie)obj).root == null : this.root.equals(((Trie)obj).root);
      }
   }

   public int hashCode() {
      return 217 + (null == this.root ? 0 : this.root.hashCode());
   }
}
