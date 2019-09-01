package mainPackage;

public class MainClass2 {
    public static void main(String[] args) {
        new MainClass2().method1();
    }
    public void method1() {
        method2();
    }
    public void method2() {
        method3();
        method5();
    }
    public void method3() {
        method4();
    }
    public void method4() {
        System.out.println("4");
    }
    public void method5() {
        method6();
    }
    public void method6() {
        method4();
    }
}
