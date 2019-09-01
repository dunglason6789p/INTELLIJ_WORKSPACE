package mainPackage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.ea.async.Async;

public class MainClass2 {
    private static final long LOOP_COUNT = 50000000000L;

    public static void main(String[] args) {
        Async.init();
        new MainClass2().doTestEA();
    }

    public void doTestEA() {

    }

    public Future<Integer> calculateAsync(int inputInt) throws InterruptedException {
        CompletableFuture<Integer> completableFuture
                = new CompletableFuture<>();

//        Executors.newCachedThreadPool().submit(() -> {
//            Thread.sleep(500);
//            completableFuture.complete("Hello");
//            return null;
//        });

        CompletableFuture.runAsync(()->{
            System.out.println("Entered CPU intensive task."
                    +"Current thread info:"+Thread.currentThread().getName()
                    +" - "+Thread.currentThread().getThreadGroup());
            for (long i=0; i<LOOP_COUNT; i++) {
                //NO-OP.
            }
            System.out.println("FINISHED CPU intensive task."
                    +"Current thread info:"+Thread.currentThread().getName()
                    +" - "+Thread.currentThread().getThreadGroup());
            completableFuture.complete(inputInt + 1);
        });

        return completableFuture;
    }
}
