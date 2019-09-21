/*
 * Decompiled with CFR 0.146.
 */
package command;

import command.Parser;
import command.Tagger;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import vn.edu.hust.nlp.conll.model.CONLLToken;
import vn.edu.hust.nlp.parser.BKParser;

public class CommandLine {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            CommandLine.help();
        } else {
            String arg1 = args[0];
            if (arg1.equals("--help") && args.length == 1) {
                CommandLine.help();
            } else if (arg1.equals("-r")) {
                int length = args.length;
                if (length == 4) {
                    if (args[2].equals("-t")) {
                        String text = args[3];
                        BKParser parser = new BKParser();
                        if (args[1].equals("tag")) {
                            List<List<CONLLToken>> result = parser.tag(text);
                            for (List<CONLLToken> tokenList : result) {
                                for (CONLLToken token : tokenList) {
                                    System.out.println(token.toString());
                                }
                                System.out.println();
                            }
                        } else if (args[1].equals("parse")) {
                            List<List<CONLLToken>> result = parser.parse(text);
                            for (List<CONLLToken> tokenList : result) {
                                for (CONLLToken token : tokenList) {
                                    System.out.println(token.toString());
                                }
                                System.out.println();
                            }
                        } else {
                            CommandLine.invalidCommand();
                        }
                    } else if (args[2].equals("-i")) {
                        String path = args[3];
                        BKParser parser = new BKParser();
                        if (args[1].equals("tag")) {
                            List<List<CONLLToken>> result = parser.tagFile(path);
                            for (List<CONLLToken> tokenList : result) {
                                for (CONLLToken token : tokenList) {
                                    System.out.println(token.toString());
                                }
                                System.out.println();
                            }
                        } else if (args[1].equals("parse")) {
                            List<List<CONLLToken>> result = parser.parseFile(path);
                            for (List<CONLLToken> tokenList : result) {
                                for (CONLLToken token : tokenList) {
                                    System.out.println(token.toString());
                                }
                                System.out.println();
                            }
                        } else {
                            CommandLine.invalidCommand();
                        }
                    }
                } else if (length == 6) {
                    if (args[2].equals("-i") && args[4].equals("-o")) {
                        String pathInput = args[3];
                        String pathOutput = args[5];
                        PrintWriter writer = new PrintWriter(new FileWriter(pathOutput));
                        BKParser parser = new BKParser();
                        if (args[1].equals("tag")) {
                            List<List<CONLLToken>> result = parser.tagFile(pathInput);
                            for (List<CONLLToken> tokenList : result) {
                                for (CONLLToken token : tokenList) {
                                    writer.println(token.toString());
                                }
                                writer.println();
                            }
                            System.out.println("Done! Please check your output file");
                        } else if (args[1].equals("parse")) {
                            List<List<CONLLToken>> result = parser.parseFile(pathInput);
                            for (List<CONLLToken> tokenList : result) {
                                for (CONLLToken token : tokenList) {
                                    writer.println(token.toString());
                                }
                                writer.println();
                            }
                            System.out.println("Done! Please check your output file");
                        } else {
                            CommandLine.invalidCommand();
                        }
                        writer.close();
                    } else {
                        CommandLine.invalidCommand();
                    }
                } else {
                    CommandLine.invalidCommand();
                }
            } else {
                CommandLine.invalidCommand();
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

