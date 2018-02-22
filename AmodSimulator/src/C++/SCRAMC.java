
/*
// We consider a bipartite graph with n nodes on the left and right.

// Global variables used in the floodfill.
// foo[0][i] corresponds to i'th node on left; foo[1][i] for the right.

        std::vector<std::vector<int> > out[2];  //adjacency list for each node
        std::vector<bool> visited[2];           //reached in floodfill
        std::vector<int> back[2];               //way to reach it
        std::vector<int> used;                  //whether a left node is used
//right nodes are used if and only if out[1][j].size() == 1.

// Floodfill from a node.
//  x in {0, 1}: left or right side
//  y in {0, ..., n-1}: node on side x
//  prev in {-1, 0, ..., n-1}: node on side 1-x that we came in on
//                             (-1 for unassigned node on left)
// Returns:
//  If it reaches an unassigned right node, the index of this node.
//  Otherwise, -1.
        int flood(int x, int y, int prev) {
            visited[x][y] = 1;
            back[x][y] = prev;
            if (x == 1 && out[x][y].size() == 0) //reached an unassigned right node!
            return y;

            for (int j = 0; j < out[x][y].size(); j++) {
                        if (!visited[1-x][out[x][y][j]]) {
                        int tmp = flood(1-x, out[x][y][j], y);
                        if (tmp != -1) //Flood reached the end
                        return tmp;
                    }
                }
            return -1;
        }

// starting at node (x, y), follow the back pointers and reverse each edge.
// Return the last node reached (i.e., the newly assigned left node)
        inline int reverse(int x, int y) {
            while (true) {
                int prev = back[x][y];
                if (prev == -1)       // Reached the unassigned node on the left
                break;
                out[x][y].push_back(prev);
                VECREMOVE(out[1-x][prev], y);
                x = 1-x; y = prev;
            }
            return y;
        }

// Set visited to 0 and flood from unassigned left nodes.
        inline void reset_flooding(int n) {
            for(int i = 0; i < 2; i++)
            std::fill(visited[i].begin(), visited[i].end(), 0);

            for(int i = 0; i < n; i++)
            if(!used[i])
            flood(0, i, -1);
        }

/*
  Add edges in order until k nodes can be matched.

  edges is a sorted vector of (dist, (left, right))

  Returns the index of the last edge added; this edge must appear.
 */

/*
        int getMinimalMaxEdgeInPerfectMatching(std::vector<Edge> edges, int n, int k) {

            for (int i = 0; i < 2; i++) { //Clear the graph
                out[i].clear();
                out[i].resize(n);
            }

            std::fill(used.begin(), used.end(), 0);
            reset_flooding(n);

            int answer;
            for (answer = 0; answer < edges.size(); answer++) {

                std::pair<int, int> e = edges[answer].second;       // gets edge between match -> to-node

                out[0][e.first].push_back(e.second);                //

                //printf("Added edge: %d %d\n", e.first, e.second);
                if (visited[0][e.first] && !visited[1][e.second]) { // if
                    int ans = flood(1, e.second, e.first);
                    if (ans != -1) {  //We made it to the end!
                        if (--k == 0) break;
                        int start = reverse(1, ans);
                        used[start] = 1;
                        reset_flooding(n);
                    }
                }
            }
            // We must use edges[answer] to push k flow with minimal max edge.
            return answer;
        }
*/