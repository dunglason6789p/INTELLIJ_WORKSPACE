package mainPackage2;

import java.util.concurrent.Executors;

public class TEMP {
    public static void main(String[] args) {
        new TEMP().dosth();
    }
    public void dosth() {
        Executors.newCachedThreadPool().submit(() -> {
            String thisMethodName = new Object() {
            }.getClass().getEnclosingMethod().getName();
            System.out.println("thisMethodName=" + thisMethodName);
            System.out.println(this.getClass().getName());
            //
        });
    }
}
