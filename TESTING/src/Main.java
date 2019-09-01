import java.util.*;

public class Main {

    protected Set<Integer>[] adjList;
    /**[width*height][width*height] because this is adjacency matrix !!!*/
    protected int[][] adjMatrix;
    /**For quick query only!*/
    private Set<Integer> neighborSet;

    /**Num of columns*/
    private int width;
    /**Num of rows*/
    private int height;
    private static final int MAX_NUM = 1000000;

    private int calRowOf(int i) {
        if (i<0) return -1;
        if (i>=width*height) return MAX_NUM+1;
        return i/width;
    }

    private int calColOf(int i) {
        if (i<0) return MAX_NUM+1;
        if (i>=width*height) return -1;
        return i%width;
    }

    public static void main(String[] args) {
        Main main = new Main();

        main.init(6,6);
        main.hello();
        main.run();
        main.printResult();
        main.test_kHopNeighbor(20,2);
        main.hello();

        main.printAdjacencyList();
    }

    public void hello() {
        for (int count=0; count<width*height; count++) {
            System.out.print(count);
            if (neighborSet.contains(count)) {
                System.out.print("_");
            } else {
                System.out.print(" ");
            }
            if (count<10) {
                System.out.print(" ");
            }
            if (count<100) {
                System.out.print(" ");
            }
            if (count%width == width-1) {
                System.out.println();
            }
        }
    }

    public void init(int width_param, int height_param) {
        this.width = width_param;
        this.height = height_param;

        this.adjMatrix = new int[width*height][width*height];
        this.neighborSet = new HashSet<>();

        adjList = new Set[width*height];
//        for(Set item : adjList) {
//            item = new HashSet();
//        }
//        for(Set item : adjList) {
//            System.out.println(item);
//        }
        for(int i = 0; i< adjList.length; i++) {
            adjList[i] = new HashSet<>();
        }
//        for(Set item : adjList) {
//            System.out.println(item);
//        }
    }

    public void run() {
        for (int i=0; i<width*height; i++) {
            int row = calRowOf(i);
            int col = calColOf(i);
//            if (!(calRowOf(i-1) < row)) {
//                makeUndirectedEdge(i,i-1);
//            }
//            if (!(calRowOf(i+1) > row)) {
//                makeUndirectedEdge(i,i+1);
//            }
//            if (!(calColOf(i-width) > col)) {
//                makeUndirectedEdge(i,i-1);
//            }
//            if (!(calColOf(i+width) < col)) {
//                makeUndirectedEdge(i,i+1);
//            }
            if (!isOnLeftEdge(i)) {
                makeUndirectedEdge(i,i-1);
            }
            if (!isOnRightEdge(i)) {
                makeUndirectedEdge(i,i+1);
            }
            if (!isOnUpperEdge(i)) {
                makeUndirectedEdge(i,i-width);
            }
            if (!isOnLowerEdge(i)) {
                makeUndirectedEdge(i,i+width);
            }
        }
    }

    private boolean isOnLeftEdge(int node) {
        if (node%width==0) return true;
        return false;
    }

    private boolean isOnRightEdge(int node) {
        if (node%width==width-1) return true;
        return false;
    }

    private boolean isOnUpperEdge(int node) {
        if (node-width<0) return true;
        return false;
    }

    private boolean isOnLowerEdge(int node) {
        if (node+width>=width*height) return true;
        return false;
    }

    private boolean isOutOfBound(int node) {
        if (node<0) return true;
        if (node>=width*height) return true;
        return false;
    }

    private void printResult() {
        for(int i=0;i<width*height;i++) { //for each row of the rectangle.
            for(int j=0;j<width*height;j++) { //inside each row.
                if(j==width*height-1) { //if the last one in the row.
                    System.out.print(adjMatrix[i][j]);
                    System.out.println();
                } else {
                    System.out.print(adjMatrix[i][j]+", ");
                }
            }
        }
    }

    private void makeDirectedEdge(int source, int dest) {
        adjList[source].add(dest);
        adjMatrix[source][dest] = 1;
//        try {
//
//        } catch (Exception ex) {
//            System.exit(1);
//        }
    }

    private void makeUndirectedEdge(int source, int dest) {
        makeDirectedEdge(source, dest);
        makeDirectedEdge(dest, source);
    }

    private void printAdjacencyList() {
        System.out.println("***ADJACENCY LIST:");
        System.out.println();
        for(int i=0; i<adjList.length; i++) {
            Set<Integer> nodeSet = adjList[i];
            for(int node : nodeSet) {
                System.out.println(i+" "+node);
            }
        }
        System.out.println();
    }

    public List<List<Integer>> kHopNeighbor(int id, int k) {
        /*NTS: This is the data to return ?*/
        List<List<Integer>> neighbors = new ArrayList<List<Integer>>();
        /*NTS: */
        Queue<Integer> queue = new LinkedList<>();
        /*NTS: */
        boolean[] visited = new boolean[width*height];
        /*NTS: */
        int[] trace = new int[width*height];
        /*NTS: */
        queue.add(id);
        visited[id] = true;
        trace[id] = -1;
        for (int i = 0; i < k; i++) {
            int uNode = queue.remove();
            for (int vNode : adjList[uNode]) { /*For each adjacent node of uNode*/
                if (!visited[vNode]) { /*If not visited*/
                    visited[vNode] = true;
                    queue.add(vNode);
                    trace[vNode] = uNode;

                    int nextNode = vNode;
                    while (trace[nextNode] != id) {
                        nextNode = trace[nextNode];
                    }
                    List<Integer> info = new ArrayList<>();
                    info.add(vNode);
                    info.add(i + 1);
                    info.add(nextNode);
                    neighbors.add(info);
                    this.neighborSet.add(vNode);
                }
            }
        }
        return neighbors;
    }

    private void test_kHopNeighbor(int node, int k) {
        List<List<Integer>> result =  kHopNeighbor(node, k);
        for(List<Integer> list : result) {
//            for(Integer i : list) {
//
//            }
            System.out.println("neighbor:"+list.get(0)+"|hop:"+list.get(1)+"|firstNode:"+list.get(2));
        }
    }
}
