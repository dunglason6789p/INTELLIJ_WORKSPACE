/*
 * Decompiled with CFR 0.146.
 */
package command;

import java.io.PrintStream;

public class Parser {
    protected static void showHelp() {
        System.out.println("Method for dependency parsing. Needed arguments:\n");
        System.out.println("-i <input_path> -o <output_path>\nor\n-t <text>\n");
        System.out.println("\t-i\t:\tpath to the input text (file) (required)");
        System.out.println("\t-o\t:\tpath to the output text (file) (required)");
        System.out.println("\t-t\t:\ttext(required)");
        System.out.println("Example:");
        System.out.println("\tjava -jar BKParser.jar -r parse -i /home/user/input.txt -o /home/user/output.txt");
        System.out.println("\tjava -jar BKParser.jar -r parse -t \"H\u00f4m nay, t\u00f4i \u0111i h\u1ecdc.\"");
        System.out.println();
    }
}

