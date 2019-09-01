package packageTryMethodVSFunction;

import java.util.function.Function;

public class ClassMethodRetriever {
    public Function<Integer,Integer> getFunction() {
        try {
            var tryMethodVSFunction = new TryMethodVSFunction();
            return tryMethodVSFunction::method3;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
