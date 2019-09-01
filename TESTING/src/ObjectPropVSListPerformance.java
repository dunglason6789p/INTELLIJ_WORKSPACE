import java.util.*;

public class ObjectPropVSListPerformance {
    public static void main(String[] args) {
        ObjectPropVSListPerformance o = new ObjectPropVSListPerformance();
        System.out.println("Starting testList() - LinkedList");
        o.testList(true);
        System.out.println("Starting testList() - ArrayList");
        o.testList(false);
        System.out.println("Starting testObject()");
        o.testObject();
        System.out.println("Starting testHashMap()");
        o.testHashMap();
    }

    private static final long NUM_OF_LOOPS = 1000000000L;

    private void testList(boolean useLinkedList) {
        List<Integer> list = new ArrayList<>();
        if (useLinkedList) {
            list = null;
            list = new LinkedList<>();
        }
        list.add(23423423);
        list.add(12452355);
        list.add(43425623);

        Date timer = new Date();
        long startTime = timer.getTime();

        for (long i=0; i<NUM_OF_LOOPS; i++) {
            int x = list.get(0);
            int y = list.get(1);
            int z = list.get(2);

            list.set(0, y);
            list.set(1, z);
            list.set(2, x);
        }

        //long endTime = timer.getTime();
        long endTime = (new Date()).getTime();
        long diffTime = endTime - startTime;

        System.out.println("Time list: "+diffTime);
    }

    private void testObject() {
        ABC abc = new ABC();
        abc.prop1 = 23423423;
        abc.prop2 = 12452355;
        abc.prop3 = 43425623;

        Date timer = new Date();
        long startTime = timer.getTime();

        for (long i=0; i<NUM_OF_LOOPS; i++) {
            int x = abc.prop1;
            int y = abc.prop2;
            int z = abc.prop3;

            abc.prop1 = y;
            abc.prop2 = z;
            abc.prop3 = x;
        }

        //long endTime = timer.getTime();
        long endTime = (new Date()).getTime();
        long diffTime = endTime - startTime;

        System.out.println("Time object: "+diffTime);
    }

    private void testHashMap() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("a", 23423423);
        map.put("b", 12452355);
        map.put("c", 43425623);

        Date timer = new Date();
        long startTime = timer.getTime();

        int x = 23423423;
        int y = 12452355;
        int z = 43425623;

        for (long i=0; i<NUM_OF_LOOPS; i++) {
//            int x = map.get("c");
//            int y = map.get("a");
//            int z = map.get("b");
//
//            map.replace("b", y);
//            map.replace("a", x);
//            map.replace("c", z);

            map.replace("b", y);
            map.replace("a", x);
            map.replace("c", z);
        }

        //long endTime = timer.getTime();
        long endTime = (new Date()).getTime();
        long diffTime = endTime - startTime;

        System.out.println("Time hashmap: "+diffTime);
    }
}

class ABC {
    public int prop1;
    public int prop2;
    public int prop3;
}
