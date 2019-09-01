package mainPackage4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassTry_AtomicInteger2 {
    private static final int LOOP_COUNT = 10000;
    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    //private volatile int count;
    private AtomicInteger count = new AtomicInteger();
    private synchronized int getCount() {
        System.out.println(count.get());
        return count.get();
    }
    private synchronized void setCount(int newCount) {
        this.count.set(newCount);// = newCount;
    }
    public static void main(String[] args) {
        new ClassTry_AtomicInteger2().doTheTest();
    }
    public void doTheTest() {
        for (int i=0; i<LOOP_COUNT; i++) {
            //setCount(getCount()+1);
            POOL.submit(()->{
                setCount(getCount()+1);
            });
        }
        POOL.shutdown();
        try {
            POOL.awaitTermination(7, TimeUnit.DAYS);
            System.out.println("final : "+count);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void dummy() {
        count.incrementAndGet();
    }
}
