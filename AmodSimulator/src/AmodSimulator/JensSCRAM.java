//package AmodSimulator;
//
//import com.sun.tools.javac.util.Pair;
//import org.graphstream.graph.Edge;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Jens on 21/02/2018.
// */
//public class JensSCRAM {
//    // We consider a bipartite graph with n nodes on the left and right.
//
//    // Global variables used in the floodfill.
//    // foo[0][i] corresponds to i'th node on left; foo[1][i] for the right.
//    std::vector<std::vector<int> > out[2];  //adjacency list for each node      // y = allowedEdges
//    std::vector<bool> visited[2];           //reached in floodfill
//    std::vector<int> back[2];               //way to reach it
//    std::vector<int> used;                  //whether a left node is used
//    //right nodes are used if and only if out[1][j].size() == 1.
//    // todo : what's the [2]?
//
//    List<ArrayList<Integer>> out = new ArrayList<>();   // adjacency list for each node
//    List<Boolean> visited = new ArrayList<>();          // reached in floodfill
//    List<Integer> back = new ArrayList<>();             // way to reach it
//    List<Integer> used = new ArrayList<>();             //whether a left node is used
//
//    /**
//     *
//     * @param n
//     * @param k
//     * @return
//     */
//    public int getMinimalMaxEdgeInPerfectMatching(List<Edge> edges, int n, int k) {
//
//        for (int i = 0; i < 2; i++) { //Clear the graph
//            out.get(i).clear();
////            c++ shit
////            out[i].clear();
////            out[i].resize(n); // is this necessary?
//        }
//
////        std::fill(used.begin(), used.end(), 0); // fills 'used' with zero's from beginning to end?
////        reset_flooding(n);
//        resetflooding(n);
//
//        int answer;
//        for (answer = 0; answer < edges.size(); answer++) {
//
//            //
//            // java: Pair<Integer, Integer> e = edges.get(answer).second; todo : we can't just convert edge to a Pair like this..
//            std::pair<int, int> e = edges[answer].second;       // gets edge between match -> to-node
//
//            out[0][e.first].push_back(e.second);                //
//            // todo java : out.get(0).add(e.first, e.second); adds an edge from a vehicle in the matrix
//
//            //printf("Added edge: %d %d\n", e.first, e.second);
//            // todo : is this line 5-6 in pseudocode?
//            if (visited[0][e.first] && !visited[1][e.second]) { // if the from-node is visited and the to-node is not visited
//                int ans = flood(1, e.second, e.first);
//                if (ans != -1) {  //We made it to the end!
//                    if (--k == 0) break;
//                    int start = reverse(1, ans);
//                    used[start] = 1;
//                    resetflooding(n);
//                }
//            }
//        }
//        // We must use edges[answer] to push k flow with minimal max edge.
//        return answer;
//    }
//
//    // Floodfill from a node.
////  x in {0, 1}: left or right side
////  y in {0, ..., n-1}: node on side x
////  prev in {-1, 0, ..., n-1}: node on side 1-x that we came in on
////                             (-1 for unassigned node on left)
//// Returns:
////  If it reaches an unassigned right node, the index of this node.
////  Otherwise, -1.
//
//    /**
//     * Floodfill from a node.
//     * x in {0, 1}: left or right side
//     * y in {0, ..., n-1}: node on side x
//     * prev in {-1, 0, ..., n-1}: node on side 1-x that we came in on
//     * (-1 for unassigned node on left)
//     *
//     * Returns:
//     * If it reaches an unassigned right node, the index of this node.
//     * Otherwise, -1.
//     * @param x
//     * @param y
//     * @param prev
//     * @return
//     */
//    private int flood(int x, int y, int prev) {
//        visited[x][y] = 1;      // visited.get(x).add(y, 1);
//        back[x][y] = prev;      // back.get(x).add(y, prev);
//        // todo : this is line 5-6, if node is a position (x=1) and there is no out-edge from y, then return y
//        if (x == 1 && out[x][y].size() == 0) //reached an unassigned right node!
//            return y;
//
//        for (int j = 0; j < out[x][y].size(); j++) {
//            if (!visited[1-x][out[x][y][j]]) {
//                int tmp = flood(1-x, out[x][y][j], y);
//                if (tmp != -1) //Flood reached the end
//                    return tmp;
//            }
//        }
//        return -1;
//    }
//
//    // Set visited to 0 and flood from unassigned left nodes.
//    public void resetflooding(int n) {
//        for(int i = 0; i < 2; i++) {
//            // TODO : I'm not sure I get the visited[2] thing, it's a vector of booleans but also with nested vectors?
//            // std::fill assigns the given value to the elements in the range [first, last).
//            // std::fill (visited[i].begin(), visited[i].end(), 0);
//        }
//        for(int i = 0; i < n; i++) {
//            if (!used[i])
//                flood(0, i, -1);
//        }
//    }
//
//    /**
//     * starting at node (x, y), follow the back pointers and reverse each edge.
//     * Return the last node reached (i.e., the newly assigned left node)
//     */
//    private int reverse(int x, int y) {
//        while (true) {
//            int prev = back[x][y];          // int prev = back.get(x).get(y);
//            if (prev == -1)                 // Reached the unassigned node on the left
//                break;
//            out[x][y].push_back(prev);      // out.get(x).add(y, prev);
//            VECREMOVE(out[1-x][prev], y);   // VECREMOVE = ??
//            x = 1-x; y = prev;
//        }
//        return y;
//    }
//}
