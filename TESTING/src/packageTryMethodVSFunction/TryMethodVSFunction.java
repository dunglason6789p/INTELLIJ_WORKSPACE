package packageTryMethodVSFunction;

import java.lang.reflect.Method;
import java.util.function.Function;

public class TryMethodVSFunction {
    public static void main(String[] args) throws Exception {
        new TryMethodVSFunction().methodMain();
    }

//    public int method1(int x) {
//        return x+1;
//    }
    public Integer method1(Integer x) {
        return x+1;
    }
    private Integer method2(Integer x) {
        return x+2;
    }
    public Integer method3(Integer x) {
        return privateInt+x;
    }
    private Integer privateInt = 999;

    public void methodMain() throws Exception {
        //Method method = TryMethodVSFunction::method1;
        Class myClass = Class.forName("packageTryMethodVSFunction.TryMethodVSFunction");
        for(var oneMethod : myClass.getDeclaredMethods()) {
            System.out.println(oneMethod.getName());
        }
        System.out.println("--------------");
        for(var oneMethod : myClass.getMethods()) {
            System.out.println(oneMethod.getName());
        }
        System.out.println("--------------");

        Method myMethod = myClass.getDeclaredMethod("method1",Integer.class);
        int resultMethod = (int) myMethod.invoke(new TryMethodVSFunction(), 10);
        System.out.println(resultMethod);
        System.out.println("DONE TESTING METHOD. NOW TEST FUNCTION :");

        Function<Integer,Integer> myFunction = this::method1;
        int resultFunction = myFunction.apply(10);
        System.out.println(resultFunction);
    }
}
