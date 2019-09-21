package vn.edu.vnu.uet.nlp.tokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vn.edu.vnu.uet.nlp.utils.StringUtils;

public class Tokenizer {
   public Tokenizer() {
   }

   public static List<String> tokenize(String s) throws IOException {
      if (s != null && !s.trim().isEmpty()) {
         String[] tempTokens = s.trim().split("\\s+");
         if (tempTokens.length == 0) {
            return new ArrayList();
         } else {
            List<String> tokens = new ArrayList();
            String[] var3 = tempTokens;
            int var4 = tempTokens.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String token = var3[var5];
               if (token.length() != 1 && StringUtils.hasPunctuation(token)) {
                  if (token.endsWith(",")) {
                     ((List)tokens).addAll(tokenize(token.substring(0, token.length() - 1)));
                     ((List)tokens).add(",");
                  } else if (AbbreviationException.getAbbreviation().contains(token)) {
                     ((List)tokens).add(token);
                  } else if (token.endsWith(".") && Character.isAlphabetic(token.charAt(token.length() - 2))) {
                     if (token.length() == 2 && Character.isUpperCase(token.charAt(token.length() - 2))) {
                        ((List)tokens).add(token);
                     } else {
                        ((List)tokens).addAll(tokenize(token.substring(0, token.length() - 1)));
                        ((List)tokens).add(".");
                     }
                  } else if (AbbreviationException.getException().contains(token)) {
                     ((List)tokens).add(token);
                  } else {
                     boolean tokenContainsAbb = false;
                     Iterator var8 = AbbreviationException.getAbbreviation().iterator();

                     while(var8.hasNext()) {
                        String e = (String)var8.next();
                        int i = token.indexOf(e);
                        if (i >= 0) {
                           tokenContainsAbb = true;
                           tokens = recursive((List)tokens, token, i, i + e.length());
                           break;
                        }
                     }

                     if (!tokenContainsAbb) {
                        boolean tokenContainsExp = false;
                        Iterator var22 = AbbreviationException.getException().iterator();

                        int i;
                        while(var22.hasNext()) {
                           String e = (String)var22.next();
                           i = token.indexOf(e);
                           if (i >= 0) {
                              tokenContainsExp = true;
                              tokens = recursive((List)tokens, token, i, i + e.length());
                              break;
                           }
                        }

                        if (!tokenContainsExp) {
                           List<String> regexes = Regex.getRegexList();
                           boolean matching = false;
                           Iterator var26 = regexes.iterator();

                           while(var26.hasNext()) {
                              String regex = (String)var26.next();
                              if (token.matches(regex)) {
                                 ((List)tokens).add(token);
                                 matching = true;
                                 break;
                              }
                           }

                           if (!matching) {
                              i = 0;

                              while(i < regexes.size()) {
                                 label139: {
                                    Pattern pattern = Pattern.compile((String)regexes.get(i));
                                    Matcher matcher = pattern.matcher(token);
                                    if (matcher.find()) {
                                       boolean hasLetter;
                                       if (i != Regex.getRegexIndex("url")) {
                                          if (i == Regex.getRegexIndex("month")) {
                                             int start = matcher.start();
                                             hasLetter = false;

                                             for(int j = 0; j < start; ++j) {
                                                if (Character.isLetter(token.charAt(j))) {
                                                   tokens = recursive((List)tokens, token, matcher.start(), matcher.end());
                                                   hasLetter = true;
                                                   break;
                                                }
                                             }

                                             if (!hasLetter) {
                                                ((List)tokens).add(token);
                                             }
                                          } else {
                                             tokens = recursive((List)tokens, token, matcher.start(), matcher.end());
                                          }
                                          break label139;
                                       }

                                       String[] elements = token.split(Pattern.quote("."));
                                       hasLetter = true;
                                       String[] var16 = elements;
                                       int var17 = elements.length;

                                       for(int var18 = 0; var18 < var17; ++var18) {
                                          String ele = var16[var18];
                                          if (ele.length() == 1 && Character.isUpperCase(ele.charAt(0))) {
                                             hasLetter = false;
                                             break;
                                          }

                                          for(int j = 0; j < ele.length(); ++j) {
                                             if (ele.charAt(j) >= 128) {
                                                hasLetter = false;
                                                break;
                                             }
                                          }
                                       }

                                       if (hasLetter) {
                                          tokens = recursive((List)tokens, token, matcher.start(), matcher.end());
                                          break label139;
                                       }
                                    }

                                    ++i;
                                    continue;
                                 }

                                 matching = true;
                                 break;
                              }

                              if (!matching) {
                                 ((List)tokens).add(token);
                              }
                           }
                        }
                     }
                  }
               } else {
                  ((List)tokens).add(token);
               }
            }

            return (List)tokens;
         }
      } else {
         return new ArrayList();
      }
   }

   private static List<String> recursive(List<String> tokens, String token, int beginMatch, int endMatch) throws IOException {
      if (beginMatch > 0) {
         tokens.addAll(tokenize(token.substring(0, beginMatch)));
      }

      tokens.addAll(tokenize(token.substring(beginMatch, endMatch)));
      if (endMatch < token.length()) {
         tokens.addAll(tokenize(token.substring(endMatch)));
      }

      return tokens;
   }

   public static List<String> joinSentences(List<String> tokens) {
      List<String> sentences = new ArrayList();
      List<String> sentence = new ArrayList();

      for(int i = 0; i < tokens.size(); ++i) {
         String token = (String)tokens.get(i);
         String nextToken = null;
         if (i != tokens.size() - 1) {
            nextToken = (String)tokens.get(i + 1);
         }

         String beforeToken = null;
         if (i > 0) {
            beforeToken = (String)tokens.get(i - 1);
         }

         sentence.add(token);
         if (i == tokens.size() - 1) {
            sentences.add(joinSentence(sentence));
            return sentences;
         }

         if (i < tokens.size() - 2 && token.equals(":") && (Character.isDigit(nextToken.charAt(0)) && ((String)tokens.get(i + 2)).equals(".") || ((String)tokens.get(i + 2)).equals(","))) {
            sentences.add(joinSentence(sentence));
            sentence.clear();
         } else if (token.matches("(\\.+|\\?|!|…)") && !StringUtils.isBrace(nextToken) && !nextToken.isEmpty() && !Character.isLowerCase(nextToken.charAt(0)) && !nextToken.equals(",") && !Character.isDigit(nextToken.charAt(0)) && (sentence.size() != 2 || !token.equals(".") || !Character.isDigit(beforeToken.charAt(0)) && !Character.isLowerCase(beforeToken.charAt(0)) && (!Character.isUpperCase(beforeToken.charAt(0)) || beforeToken.length() != 1))) {
            sentences.add(joinSentence(sentence));
            sentence.clear();
         }
      }

      return sentences;
   }

   public static String joinSentence(List<String> tokens) {
      StringBuffer sent = new StringBuffer();
      int length = tokens.size();

      for(int i = 0; i < length; ++i) {
         String token = (String)tokens.get(i);
         if (!token.isEmpty() && token != null && !token.equals(" ")) {
            sent.append(token);
            if (i < length - 1) {
               sent.append(" ");
            }
         }
      }

      return sent.toString().trim();
   }
}
