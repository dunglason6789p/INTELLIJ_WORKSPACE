//package mainPackage;
//
//import java.util.concurrent.CompletableFuture;
//
//public class MainClass {
//    public static void main(String[] args) {
//        new MainClass().startTheShow();
//    }
//
//    public void startTheShow() {
//        CompletableFuture.supplyAsync(this::getYourId)
//                .thenApply(this::getYourName)
//                .thenAccept(this::notify);
//    }
//
//    public int getYourId() {
//        return 123;
//    }
//    public String getYourName() {
//        return "ntson";
//    }
//    public int getYourId() {
//        return 123;
//    }
//}
