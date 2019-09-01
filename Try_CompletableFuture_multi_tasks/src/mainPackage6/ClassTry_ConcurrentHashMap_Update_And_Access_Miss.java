package mainPackage6;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassTry_ConcurrentHashMap_Update_And_Access_Miss {
    public static void main(String[] args) {
        var main = new ClassTry_ConcurrentHashMap_Update_And_Access_Miss();
        main.init();
        main.doTheTest();
    }
    private void init() {

    }
    private volatile int count;
    private synchronized int incrementedCount() {
        count++;
        return count;
    }
    private void doTheTest() {
        var conHash = new ConcurrentHashMap<Integer,Integer>();
        var POOL = Executors.newCachedThreadPool();
        for(int i=0;i<30;i++) {
            POOL.submit(()->{
                int tempCount = count;
                conHash.put(incrementedCount(),0);
                System.out.println(conHash.containsKey(tempCount+1));
            });
        }
        POOL.shutdown();
    }
}
