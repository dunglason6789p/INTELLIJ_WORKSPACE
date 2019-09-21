package mainPackage;


import vn.edu.hust.nlp.conll.model.CONLLToken;
import vn.edu.hust.nlp.parser.BKParser;

import java.util.List;

public class MainClass {
    public static void main(String[] args) {
        BKParser bkParser = new BKParser();
        String text = "Sea Game 28 đã chứng kiến phong độ cực kỳ xuất sắc của cô gái vàng Ánh Viên.";
        List<List<CONLLToken>> tagResult = bkParser.tag(text);
        List<List<CONLLToken>> parseResult = bkParser.parse(text);
        System.out.println("NTS: Done!");
    }
}
