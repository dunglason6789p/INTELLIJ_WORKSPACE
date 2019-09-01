package mainPackage2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class ClassTry_Executor_shutdown_cpu_test {
    private static final long LOOP_COUNT = 10000000000L;//10000000000L;
    private static final int NUM_OF_THREADS_WANTED = 30;

    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    public static void main(String[] args) throws InterruptedException {
        new ClassTry_Executor_shutdown_cpu_test().doTheTest2();
    }
//    public void doTheTest1() throws InterruptedException {
//        ExecutorService es = Executors.newCachedThreadPool();
//        for(int i=0;i<5;i++)
//            es.execute(new Runnable() {
//                @Override
//                public void run() {
//
//                } /*  your task */ });
//        es.shutdown();
//        boolean finished = es.awaitTermination(1, TimeUnit.MINUTES);
//        // all tasks have finished or the time has been reached.
//    }
    public void doTheTest2() throws InterruptedException {
        System.out.println("Start the show.");
        for (int i=0;i<NUM_OF_THREADS_WANTED;i++) {
            doStartNewThread();
        }
        System.out.println("Shut down the pool now...");
        POOL.shutdown();
        System.out.println("Block main thread, and test if all thread stopped? ...");
        boolean finished = POOL.awaitTermination(1, TimeUnit.MINUTES); //Block.
        if (finished) {
            System.out.println("...All thread finished!");
        } else {
            System.out.println("...Time out!");
        }
    }
    public void doStartNewThread() {
        POOL.submit(() -> {
            System.out.println("Entered CPU intensive task."
                    //+"       THREAD:  "+Thread.currentThread().getId()
                    +"     "+Thread.currentThread().getName());
                    //+" - "+Thread.currentThread().getThreadGroup());
            for (long i=0; i<LOOP_COUNT; i++) {
                //NO-OP.
            }
            System.out.println("---FINISHED !!!    "
                    //+"       THREAD:  "+Thread.currentThread().getId()
                    +"     "+Thread.currentThread().getName());
        });
    }
}
