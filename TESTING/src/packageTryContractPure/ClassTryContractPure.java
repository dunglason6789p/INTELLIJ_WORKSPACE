package packageTryContractPure;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class ClassTryContractPure {
    public static void main(String[] args) {
        var main = new ClassTryContractPure();
        main.init();
        main.doTheTest();
    }
    private List<Student> publicList;
    private void init() {
        publicList = new ArrayList<Student>();
        for(int i=0;i<10;i++) {
            publicList.add(new Student(i,"Name"+i,i));
        }
    }
    private void doTheTest() {
        System.out.println(adder(1,2));
        System.out.println("adder:"+method1(100));
        //System.out.println("age:"+method2(publicList.get(0)).age+" -- id:"+publicList.get(0).id);
        var student = publicList.get(0);
        student = method2(student);
        System.out.println(student.age+" : "+student.id);
    }

    @Contract(pure = true)
    private Integer method1(Integer intInput) {
        var anything = publicList.remove(0);
        System.out.println(anything);
        return intInput+1;
    }
    @Contract(pure = true)
    private Student method2(Student student) {
        student.age+=1;
        return student;
    }
    class Student {
        public int id;
        public String name;
        public int age;

        //@Contract(pure = true)
        public Student(int id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }

    public void doNothing() {

    }

    private int adder(int x, int y) {
        return x+y;
    }
}
