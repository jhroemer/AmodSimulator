package AmodSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CplusplusSCRAM {

    // remove an element from a vector by value.
    //#define VECREMOVE(vec, v) (vec).erase(  \
    //                                        std::remove((vec).begin(), (vec).end(), (v)), (vec).end())

    // Edge looks like (dist, (left node, right node))
    // typedef std::pair<double, std::pair<int, int> > Edge;

// We consider a bipartite graph with n nodes on the left and right.
    int n;

// Global variables used in the floodfill.
// foo[0][i] corresponds to i'th node on left; foo[1][i] for the right.

    //std::vector<std::vector<int> > out[2];  //adjacency list for each node
    //private int[][][] out;
    private List<Integer>[][] out;

    //std::vector<bool> visited[2];           //reached in floodfill
    private boolean[][] visited;
    //private List<Boolean>[] visited;

    //std::vector<int> back[2];               //way to reach it
    private int[][] back;
    //private List<Integer>[] back;

    //std::vector<int> used;                  //whether a left node is used
    private boolean[] used;

    //right nodes are used if and only if out[1][j].size() == 1.


    public CplusplusSCRAM(int n) {
        this.n = n;
        //out = new int[2][n][n];
        out = new ArrayList[2][n];
        for (int i = 0; i < n; i++) {
            out[0][i] = new ArrayList<>();
            out[1][i] = new ArrayList<>();
        }

        visited = new boolean[2][n];
        //visited = new ArrayList[2];
        //visited[0]=new ArrayList<>();
        //visited[1]=new ArrayList<>();

        back = new int[2][n];
        //back = new ArrayList[2];
        //back[0]=new ArrayList<>();
        //back[1]=new ArrayList<>();

        used = new boolean[n];
    }

    // Floodfill from a node.
    //  x in {0, 1}: left or right side
    //  y in {0, ..., n-1}: node on side x
    //  prev in {-1, 0, ..., n-1}: node on side 1-x that we came in on
    //                             (-1 for unassigned node on left)
    // Returns:
    //  If it reaches an unassigned right node, the index of this node.
    //  Otherwise, -1.
    int flood(int x, int y, int prev){
        //visited[x][y] = 1; //currentNode.visited = true;
        visited[x][y] = true;
        //back[x][y] = prev; //currentNode.previous = prevNode
        back[x][y] = prev;
        if (x == 1 && out[x][y].size() == 0) //reached an unassigned right node!
            return y;

        for(int j = 0; j < out[x][y].size(); j++){
            //if (!visited[1-x][out[x][y][j]]){
            if (!visited[1-x][out[x][y].get(j)]){
                int tmp = flood(1-x, out[x][y].get(j), y);
                if (tmp != -1) //Flood reached the end
                    return tmp;
            }
        }
        return -1;
    }

// starting at node (x, y), follow the back pointers and reverse each edge.
// Return the last node reached (i.e., the newly assigned left node)
    //inline int reverse(int x, int y){
    private int reverse(int x, int y){
        while (true) {
            int prev = back[x][y];
            if (prev == -1)       // Reached the unassigned node on the left
                break;
            //out[x][y].push_back(prev);
            out[x][y].add(prev);
            //VECREMOVE(out[1-x][prev], y);
            //out[1-x][prev].remove(y); //todo Jeg er i tvivl her, for i java fjerner det index y, men i c++ fjerner VECREMOVE måske (alle forekomster af) værdien y?
            for (int i = 0; i < out[1-x][prev].size(); i++) if (out[1-x][prev].get(i) == y) out[1-x][prev].remove(i);
            x = 1-x; y = prev;
        }
        return y;
    }


// Set visited to 0 and flood from unassigned left nodes.
    //private void reset_flooding(n){
    private void reset_flooding(){
        for(int i = 0; i < 2; i++)
            //std::fill(visited[i].begin(), visited[i].end(), 0);
            visited[i] = new boolean[n];

        for(int i = 0; i < n; i++)
            if(!used[i])
                flood(0, i, -1);
    }

/*
  Add edges in order until k nodes can be matched.

  edges is a sorted vector of (dist, (left, right))

  Returns the index of the last edge added; this edge must appear.
 */


    //int getMinimalMaxEdgeInPerfectMatching(std::vector<Edge> edges, int n, int k) {
    int getMinimalMaxEdgeInPerfectMatching(List<Edge> edges, int k) { //todo k can just be n as well?
        Collections.sort(edges);

        for (int i = 0; i < 2; i++) { //Clear the graph
            //out[i].clear();
            //out[i].resize(n);
            for (int j = 0; j < n; j++) out[i][j] = new ArrayList<>();
        }

        //std::fill(used.begin(), used.end(), 0);
        used = new boolean[n];
        //reset_flooding(n);
        reset_flooding();

        int answer;
        for (answer = 0; answer < edges.size(); answer++) {

            //std::pair <int, int>e = edges[answer].second;       // gets edge between match -> to-node
            int edgeStart = edges.get(answer).getStart();
            int edgeEnd = edges.get(answer).getEnd();

            //  out[0][e.first].push_back(e.second);                //
            out[0][edgeStart].add(edgeEnd);                //

            //printf("Added edge: %d %d\n", e.first, e.second);
            //if (visited[0][e.first] && !visited[1][e.second]) { // if
            if (visited[0][edgeStart] && !visited[1][edgeEnd]) { // if
                //int ans = flood(1, e.second, e.first);
                int ans = flood(1, edgeEnd, edgeStart);
                if (ans != -1) {  //We made it to the end!
                    if (--k == 0) break;
                    int start = reverse(1, ans);
                    //used[start] = 1;
                    used[start] = true;
                    //reset_flooding(n);
                    reset_flooding();
                }
            }
        }
        // We must use edges[answer] to push k flow with minimal max edge.
        return answer;
    }

    public static class Edge implements Comparable<Edge> {
        int weight;
        int start;
        int end;

        public Edge(int start, int end, int weight) {
            this.weight = weight;
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public int compareTo(Edge other) {
            if (this.weight < other.weight) return -1;
            else if (this.weight == other.weight) return 0;
            else return 1;
        }

        public int getWeight() {
            return weight;
        }
    }

    public static void main(String[] args) {

        CplusplusSCRAM scram = new CplusplusSCRAM(3);

        List<Edge> edges = new ArrayList<>();

        // 0 = v1
        // 1 = v2
        // 2 = v3
        // 0 = r1
        // 1 = r2
        // 2 = r3

        edges.add(new Edge(0, 0, 2));
        edges.add(new Edge(0, 1, 3));
        edges.add(new Edge(0, 2, 5));
        edges.add(new Edge(1, 0, 5));
        edges.add(new Edge(1, 1, 1));
        edges.add(new Edge(1, 2, 2));
        edges.add(new Edge(2, 0, 7));
        edges.add(new Edge(2, 1, 3));
        edges.add(new Edge(2, 2, 8));

        int edgeIndex = scram.getMinimalMaxEdgeInPerfectMatching(edges, 3); //todo k can just be n as well?

        int longestWeight = edges.get(edgeIndex).getWeight();

        System.out.println("result = " + longestWeight);
    }
}

