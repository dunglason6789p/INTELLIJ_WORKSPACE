package vn.edu.hust.nlp.parser;

import com.github.jcrfsuite.CrfTagger;
import com.github.jcrfsuite.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import third_party.org.chokkan.crfsuite.ItemSequence;
import vn.edu.hust.nlp.conll.model.CONLLToken;
import vn.edu.hust.nlp.preprocess.FeaturesExtraction;
import vn.edu.vnu.uet.nlp.segmenter.UETSegmenter;

public class BKParser {
   private UETSegmenter uetSegmenter;
   private FeaturesExtraction featuresExtraction;
   private CrfTagger crfTagger;
   private MaltParserService maltParserService;

   public BKParser(boolean loadUETSegmenter) {
      this.featuresExtraction = new FeaturesExtraction();
      System.out.println("Loading CRF Tagger...");
      this.crfTagger = new CrfTagger("models/modelPOS.model");
      System.out.println("Loading Malt Parser...");

      try {
         this.maltParserService = new MaltParserService();
         this.maltParserService.initializeParserModel("-c modelDependency -m parse -w models -lfi parser.log");
      } catch (MaltChainedException var3) {
         var3.printStackTrace();
      }

      if (loadUETSegmenter) {
         System.out.println("Loading UETSegmenter...");
         this.uetSegmenter = new UETSegmenter();
      }

   }

   public BKParser() {
      this(true);
   }

   public List<List<CONLLToken>> tag(String text) {
      if (this.uetSegmenter == null) {
         System.out.println("Loading UETSegmenter...");
         this.uetSegmenter = new UETSegmenter();
      }

      List<List<CONLLToken>> taggerResult = new ArrayList();
      List<String> sentences = this.segment(text);
      Iterator var4 = sentences.iterator();

      while(var4.hasNext()) {
         String sentence = (String)var4.next();
         List<CONLLToken> sentenceTag = this.tagger(sentence);
         taggerResult.add(sentenceTag);
      }

      return taggerResult;
   }

   public List<List<CONLLToken>> parse(String text) {
      if (this.uetSegmenter == null) {
         System.out.println("Loading UETSegmenter...");
         this.uetSegmenter = new UETSegmenter();
      }

      List<List<CONLLToken>> parserResult = new ArrayList();
      List<String> sentences = this.segment(text);
      Iterator var4 = sentences.iterator();

      while(var4.hasNext()) {
         String sentence = (String)var4.next();
         List<CONLLToken> sentenceParse = this.parseToCONLLToken(sentence);
         parserResult.add(sentenceParse);
      }

      return parserResult;
   }

   public List<List<CONLLToken>> tag(List<String> sentences, boolean isSegmented) {
      List<List<CONLLToken>> taggerResult = new ArrayList();
      Iterator var4;
      String sentence;
      if (!isSegmented) {
         System.out.println("Loading UETSegmenter...");
         this.uetSegmenter = new UETSegmenter();
         var4 = sentences.iterator();

         while(var4.hasNext()) {
            sentence = (String)var4.next();
            String segmentSentence = this.uetSegmenter.segment(sentence);
            List<CONLLToken> sentenceTag = this.tagger(segmentSentence);
            taggerResult.add(sentenceTag);
         }

         return taggerResult;
      } else {
         var4 = sentences.iterator();

         while(var4.hasNext()) {
            sentence = (String)var4.next();
            List<CONLLToken> sentenceTag = this.tagger(sentence);
            taggerResult.add(sentenceTag);
         }

         return taggerResult;
      }
   }

   public List<List<CONLLToken>> parse(List<String> sentences, boolean isSegmented) {
      List<List<CONLLToken>> parserResult = new ArrayList();
      Iterator var4;
      String sentence;
      if (!isSegmented) {
         if (this.uetSegmenter == null) {
            System.out.println("Loading UETSegmenter...");
            this.uetSegmenter = new UETSegmenter();
         }

         var4 = sentences.iterator();

         while(var4.hasNext()) {
            sentence = (String)var4.next();
            String segmentSentence = this.uetSegmenter.segment(sentence);
            List<CONLLToken> sentenceTag = this.parseToCONLLToken(segmentSentence);
            parserResult.add(sentenceTag);
         }

         return parserResult;
      } else {
         var4 = sentences.iterator();

         while(var4.hasNext()) {
            sentence = (String)var4.next();
            List<CONLLToken> sentenceTag = this.parseToCONLLToken(sentence);
            parserResult.add(sentenceTag);
         }

         return parserResult;
      }
   }

   public List<List<CONLLToken>> tagFile(String filePath) {
      Object tagResult = new ArrayList();

      try {
         String text = (new Scanner(new File(filePath))).useDelimiter("\\Z").next();
         tagResult = this.tag(text);
      } catch (FileNotFoundException var4) {
         var4.printStackTrace();
      }

      return (List)tagResult;
   }

   public List<List<CONLLToken>> parseFile(String filePath) {
      Object parseResult = new ArrayList();

      try {
         String text = (new Scanner(new File(filePath))).useDelimiter("\\Z").next();
         parseResult = this.parse(text);
      } catch (FileNotFoundException var4) {
         var4.printStackTrace();
      }

      return (List)parseResult;
   }

   public List<String> segment(String unsegmented_text) {
      if (this.uetSegmenter == null) {
         System.out.println("Loading UETSegmenter...");
         this.uetSegmenter = new UETSegmenter();
      }

      String[] paras = unsegmented_text.split("\n");
      List<String> result = new ArrayList();
      String[] var4 = paras;
      int var5 = paras.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String para = var4[var6];
         List<String> paraSegmented = this.uetSegmenter.segmentSentences(para);
         result.addAll(paraSegmented);
      }

      return result;
   }

   private List<CONLLToken> tagger(String sentence) {
      String[] words = sentence.split(" ");
      int i = 0;
      ItemSequence itemSequence = this.featuresExtraction.sequence2itemsequence(sentence);
      List<Pair<String, Double>> tagProbs = this.crfTagger.tag(itemSequence);
      List<CONLLToken> tokenList = new ArrayList();

      for(Iterator var7 = tagProbs.iterator(); var7.hasNext(); ++i) {
         Pair<String, Double> tagProb = (Pair)var7.next();
         String prediction = (String)tagProb.first;
         int index = i + 1;
         String form = words[i];
         String uposTag = prediction;
         if (prediction.equals("NML") || prediction.equals("PFN")) {
            uposTag = "PART";
         }

         CONLLToken token = new CONLLToken(String.valueOf(index), form, "_", uposTag, prediction, "_", "_", "_", "_", "_");
         tokenList.add(token);
      }

      return tokenList;
   }

   public List<CONLLToken> parseToCONLLToken(String sentence) {
      List<CONLLToken> tokenList = this.tagger(sentence);
      int n = tokenList.size();
      String[] tokens = new String[n];

      int i;
      for(int i = 0; i < n; ++i) {
         CONLLToken token = (CONLLToken)tokenList.get(i);
         i = i + 1;
         tokens[i] = i + "\t" + token.getForm() + "\t_\t" + token.getuPOSTag() + "\t" + token.getxPOSTag() + "\t_";
      }

      ArrayList parserTokens = new ArrayList();

      try {
         String[] depedency = this.maltParserService.parseTokens(tokens);

         for(i = 0; i < depedency.length; ++i) {
            String[] parts = depedency[i].split("\t");
            CONLLToken token = new CONLLToken(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], "_", "_");
            parserTokens.add(token);
         }
      } catch (MaltChainedException var10) {
         var10.printStackTrace();
      }

      return parserTokens;
   }
}
