package mainPackage;

import java.util.ArrayList;
import java.util.List;

public class MainClass2 {
    public static void main(String[] args) {
        List<Student> listStudents = new ArrayList<>();
        for(var student:listStudents) {
            System.out.println(student.name);
        }
        var human = new Teacher();
        var someOne = "Hey";
    }
}

class Student {
    public String name;
}

class Teacher {

}