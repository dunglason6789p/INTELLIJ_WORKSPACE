package packageTryLambda;

import java.util.function.Function;


/**Chứng tỏ rằng dù method được truyền đi từ chỗ này sang chỗ khác, thì nó vẫn gắn liền với object sở hữu nó.*/
public class MainClassTryLambda {
    public static void main(String[] args) {
        var xxx = new MainClassTryLambda();
        xxx.doTheTest();
    }
    public void doTheTest() {
//        getMyVoidNoArgMethod(0).run();
//        getMyVoidNoArgMethod(1).run();
//        getMyVoidNoArgMethod(2).run();
//        getMyVoidNoArgMethod(3).run();
//        getMyVoidNoArgMethod(4).run();
        var runnables = new Runnable[5];
        for(int i=0;i<5;i++) {
            runnables[i]=getMyVoidNoArgMethod(i);
        }
        for(int i=0;i<5;i++) {
            runnables[i].run();
        }
        for(int i=0;i<5;i++) {
            runnables[i].run();
        }
    }
    private Runnable getMyVoidNoArgMethod(int id) {
        try {
            var abc = new ABC(id);
            //return abc::myVoidNoArgMethod;
            Runnable runnable = abc::myVoidNoArgMethod;
            abc = null;
            for (int i=0;i<1000;i++) {
                System.gc();
            }
            return runnable;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

class ABC {
    private int id;
    public ABC(int x) {
        this.id = x;
    }

    public void myVoidNoArgMethod() {
        System.out.println("id="+this.id);
    }
}
