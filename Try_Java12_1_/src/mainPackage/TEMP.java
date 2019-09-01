package mainPackage;

public class TEMP {
//    public void doTheTest() {
//        Thread t = new Thread(new MyRunnableTask());
//        t.start();
//    }
//    class MyRunnableTask implements Runnable {
//        public void run() {
//            // do stuff here
//        }
//    }


    public void doTheTestX() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // stuff here
            }
        });
        t.start();
    }


    public void doTheTest() {
        Thread t = new Thread(()->{
            // stuff here
        });
        t.start();
    }
}
