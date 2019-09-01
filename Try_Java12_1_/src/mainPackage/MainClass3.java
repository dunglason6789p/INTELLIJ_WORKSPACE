//package mainPackage;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Future;
//
//public class MainClass3 {
//    private static final long LOOP_COUNT = 20000000000L;
//    private static int threadCount = 0;
//
//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        new MainClass3().doTest();
//    }
//
//    public void doTest() throws InterruptedException, ExecutionException {
//        Future<Integer> futureResult1 = calculateAsync(100);
//        Future<Integer> futureResult2 = calculateAsync(200);
//        System.out.println("(MAIN)Entered CPU intensive task.");
//        for (long i=0; i<LOOP_COUNT; i++) {
//            //NO-OP.
//        }
//        System.out.println("(MAIN)Finished CPU intensive task.");
//
//        int result1 = futureResult1.get();
//        int result2 = futureResult2.get();
//        System.out.println(result1);
//        System.out.println(result2);
//    }
//
//    public Future<Integer> calculateAsync(int inputInt) throws InterruptedException {
//        final int threadId = threadCount++;
//        CompletableFuture<Integer> completableFuture
//                = new CompletableFuture<>();
//
////        Executors.newCachedThreadPool().submit(() -> {
////            Thread.sleep(500);
////            completableFuture.complete("Hello");
////            return null;
////        });
//
//        System.out.println("Entered CPU intensive task. ThreadID = "+threadId);
//        for (long i=0; i<LOOP_COUNT; i++) {
//            //NO-OP.
//        }
//        System.out.println("Finished CPU intensive task. ThreadID = "+threadId);
//
//        completableFuture.complete(inputInt + 1);
//
//        return completableFuture;
//    }
//}
