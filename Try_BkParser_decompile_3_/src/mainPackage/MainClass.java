package mainPackage;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import vn.edu.hust.nlp.conll.model.CONLLToken;
import vn.edu.hust.nlp.parser.BKParser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainClass {
    public static void main(String[] args) throws Exception {
        System.out.println("Start the engine ...");
        BKParser bkParser = new BKParser();
        System.out.println("Finish BKParser(). Now testing with texts...");
        //Gson gson = new Gson();
        Gson gsoner = new GsonBuilder().setPrettyPrinting().create();
        List<String> texts = new ArrayList<>();
        texts.add("Sea Game 28 đã chứng kiến phong độ cực kỳ xuất sắc của cô gái vàng Ánh Viên.");
        texts.add("Theo ông Hiểu, hiện Trung Quốc nghỉ một tuần vào dịp quốc khánh, Việt Nam có thể tham khảo để cho người lao động nghỉ từ 2/9 đến 5/9.");
        texts.add("Như vậy, các gia đình trẻ, công nhân được tạo điều kiện đưa con đến trường vào ngày đầu năm học mới.");
        texts.add("Hiện, công trình đã hoàn thành toàn bộ đoạn trục chính dài 11,5 km, từ cầu Sài Gòn đến nút giao Đại học Quốc gia; đoạn còn lại vướng giải phóng mặt bằng do một số hộ dân chưa chịu di dời.");
        texts.add("Theo hợp đồng cũ, chủ đầu tư sẽ chuyển sang thu phí dự án mở rộng xa lộ ngay nhưng TP HCM cần xem xét, đánh giá lại.");
        texts.add("Nhiều nơi ở Trảng Bom ngập hơn một mét, đường giao thông chia cắt, người dân phải dùng thuyền di tản, sau đợt mưa lớn kết hợp thủy điện xả lũ.");
        texts.add("Theo các khảo sát, sau thời gian tốt nghiệp từ 6 tháng đến một năm, tỷ lệ sinh viên tại các trường thành viên Đại học Quốc gia TP HCM có việc làm xấp xỉ 100% - ông Chính cho biết.");
        texts.add("Bảng xếp hạng này được Tổ chức Giáo dục Quacquarelli Symonds (Vương quốc Anh) thực hiện lần đầu vào năm 2015, nhằm cung cấp thông tin chi tiết hơn về mối quan hệ giữa đại học với doanh nghiệp, sinh viên tốt nghiệp của trường.");
        texts.add("Cuộc họp an ninh quốc gia này được cho là cơ hội đầu tiên để đưa ra những quyết định về cách phản ứng của Mỹ trước vụ tấn công nhằm vào các nhà máy lọc dầu của Arab Saudi, một đồng minh quan trọng của Washington tại Trung Đông, các quan chức giấu tên Mỹ cho biết.");
        texts.add("Tuy nhiên, Bộ Quốc phòng Mỹ cũng cảnh báo Trump rằng lựa chọn quân sự nhằm vào Tehran có thể leo thang thành xung đột.");
        texts.add("Trong khi đó, phát ngôn viên Lầu Năm Góc Jonathan Rath Hoffman hôm qua cũng tuyên bố chưa có bất cứ quyết định nào liên quan đến các phương án quân sự nhằm vào Iran cho đến khi có kết luận cuối cùng về thủ phạm gây ra vụ tấn công.");
        texts.add("Hành vi của hai cựu tổng giám đốc Bảo hiểm xã hội Việt Nam Nguyễn Huy Ban, Lê Bạch Hồng bị VKS cáo buộc phạm tội Cố ý làm trái quy định của Nhà nước về quản lý kinh tế gây hậu quả nghiêm trọng.");
        texts.add("Bà Trần Thanh Thủy (nguyên chuyên viên Phòng Kế hoạch - Tổng hợp, Ban Kế hoạch - Tài chính) bị cáo buộc phạm tội Thiếu trách nhiệm gây hậu quả nghiêm trọng song lỗi vô ý nên đề nghị mức án 24-30 tháng.");
        texts.add("Cơ quan công tố cho rằng, các bị cáo đều nắm rõ, chắc quy định sử dụng Quỹ đầu tư xã hội của Bảo hiểm xã hội Việt Nam, trong đó ông Ban là chuyên gia hàng đầu, có chuyên môn cao trong bảo hiểm xã hội, nhưng cố ý làm trái quy định.");
        //List<List<CONLLToken>> tagResult = bkParser.tag(text);
        long startTime = System.nanoTime();
        List<List<List<CONLLToken>>> totalParseResult = new ArrayList<>();
        for(int i=0;i<texts.size();i++) {
            totalParseResult.add(bkParser.parse(texts.get(i)));
            //gsoner.toJson(obj);
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Parsed in : "+duration+" nanoseconds. - Average: "+(duration/texts.size())+" nanoseconds per text.");
        gsoner.toJson(totalParseResult, new FileWriter(".\\output\\out1.json"));
        System.out.println("NTS: Done!");
    }
}
