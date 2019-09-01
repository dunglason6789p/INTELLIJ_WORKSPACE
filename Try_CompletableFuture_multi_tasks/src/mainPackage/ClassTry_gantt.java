package mainPackage;

import java.util.concurrent.*;

@SuppressWarnings("Duplicates")
public class ClassTry_gantt {
    private static final long LOOP_COUNT = 10000000000L;

    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    private volatile int ganttInt = 0;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new ClassTry_gantt().doTest();
    }

    private void doTest() throws InterruptedException, ExecutionException {
        System.out.println("Start the show.");
        for(int i=0;i<100;i++) {
            para();
        }
        System.out.println("Shut down the pool now...");
        POOL.shutdown();
        System.out.println("Block main thread, and test if all thread stopped? ...");
        boolean finished = POOL.awaitTermination(1, TimeUnit.MINUTES); //Block.
        if (finished) {
            System.out.println("...All thread finished!");
            System.out.println("ganttInt="+ganttInt);
        } else {
            System.out.println("...Time out!");
        }
    }

    private void para() throws InterruptedException {
//        CompletableFuture<Void> completableFuture
//                = new CompletableFuture<>();
//
//        CompletableFuture.runAsync(()->{
//            System.out.println(Thread.currentThread().getName()+" - "+Thread.currentThread().getThreadGroup());
//            ganttInt++;
//            completableFuture.complete(null);
//        });
//
//        return completableFuture;

        POOL.submit(() -> {
            System.out.println("Entered CPU intensive task."
                    +"Current thread info:"+Thread.currentThread().getName()
                    +" - "+Thread.currentThread().getThreadGroup());
            //incGanttInt();
            synchronized(this) {
                int ganttTemp = ganttInt;
                for (int i=0;i<10000;i++) {
                    //NO-OP !
                }
                ganttInt = ganttTemp + 1;
            }
            System.out.println("FINISHED CPU intensive task."
                    +"Current thread info:"+Thread.currentThread().getName()
                    +" - "+Thread.currentThread().getThreadGroup());
        });
    }

    synchronized private void incGanttInt() {
        ganttInt++;
//        int ganttTemp = ganttInt;
//        for (int i=0;i<10000;i++) {
//            //NO-OP !
//        }
//        ganttInt = ganttTemp + 1;
    }
}
