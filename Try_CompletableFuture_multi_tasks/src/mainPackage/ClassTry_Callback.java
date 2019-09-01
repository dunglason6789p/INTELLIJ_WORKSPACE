package mainPackage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@SuppressWarnings("Duplicates")
public class ClassTry_Callback {
    private static final long LOOP_COUNT = 20000000000L;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new ClassTry_Callback().doTest();
    }

    public void doTest() throws InterruptedException, ExecutionException {
        asyncWork(100,this::printResult);
        asyncWork(200,this::printResult);
        System.out.println("(MAIN)Entered CPU intensive task.");
        for (long i=0; i<LOOP_COUNT * 2; i++) { // Run longer to wait child thread to finish.
            //NO-OP.
        }
        System.out.println("(MAIN)Finished CPU intensive task.");
    }

    public void printResult(int x) {
        System.out.println("RESULT: "+x);
    }

    public void asyncWork(int inputInt, Consumer<Integer> callback) throws InterruptedException {

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

            callback.accept(inputInt+1);
        });


    }
}
