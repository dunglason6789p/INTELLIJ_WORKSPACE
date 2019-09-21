/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.nlp.tokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vn.edu.vnu.uet.nlp.tokenizer.AbbreviationException;
import vn.edu.vnu.uet.nlp.tokenizer.Regex;
import vn.edu.vnu.uet.nlp.utils.StringUtils;

public class Tokenizer {
    public static List<String> tokenize(String s) throws IOException {
        if (s == null || s.trim().isEmpty()) {
            return new ArrayList<String>();
        }
        String[] tempTokens = s.trim().split("\\s+");
        if (tempTokens.length == 0) {
            return new ArrayList<String>();
        }
        List<String> tokens = new ArrayList<String>();
        for (String token : tempTokens) {
            if (token.length() == 1 || !StringUtils.hasPunctuation(token)) {
                tokens.add(token);
                continue;
            }
            if (token.endsWith(",")) {
                tokens.addAll(Tokenizer.tokenize(token.substring(0, token.length() - 1)));
                tokens.add(",");
                continue;
            }
            if (AbbreviationException.getAbbreviation().contains(token)) {
                tokens.add(token);
                continue;
            }
            if (token.endsWith(".") && Character.isAlphabetic(token.charAt(token.length() - 2))) {
                if (token.length() == 2 && Character.isUpperCase(token.charAt(token.length() - 2))) {
                    tokens.add(token);
                    continue;
                }
                tokens.addAll(Tokenizer.tokenize(token.substring(0, token.length() - 1)));
                tokens.add(".");
                continue;
            }
            if (AbbreviationException.getException().contains(token)) {
                tokens.add(token);
                continue;
            }
            boolean tokenContainsAbb = false;
            for (String e2 : AbbreviationException.getAbbreviation()) {
                int i = token.indexOf(e2);
                if (i < 0) continue;
                tokenContainsAbb = true;
                tokens = Tokenizer.recursive(tokens, token, i, i + e2.length());
                break;
            }
            if (tokenContainsAbb) continue;
            boolean tokenContainsExp = false;
            for (String e3 : AbbreviationException.getException()) {
                int i = token.indexOf(e3);
                if (i < 0) continue;
                tokenContainsExp = true;
                tokens = Tokenizer.recursive(tokens, token, i, i + e3.length());
                break;
            }
            if (tokenContainsExp) continue;
            List<String> regexes = Regex.getRegexList();
            boolean matching = false;
            for (String regex : regexes) {
                if (!token.matches(regex)) continue;
                tokens.add(token);
                matching = true;
                break;
            }
            if (matching) continue;
            for (int i = 0; i < regexes.size(); ++i) {
                Pattern pattern = Pattern.compile(regexes.get(i));
                Matcher matcher = pattern.matcher(token);
                if (!matcher.find()) continue;
                if (i == Regex.getRegexIndex("url")) {
                    String[] elements = token.split(Pattern.quote("."));
                    boolean hasURL = true;
                    block5 : for (String ele : elements) {
                        if (ele.length() == 1 && Character.isUpperCase(ele.charAt(0))) {
                            hasURL = false;
                            break;
                        }
                        for (int j = 0; j < ele.length(); ++j) {
                            if (ele.charAt(j) < '\u0080') continue;
                            hasURL = false;
                            continue block5;
                        }
                    }
                    if (!hasURL) continue;
                    tokens = Tokenizer.recursive(tokens, token, matcher.start(), matcher.end());
                } else if (i == Regex.getRegexIndex("month")) {
                    int start = matcher.start();
                    boolean hasLetter = false;
                    for (int j = 0; j < start; ++j) {
                        if (!Character.isLetter(token.charAt(j))) continue;
                        tokens = Tokenizer.recursive(tokens, token, matcher.start(), matcher.end());
                        hasLetter = true;
                        break;
                    }
                    if (!hasLetter) {
                        tokens.add(token);
                    }
                } else {
                    tokens = Tokenizer.recursive(tokens, token, matcher.start(), matcher.end());
                }
                matching = true;
                break;
            }
            if (matching) continue;
            tokens.add(token);
        }
        return tokens;
    }

    private static List<String> recursive(List<String> tokens, String token, int beginMatch, int endMatch) throws IOException {
        if (beginMatch > 0) {
            tokens.addAll(Tokenizer.tokenize(token.substring(0, beginMatch)));
        }
        tokens.addAll(Tokenizer.tokenize(token.substring(beginMatch, endMatch)));
        if (endMatch < token.length()) {
            tokens.addAll(Tokenizer.tokenize(token.substring(endMatch)));
        }
        return tokens;
    }

    public static List<String> joinSentences(List<String> tokens) {
        ArrayList<String> sentences = new ArrayList<String>();
        ArrayList<String> sentence = new ArrayList<String>();
        for (int i = 0; i < tokens.size(); ++i) {
            String token = tokens.get(i);
            String nextToken = null;
            if (i != tokens.size() - 1) {
                nextToken = tokens.get(i + 1);
            }
            String beforeToken = null;
            if (i > 0) {
                beforeToken = tokens.get(i - 1);
            }
            sentence.add(token);
            if (i == tokens.size() - 1) {
                sentences.add(Tokenizer.joinSentence(sentence));
                return sentences;
            }
            if (i < tokens.size() - 2 && token.equals(":") && (Character.isDigit(nextToken.charAt(0)) && tokens.get(i + 2).equals(".") || tokens.get(i + 2).equals(","))) {
                sentences.add(Tokenizer.joinSentence(sentence));
                sentence.clear();
                continue;
            }
            if (!token.matches("(\\.+|\\?|!|\u2026)") || StringUtils.isBrace(nextToken) || nextToken.isEmpty() || Character.isLowerCase(nextToken.charAt(0)) || nextToken.equals(",") || Character.isDigit(nextToken.charAt(0)) || sentence.size() == 2 && token.equals(".") && (Character.isDigit(beforeToken.charAt(0)) || Character.isLowerCase(beforeToken.charAt(0)) || Character.isUpperCase(beforeToken.charAt(0)) && beforeToken.length() == 1)) continue;
            sentences.add(Tokenizer.joinSentence(sentence));
            sentence.clear();
        }
        return sentences;
    }

    public static String joinSentence(List<String> tokens) {
        StringBuffer sent = new StringBuffer();
        int length = tokens.size();
        for (int i = 0; i < length; ++i) {
            String token = tokens.get(i);
            if (token.isEmpty() || token == null || token.equals(" ")) continue;
            sent.append(token);
            if (i >= length - 1) continue;
            sent.append(" ");
        }
        return sent.toString().trim();
    }
}

