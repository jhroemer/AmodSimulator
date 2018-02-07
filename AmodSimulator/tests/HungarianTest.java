import ExternalAlgorithms.Hungarian;

/**
 *
 */
public class HungarianTest {

    public static void main(String[] args) {
        double[][] arr = {{5.0, 3.0, 11.0}, {8.0, 10.0, 9.0}, {3.0, 4.0, 5.0}};
        Hungarian hungarian = new Hungarian(arr);
    }
    // TODO should we not just find a hungarian O(3) implementation, and then just use that?
    // hungarian finds a maximum-weight matching in a bipartite graph, which is also a perfect matching
    // two sets/lists which are connected
}
