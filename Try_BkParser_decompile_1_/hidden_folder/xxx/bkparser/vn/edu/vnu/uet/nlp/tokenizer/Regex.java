package vn.edu.vnu.uet.nlp.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class Regex {
   public static final String ELLIPSIS = "\\.{2,}";
   public static final String EMAIL = "([\\w\\d_\\.-]+)@(([\\d\\w-]+)\\.)*([\\d\\w-]+)";
   public static final String FULL_DATE = "(0?[1-9]|[12][0-9]|3[01])(\\/|-|\\.)(1[0-2]|(0?[1-9]))((\\/|-|\\.)\\d{4})";
   public static final String MONTH = "(1[0-2]|(0?[1-9]))(\\/)\\d{4}";
   public static final String DATE = "(0?[1-9]|[12][0-9]|3[01])(\\/)(1[0-2]|(0?[1-9]))";
   public static final String TIME = "(\\d\\d:\\d\\d:\\d\\d)|((0?\\d|1\\d|2[0-3])(:|h)(0?\\d|[1-5]\\d)(’|'|p|ph)?)";
   public static final String MONEY = "\\p{Sc}\\d+([\\.,]\\d+)*|\\d+([\\.,]\\d+)*\\p{Sc}";
   public static final String PHONE_NUMBER = "(\\(?\\+\\d{1,2}\\)?[\\s\\.-]?)?\\d{2,}[\\s\\.-]?\\d{3,}[\\s\\.-]?\\d{3,}";
   public static final String URL = "(((https?|ftp):\\/\\/|www\\.)[^\\s/$.?#].[^\\s]*)|(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
   public static final String NUMBER = "[-+]?\\d+([\\.,]\\d+)*";
   public static final String PUNCTUATION = ",|\\.|:|\\?|!|;|-|_|\"|'|“|”|\\||\\(|\\)|\\[|\\]|\\{|\\}|âŸ¨|âŸ©|Â«|Â»|\\\\|\\/|\\â€˜|\\â€™|\\â€œ|\\â€�|â€¦|…|‘|’|·";
   public static final String SPECIAL_CHAR = "\\~|\\@|\\#|\\^|\\&|\\*|\\+|\\-|\\â€“|<|>|\\|";
   public static final String EOS_PUNCTUATION = "(\\.+|\\?|!|…)";
   public static final String NUMBERS_EXPRESSION = "[-+]?\\d+([\\.,]\\d+)*([\\+\\-\\*\\/][-+]?\\d+([\\.,]\\d+)*)*";
   public static final String SHORT_NAME = "[\\p{Upper}]\\.([\\p{L}\\p{Upper}])*";
   public static final String ALLCAP = "[A-Z]+\\.[A-Z]+";
   private static List<String> regexes = null;
   private static List<String> regexIndex = null;

   public Regex() {
   }

   public static List<String> getRegexList() {
      if (regexes == null) {
         regexes = new ArrayList();
         regexIndex = new ArrayList();
         regexes.add("\\.{2,}");
         regexIndex.add("ELLIPSIS");
         regexes.add("([\\w\\d_\\.-]+)@(([\\d\\w-]+)\\.)*([\\d\\w-]+)");
         regexIndex.add("EMAIL");
         regexes.add("(((https?|ftp):\\/\\/|www\\.)[^\\s/$.?#].[^\\s]*)|(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
         regexIndex.add("URL");
         regexes.add("(0?[1-9]|[12][0-9]|3[01])(\\/|-|\\.)(1[0-2]|(0?[1-9]))((\\/|-|\\.)\\d{4})");
         regexIndex.add("FULL_DATE");
         regexes.add("(1[0-2]|(0?[1-9]))(\\/)\\d{4}");
         regexIndex.add("MONTH");
         regexes.add("(0?[1-9]|[12][0-9]|3[01])(\\/)(1[0-2]|(0?[1-9]))");
         regexIndex.add("DATE");
         regexes.add("(\\d\\d:\\d\\d:\\d\\d)|((0?\\d|1\\d|2[0-3])(:|h)(0?\\d|[1-5]\\d)(’|'|p|ph)?)");
         regexIndex.add("TIME");
         regexes.add("\\p{Sc}\\d+([\\.,]\\d+)*|\\d+([\\.,]\\d+)*\\p{Sc}");
         regexIndex.add("MONEY");
         regexes.add("(\\(?\\+\\d{1,2}\\)?[\\s\\.-]?)?\\d{2,}[\\s\\.-]?\\d{3,}[\\s\\.-]?\\d{3,}");
         regexIndex.add("PHONE_NUMBER");
         regexes.add("[\\p{Upper}]\\.([\\p{L}\\p{Upper}])*");
         regexIndex.add("SHORT_NAME");
         regexes.add("[-+]?\\d+([\\.,]\\d+)*([\\+\\-\\*\\/][-+]?\\d+([\\.,]\\d+)*)*");
         regexIndex.add("NUMBERS_EXPRESSION");
         regexes.add("[-+]?\\d+([\\.,]\\d+)*");
         regexIndex.add("NUMBER");
         regexes.add(",|\\.|:|\\?|!|;|-|_|\"|'|“|”|\\||\\(|\\)|\\[|\\]|\\{|\\}|âŸ¨|âŸ©|Â«|Â»|\\\\|\\/|\\â€˜|\\â€™|\\â€œ|\\â€�|â€¦|…|‘|’|·");
         regexIndex.add("PUNCTUATION");
         regexes.add("\\~|\\@|\\#|\\^|\\&|\\*|\\+|\\-|\\â€“|<|>|\\|");
         regexIndex.add("SPECIAL_CHAR");
         regexes.add("[A-Z]+\\.[A-Z]+");
         regexIndex.add("ALLCAP");
      }

      return regexes;
   }

   public static int getRegexIndex(String regex) {
      return regexIndex.indexOf(regex.toUpperCase());
   }
}
