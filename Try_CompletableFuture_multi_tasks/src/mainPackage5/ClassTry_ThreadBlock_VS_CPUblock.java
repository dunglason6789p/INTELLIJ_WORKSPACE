package mainPackage5;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class ClassTry_ThreadBlock_VS_CPUblock {
    public static void main(String[] args) throws InterruptedException {
        System.out.println(Integer.MAX_VALUE );
        var main = new ClassTry_ThreadBlock_VS_CPUblock();
        main.init();
        main.doTheTest();
    }
    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    private static final int NUM_OF_THREAD_TO_CREATE = 25;
    private static final long INTENSIVE_WORK_LOOP_COUNT = 10000000000L;//2150000000L;
    public void init() {

    }

    public void doTheTest() throws InterruptedException {
        for(int threadCount=0;threadCount<NUM_OF_THREAD_TO_CREATE;threadCount++) {
            POOL.submit(() -> {
                System.out.println("START : "+Thread.currentThread().getName());
                cpuIntensiveWork();
                System.out.println(">>>DONE INTENSIVE : "+Thread.currentThread().getName());
                blockingMethod();
                System.out.println("-------MAY FINISHED : "+Thread.currentThread().getName());
            });
        }
        System.out.println("Shut down the pool now...");
        POOL.shutdown();
        System.out.println("Block main thread, and test if all thread stopped? ...");
        boolean finished = POOL.awaitTermination(45, TimeUnit.SECONDS); //Block.
        if (finished) {
            System.out.println("...All thread finished!");
        } else {
            System.exit(-2);
//            System.out.println("...Time out!");
//            for(int i=1;i<=100;i++) {
//                System.out.println("Checking deadlock now ("+i+") ...");
//                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
//                long[] deadlockedThreadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.
//
//                if (deadlockedThreadIds != null) {
//                    ThreadInfo[] infos = bean.getThreadInfo(deadlockedThreadIds);
//
//                    int count=1;
//                    for (ThreadInfo info : infos) {
//                        System.out.println("Deadlock Info "+(count++));
//                        StackTraceElement[] stack = info.getStackTrace();
//                        // Log or store stack trace information.
//                    }
//                } else {
//                    System.out.println("No deadlock found !");
//                    System.out.println();
//                }
//
//                System.out.println("All threads info:");
//                Set<Thread> allThreads = Thread.getAllStackTraces().keySet();
//                for(var anything:allThreads) {
//                    System.out.println(anything.getName()+" -- status: "+anything.getState());
//                }
//                System.out.println("--------------------------------");
//                Thread.sleep(5000);
//            }
        }
    }
    public void doTheTest1() {
        for(int threadCount=0;threadCount<NUM_OF_THREAD_TO_CREATE;threadCount++) {
            POOL.submit(() -> {
                for(int i=0;i<5;i++) {
                    cpuIntensiveWork();
                    blockingMethod();
                }
            });
        }
    }
    public void cpuIntensiveWork() {
        for(long i=0;i<INTENSIVE_WORK_LOOP_COUNT;i++) {
            //No-op.
//            if(i%2150000==0){
//                System.out.println("i="+i+"/"+INTENSIVE_WORK_LOOP_COUNT+" !!! Thread : "+Thread.currentThread().getName());
//            }
        }
    }
    private int count;
    synchronized void blockingMethod() {
        count++;
//        if(count%2150000==0){
//            System.out.println(count+"<--count !!! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//        }
    }
}
