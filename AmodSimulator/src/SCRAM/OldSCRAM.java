package SCRAM;

import AmodSimulator.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Based on MMD+MSD^2 algorithm by MacAlpine etc...
 */
public class OldSCRAM {
    private List<Edge> assignments;
    List<Node> vehicles;
    List<Node> requests;
    List<Edge> allowedEdges;
    List<Node> matchedAgents;
    List<Edge> edges;
    List<Node> unmatchedAgents;
    int n;
    private int longestEdgeWeight;

    public OldSCRAM(List<Node> vehicles, List<Node> requests) {

        // add dummy vertices s.t. |vehicles| = |requests|
        if (vehicles.size() != requests.size()) {
            int difference = Math.abs(vehicles.size() - requests.size());
            if (vehicles.size() > requests.size()) addDummyNodes(requests, difference);
            else addDummyNodes(vehicles, difference);
        }

        n = vehicles.size();

        this.vehicles = vehicles;   // todo : remember to check for immutability
        this.requests = requests;   // todo : remember to check for immutability
        allowedEdges = new ArrayList<Edge>();
        matchedAgents = new ArrayList<>();
        edges = new ArrayList<>();
        unmatchedAgents = new ArrayList<>(vehicles);

        // 2. create edges for the bipartite matching-graph
        createMatchingEdges();

        // 3. get the minimal
        longestEdgeWeight = getMinimalMaxEdgeInPerfectMatching();

        // 4. 'remove' (set to infinity) edges that are longer than longestEdgeWeight, ensuring that they will not be included in the assignment
        for (Edge edge : edges) if (edge.getWeight() > longestEdgeWeight) edge.setWeight(Integer.MAX_VALUE);

        // 5. run hungarian on the reduced set of edges, to find a min-matching
        Hungarian hungarian = new Hungarian(edges, n);
        assignments = hungarian.getAssignments();
    }

    public int getLongestEdgeWeight() {
        return longestEdgeWeight;
    }

    /**
     *
     * @return ..
     */
    private int getMinimalMaxEdgeInPerfectMatching() {
        Collections.sort(edges);
        List<Edge> edgeQ = new ArrayList<>(edges);
        Edge longestEdge = null;

        for (int i = 0; i < n; i++) {
            resetFlood();
            Node matchedPosition = null;
            while (matchedPosition == null) { // if matchedPosition is null it means we haven't found a matching yet
                longestEdge = edgeQ.remove(0);
                allowedEdges.add(longestEdge);
                if (longestEdge.startNode.isVisited() && !longestEdge.endNode.isVisited()) {
                    matchedPosition = flood(longestEdge.endNode, longestEdge.startNode);
                }
            }
            Node matchedAgent = reversePath(matchedPosition);
            unmatchedAgents.remove(matchedAgent);
            matchedAgents.add(matchedAgent);
        }
        if (longestEdge == null) try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        longestEdgeWeight = longestEdge.weight;
        return longestEdgeWeight;
    }

    /**
     *
     * @param curNode
     * @param prevNode
     * @return returns a request that has ... to be continued..
     */
    private Node flood(Node curNode, Node prevNode) {
        curNode.setVisited(true);
        curNode.setPrevious(prevNode);

        if (requests.contains(curNode) && !isThereOutgoingAllowedEdge(curNode)) return curNode;

        for (Edge e : allowedEdges) {  // line 7-10
            if (e.startNode == curNode && !e.endNode.isVisited()) {
                Node val = flood(e.endNode, e.startNode);
                if (val != null) return val;
            }
        }
        return null; // line 11
    }

    /**
     * Checks if
     * @param curNode
     * @return
     */
    private boolean isThereOutgoingAllowedEdge(Node curNode) {
        for (Edge e : allowedEdges) if (e.startNode == curNode) return true; // possible fix: if (e.endIndex == curNode.getPrevious()) return true;
        return false;
    }

    /**
     * Resetting stuff
     */
    private void resetFlood() {

        for (int i = 0; i < vehicles.size(); i++) {
            vehicles.get(i).setVisited(false);
            vehicles.get(i).setPrevious(null); //todo This is done in the pseudocode, but is not in the c++ implemention
            requests.get(i).setVisited(false);
            requests.get(i).setPrevious(null);
        }

        for (Node veh : unmatchedAgents) {   // line 16-17
            flood(veh, null);
        }
    }

    /**
     *
     * @param matchedPosition
     * @return
     */
    private Node reversePath(Node matchedPosition) {
        Node node = matchedPosition;
        while (node.getPrevious() != null) {
            reverseEdgeDirection(node, node.getPrevious());
            node = node.getPrevious();
        }
        if (node instanceof Vehicle || node instanceof DummyNode) return node; // FIXME : problem again with dummynode
        else try {
            throw new Exception("problem with reversePath()");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void reverseEdgeDirection(Node node, Node previous) {
        // I think the problem is that we work with different edge objects

        for (Edge edge : edges) {
            // FIXME: brute-force, has to be changed e.g. saved as a field in SCRAMNodes
            if ((edge.startNode == node && edge.endNode == previous) || (edge.startNode == previous && edge.endNode == node)) {
                Node oldEnd = edge.endNode;
                edge.endNode = edge.startNode;
                edge.startNode = oldEnd;
            }
        }
    }


    /**
     *
     */
    private void createMatchingEdges() {
        for (Node veh : vehicles) {
            for (Node req : requests) {
                Edge s = new Edge(veh, req);
                edges.add(s);
                System.out.println("created edge from: " + s.getStartNode().getInfo() + " to: " + s.getEndNode().getInfo() + " with weight: " + s.getWeight());
            }
        }
    }

    private static void addDummyNodes(List<Node> smallerList, int numDummies) {
        for (int i = 0; i < numDummies; i++) {
            smallerList.add(new DummyNode());
        }
    }

    public List<Edge> getAssignments() {
        return assignments;
    }
}
