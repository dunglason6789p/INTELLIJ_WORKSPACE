package mainPackage;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("Duplicates")
public class ClassTry_CompletableFuture_multi_tasks_with_Executor {
    private static final long LOOP_COUNT = 10000000000L;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new ClassTry_CompletableFuture_multi_tasks_with_Executor().doTest();
    }

    private void doTest() throws InterruptedException, ExecutionException {
        Future<Integer> futureResult1 = calculateAsync(100);
        Future<Integer> futureResult2 = calculateAsync(200);
        System.out.println("(MAIN)Entered CPU intensive task.");
        for (long i=0; i<LOOP_COUNT; i++) {
            //NO-OP.
        }
        System.out.println("(MAIN)Finished CPU intensive task.");

        //Block the this (main) thread, until futureResult1 finished, and get its result.
        int result1 = futureResult1.get();
        //Block the this (main) thread, until futureResult2 finished, and get its result.
        int result2 = futureResult2.get();

        System.out.println(result1);
        System.out.println(result2);

        System.out.println("Current threads:");
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for(var thread:threadSet) {
            System.out.println(thread.getId()+" - "+thread.getName()+" - "
                    +thread.getThreadGroup()+" - "+thread.getState()+" - "+thread.isAlive());
        }
        System.out.println("-----");
        for (long i=0; i<LOOP_COUNT/5; i++) {
            //NO-OP.
        }
        System.out.println("Current threads:");
        Set<Thread> threadSet2 = Thread.getAllStackTraces().keySet();
        for(var thread:threadSet2) {
            System.out.println(thread.getId()+" - "+thread.getName()+" - "
                    +thread.getThreadGroup()+" - "+thread.getState()+" - "+thread.isAlive());
        }
        System.out.println("-----");
    }

    private Future<Integer> calculateAsync(int inputInt) throws InterruptedException {
        CompletableFuture<Integer> completableFuture
                = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
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
            //return;
        });

        return completableFuture;
    }
}
