package mainPackage;

import com.ea.async.Async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class MainClass {
    private static final long LOOP_COUNT = 15000000000L;

    public static void main(String[] args) {
        Async.init();
        new MainClass().doTestEA();
    }
    public void doTestEA() {
        int resultInt = await(getProductCountFromDB());
        System.out.println("Do something else...");
        for (long i=0; i<LOOP_COUNT; i++) {
            //NO-OP.
        }
        System.out.println("Done something Else.");
        System.out.println(resultInt);
    }
    public CompletableFuture<Integer> getProductCountFromDB() {
//        System.out.println("Entered CPU intensive task.");
//        for (long i=0; i<LOOP_COUNT; i++) {
//            //NO-OP.
//        }
//        System.out.println("Finished CPU intensive task.");
//        return 123;



//        Executors.newCachedThreadPool().submit(() -> {
//            Thread.sleep(500);
//            completableFuture.complete("Hello");
//            return null;
//        });

        CompletableFuture<Integer> completableFuture
                = new CompletableFuture<>();

        System.out.println("Entered CPU intensive task.");
        for (long i=0; i<LOOP_COUNT; i++) {
            //NO-OP.
        }
        System.out.println("Finished CPU intensive task.");
        completableFuture.complete(123);

        return completableFuture;
    }
    public void doTestNonEA() {

    }
}
