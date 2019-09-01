package mainPackage;

public class MainClass {
    public static void main(String[] args) {
        MainClass mainClass = new MainClass();
        mainClass.method1();
    }
    private void method1() {
        for(int i=1;i<=3;i++) {
            this.method2();
        }
    }
    private void method2() {
        for(int i=1;i<=4;i++) {
            this.method3();
        }
    }
    private void method3() {

    }
    private void method4() {

    }
    private void method5() {

    }
}
