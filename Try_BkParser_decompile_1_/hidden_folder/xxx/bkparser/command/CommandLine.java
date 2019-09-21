package command;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import vn.edu.hust.nlp.conll.model.CONLLToken;
import vn.edu.hust.nlp.parser.BKParser;

public class CommandLine {
   public CommandLine() {
   }

   public static void main(String[] args) throws IOException {
      if (args.length == 0) {
         help();
      } else {
         String arg1 = args[0];
         if (arg1.equals("--help") && args.length == 1) {
            help();
         } else if (arg1.equals("-r")) {
            int length = args.length;
            String path;
            List tokenList;
            Iterator var8;
            if (length == 4) {
               BKParser parser;
               List result;
               Iterator var6;
               CONLLToken token;
               if (args[2].equals("-t")) {
                  path = args[3];
                  parser = new BKParser();
                  if (args[1].equals("tag")) {
                     result = parser.tag(path);
                     var6 = result.iterator();

                     while(var6.hasNext()) {
                        tokenList = (List)var6.next();
                        var8 = tokenList.iterator();

                        while(var8.hasNext()) {
                           token = (CONLLToken)var8.next();
                           System.out.println(token.toString());
                        }

                        System.out.println();
                     }
                  } else if (args[1].equals("parse")) {
                     result = parser.parse(path);
                     var6 = result.iterator();

                     while(var6.hasNext()) {
                        tokenList = (List)var6.next();
                        var8 = tokenList.iterator();

                        while(var8.hasNext()) {
                           token = (CONLLToken)var8.next();
                           System.out.println(token.toString());
                        }

                        System.out.println();
                     }
                  } else {
                     invalidCommand();
                  }
               } else if (args[2].equals("-i")) {
                  path = args[3];
                  parser = new BKParser();
                  if (args[1].equals("tag")) {
                     result = parser.tagFile(path);
                     var6 = result.iterator();

                     while(var6.hasNext()) {
                        tokenList = (List)var6.next();
                        var8 = tokenList.iterator();

                        while(var8.hasNext()) {
                           token = (CONLLToken)var8.next();
                           System.out.println(token.toString());
                        }

                        System.out.println();
                     }
                  } else if (args[1].equals("parse")) {
                     result = parser.parseFile(path);
                     var6 = result.iterator();

                     while(var6.hasNext()) {
                        tokenList = (List)var6.next();
                        var8 = tokenList.iterator();

                        while(var8.hasNext()) {
                           token = (CONLLToken)var8.next();
                           System.out.println(token.toString());
                        }

                        System.out.println();
                     }
                  } else {
                     invalidCommand();
                  }
               }
            } else if (length == 6) {
               if (args[2].equals("-i") && args[4].equals("-o")) {
                  path = args[3];
                  String pathOutput = args[5];
                  PrintWriter writer = new PrintWriter(new FileWriter(pathOutput));
                  BKParser parser = new BKParser();
                  Iterator var10;
                  CONLLToken token;
                  List tokenList;
                  if (args[1].equals("tag")) {
                     tokenList = parser.tagFile(path);
                     var8 = tokenList.iterator();

                     while(true) {
                        if (!var8.hasNext()) {
                           System.out.println("Done! Please check your output file");
                           break;
                        }

                        tokenList = (List)var8.next();
                        var10 = tokenList.iterator();

                        while(var10.hasNext()) {
                           token = (CONLLToken)var10.next();
                           writer.println(token.toString());
                        }

                        writer.println();
                     }
                  } else if (!args[1].equals("parse")) {
                     invalidCommand();
                  } else {
                     tokenList = parser.parseFile(path);
                     var8 = tokenList.iterator();

                     while(true) {
                        if (!var8.hasNext()) {
                           System.out.println("Done! Please check your output file");
                           break;
                        }

                        tokenList = (List)var8.next();
                        var10 = tokenList.iterator();

                        while(var10.hasNext()) {
                           token = (CONLLToken)var10.next();
                           writer.println(token.toString());
                        }

                        writer.println();
                     }
                  }

                  writer.close();
               } else {
                  invalidCommand();
               }
            } else {
               invalidCommand();
            }
         } else {
            invalidCommand();
         }
      }

   }

   public static void invalidCommand() {
      System.out.println("Invalid command, please type:\n\tjava -jar BKParser.jar --help\nto know a valid command");
   }

   public static void help() {
      System.out.println("\n* Welcome to BKParser! You need the following arguments to execute:\n");
      System.out.println(" -r <what_to_execute> {additional arguments}\n");
      System.out.println("\t-r\t:\tthe method you want to execute (required: tag|parse)\n");
      System.out.println("* Additional arguments for each method:\n");
      System.out.print("1) '-r tag' : ");
      Tagger.showHelp();
      System.out.print("2) '-r parse' : ");
      Parser.showHelp();
   }
}
