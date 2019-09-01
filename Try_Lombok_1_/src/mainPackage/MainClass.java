package mainPackage;

import lombok.Getter;
import lombok.Setter;

public class MainClass {
    public static void main(String[] args) {

    }

    public void test() {
        var student = new Student();
        student.setId(123);
    }
}

class Student {
    @Getter @Setter
    private int id;
    private String age;
}
