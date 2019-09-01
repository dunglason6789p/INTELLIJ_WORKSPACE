package packageTryMethodVSFunction;

import java.util.function.Function;

public class ClassClient {
    public static void main(String[] args) throws Exception {
        new ClassClient().doTheTest();
    }
    public void doTheTest() {
        try { //Method
            var tryMethodVSFunction = new TryMethodVSFunction();
            int result = (int)tryMethodVSFunction
                    .getClass()
//                    .getDeclaredMethod("method2", Integer.class)  // ERROR !
                    .getDeclaredMethod("method1", Integer.class)
                    .invoke(new TryMethodVSFunction(),10);
            System.out.println(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try { //Function
            var tryMethodVSFunction = new TryMethodVSFunction();
            Function<Integer,Integer> myFunction = tryMethodVSFunction::method1;
//            Function<Integer,Integer> myFunction = tryMethodVSFunction::method2;  // ERROR !
            int result = myFunction.apply(10);
            System.out.println(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
