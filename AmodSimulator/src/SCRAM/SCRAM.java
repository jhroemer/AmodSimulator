package SCRAM;

import java.util.*;

public class SCRAM {
    // remove an element from a vector by value.
    //#define VECREMOVE(vec, v) (vec).erase(  \
    //                                        std::remove((vec).begin(), (vec).endIndex(), (v)), (vec).endIndex())

    // Edge looks like (dist, (left node, right node))
    // typedef std::pair<double, std::pair<int, int> > Edge;

// We consider a bipartite graph with n nodes on the left and right.
    int n;

// Global variables used in the floodfill.
// foo[0][i] corresponds to i'th node on left; foo[1][i] for the right.

    //std::vector<std::vector<int> > out[2];  //adjacency list for each node
    //private int[][][] out;
    private ArrayList<Integer>[][] out;

    //std::vector<bool> visited[2];           //reached in floodfill
    private boolean[][] visited;
    //private List<Boolean>[] visited;

    //std::vector<int> back[2];               //way to reach it
    private int[][] back;
    //private List<Integer>[] back;

    //std::vector<int> used;                  //whether a left node is used
    private boolean[] used;

    //right nodes are used if and only if out[1][j].size() == 1.

    private int longestEdgeWeight;


    public SCRAM(List<Node> vehicles, List<Node> requests) {
        // 1. if |vehicles| != |requests| then create dummy nodes in the smaller list s.t. |vehicles| = |requests|
        if (vehicles.size() != requests.size()) try {
            throw new Exception("SCRAM called on unequal amount of Vehicles and Requests");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (vehicles.size() != requests.size()) {
            int difference = Math.abs(vehicles.size() - requests.size());
            if (vehicles.size() > requests.size()) addDummyNodes(vehicles, difference);
            else addDummyNodes(requests, difference);
        }

        // 2. create edges for the bipartite matching-graph
        n = vehicles.size();
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                //int weight = vehicles.get(i).getLocation().getAttribute("distTo" + requests.get(j).getOrigin().getId());
                int weight = vehicles.get(i).getDistance(requests.get(j));
                edges.add(new Edge(i, j, weight));
            }
        }

        // 3. get the minimal
        longestEdgeWeight = getMinimalMaxEdgeInPerfectMatching(edges, n);

        // 4. 'remove' (set to infinity) edges that are longer than longestEdgeWeight, ensuring that they will not be included in the assignment
        for (Edge edge : edges) if (edge.getWeight() > longestEdgeWeight) edge.setWeight(Integer.MAX_VALUE);

        // 5. run hungarian on the reduced set of edges, to find a min-matching
        Hungarian hungarian = new Hungarian(edges, n);
        List<Edge> assignments = hungarian.getAssignments();
    }

    // Floodfill from a node.
    //  x in {0, 1}: left or right side
    //  y in {0, ..., n-1}: node on side x
    //  prev in {-1, 0, ..., n-1}: node on side 1-x that we came in on
    //                             (-1 for unassigned node on left)
    // Returns:
    //  If it reaches an unassigned right node, the index of this node.
    //  Otherwise, -1.
    private int flood(int x, int y, int prev){
        //visited[x][y] = 1; //currentNode.visited = true;
        visited[x][y] = true;
        //back[x][y] = prev; //currentNode.previous = prevNode
        back[x][y] = prev;
        if (x == 1 && out[x][y].size() == 0) //reached an unassigned right node!
            return y;   // todo: if it's a request and there is no outgoing node from it

        // todo: for each edge in outgoing edges
        for (int j = 0; j < out[x][y].size(); j++) {
            //if (!visited[1-x][out[x][y][j]]){
            if (!visited[1-x][out[x][y].get(j)]) { // out[x][y].get(j) = is the j'th outgoing edge of the y'th vehicle/request (depending on x = 0 or 1)
                int tmp = flood(1-x, out[x][y].get(j), y); // fixme: but is visited necessarily edge.end like in pseudo?
                if (tmp != -1) //Flood reached the endIndex
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
            //std::fill(visited[i].begin(), visited[i].endIndex(), 0);
            visited[i] = new boolean[n];

        for(int i = 0; i < n; i++)
            if(!used[i]) // todo : for unmatched agents flood(x, y, prev) - it's -1 and not null - does that change something?
                flood(0, i, -1);
    }

/*
  Add edges in order until k nodes can be matched.

  edges is a sorted vector of (dist, (left, right))

  Returns the index of the last edge added; this edge must appear.
 */


    //int getMinimalMaxEdgeInPerfectMatching(std::vector<Edge> edges, int n, int k) {
    public int getMinimalMaxEdgeInPerfectMatching(List<Edge> edges, int k) { //todo k can just be n as well? Or can we use it wisely?
        Collections.sort(edges);

        visited = new boolean[2][n];
        back = new int[2][n];
        used = new boolean[n];
        //noinspection unchecked
        out = new ArrayList[2][n];
        for (int i = 0; i < n; i++) { // fixme: isn't this done twice? also on line 156-159
            out[0][i] = new ArrayList<>();
            out[1][i] = new ArrayList<>();
        }

        //std::fill(used.begin(), used.endIndex(), 0);
        used = new boolean[n];
        //reset_flooding(n);
        reset_flooding(); // todo: resetFlooding() is run before the match-loop, which isn't how the pseudocode is

        int answer;
        for (answer = 0; answer < edges.size(); answer++) { // fixme : line 28 for-loop? in this case, then n is not |vehicles| but |edges|

            //std::pair <int, int>e = edges[longestEdgeWeight].second;       // gets edge between match -> to-node
            int edgeStart = edges.get(answer).getStartIndex();
            int edgeEnd = edges.get(answer).getEndIndex();

            //  out[0][e.first].push_back(e.second);                //
            out[0][edgeStart].add(edgeEnd); // todo : longestEdge = edgeQ.pop() ?

            //printf("Added edge: %d %d\n", e.first, e.second);
            //if (visited[0][e.first] && !visited[1][e.second]) { // if
            if (visited[0][edgeStart] && !visited[1][edgeEnd]) {    // todo: if edge starts in a vehicle and not in a request?
                //int ans = flood(1, e.second, e.first);            // todo: where is this loop in pseudo?
                int ans = flood(1, edgeEnd, edgeStart);          // todo: flood() always run with curNode = vehicle?
                if (ans != -1) {  //We made it to the endIndex!
                    if (--k == 0) break; // k is subtracted 1 each time a match is made
                    int start = reverse(1, ans);
                    //used[startIndex] = 1;
                    used[start] = true;
                    //reset_flooding(n);
                    reset_flooding();
                }
            }
        }
        // We must use edges[longestEdgeWeight] to push k flow with minimal max edge.
        return edges.get(answer).getWeight();
    }


    private static void addDummyNodes(List<Node> list, int numDummies) {
        Set<Node> dummies = new HashSet<>();
        for (int i = 0; i < numDummies; i++) {
            list.add(new DummyNode());
        }

    }

    public int getLongestEdgeWeight() {
        return longestEdgeWeight;
    }
}

