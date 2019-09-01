package mainPackage3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class ClassTry_Test_volatile {
    private static final int LOOP_COUNT = 10000;
    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    private volatile int count;
    private synchronized int getCount() {
        //System.out.println(count);
        return count;
    }
    private synchronized void setCount(int newCount) {
        System.out.println(count);
        this.count = newCount;
    }
    public static void main(String[] args) {
        new ClassTry_Test_volatile().doTheTest();
    }
    public void doTheTest() {
        try {
            for (int i=0; i<LOOP_COUNT; i++) {
                //setCount(getCount()+1);
                POOL.submit(()->{
                    setCount(getCount()+1);
                });
            }
        } catch (RejectedExecutionException rejectedException) {
            System.out.println(rejectedException.getMessage());
            rejectedException.printStackTrace();
        }

        POOL.shutdown();
        try {
            POOL.awaitTermination(7, TimeUnit.DAYS);
            System.out.println("final : "+count);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
