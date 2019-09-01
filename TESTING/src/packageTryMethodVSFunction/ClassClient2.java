package packageTryMethodVSFunction;

import java.util.function.Function;

public class ClassClient2 {
    public static void main(String[] args) {
        var main = new ClassClient2();
        main.doTheTest();
    }

    /**You will see that we can invoke method without any object owning that method !*/
    private void doTheTest() {
        var functionRetriever = new ClassMethodRetriever();
        Function<Integer,Integer> myFunction = functionRetriever.getFunction();

        int result = myFunction.apply(0);
        System.out.println(result);

        for(var any:myFunction.getClass().getDeclaredMethods()) {
            System.out.println(any.getName());
        }
    }
}
