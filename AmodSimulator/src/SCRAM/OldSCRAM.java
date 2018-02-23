package SCRAM;

import AmodSimulator.Request;
import AmodSimulator.Vehicle;

import java.util.*;

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
        // 1. if |vehicles| != |requests| then create dummy nodes in the smaller list s.t. |vehicles| = |requests|
        if (vehicles.size() != requests.size()) try {
            throw new Exception("SCRAM called on unequal amount of Vehicles and Requests");
        } catch (Exception e) {
            e.printStackTrace();
        }


        this.vehicles = vehicles;   // todo : remember to check for immutability
        this.requests = requests;   // todo : remember to check for immutability
        allowedEdges = new ArrayList<Edge>();
        matchedAgents = new ArrayList<>();
        edges = new ArrayList<>();
        unmatchedAgents = new ArrayList<>(vehicles);
        n = vehicles.size();        // todo : OldSCRAM should add dummy vertices s.t. |vehicles| = |requests|

        //adding dummy nodes
        if (vehicles.size() != requests.size()) {
            int difference = Math.abs(vehicles.size() - requests.size());
            if (vehicles.size() > requests.size()) addDummyNodes(vehicles, difference);
            else addDummyNodes(requests, difference);
        }

        // 2. create edges for the bipartite matching-graph
        createMatchingEdges();


        // 3. get the minimal
        longestEdgeWeight = getMinimalMaxEdgeInPerfectMatching();
        System.out.println("LONGEST EDGE WEIGHT IS: " + longestEdgeWeight);

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
        Collections.sort(edges); // corresponds to edgeQ from pseudocode
//        List<SCRAMEdge> edgeQ = new ArrayList<>(edges);   todo: this is wrong right? we don't want to have copies of the edge-objects?
        List<Edge> edgeQ = new ArrayList<>();       // rather we wan't the actual objects so that theyre reversed in both lists
        edgeQ.addAll(edges);
        Edge longestEdge = null;

        for (int i = 0; i < n; i++) {
            resetFlood();
            Request matchedPosition = null;
            while (matchedPosition == null) { // if matchedPosition is null it means we haven't found a matching yet
                longestEdge = edgeQ.remove(0);
                allowedEdges.add(longestEdge);
                if (longestEdge.startNode.isVisited() && !longestEdge.endNode.isVisited()) {
                    matchedPosition = flood(longestEdge.endNode, longestEdge.startNode);
                }
            }
            Vehicle matchedAgent = reversePath(matchedPosition);
            unmatchedAgents.remove(matchedAgent);   // FIXME : is this always up-to date?
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
    private Request flood(Node curNode, Node prevNode) {
        curNode.setVisited(true);
        curNode.setPrevious(prevNode);

        // if curNode ∈ Positions and  ̸∃ e ∈ allowedEdges, s.t. e.startIndex = curNode
        //    then return currentNode
        // todo : curNode instanceof does not necessarily work, refer to pseudocode again if encountering problems
        if (curNode instanceof Request && !isThereOutgoingAllowedEdge(curNode)) return (Request) curNode;

        // for each e ∈ allowedEdges, s.t. (e.startIndex = curNode and not e.endIndex.visited) do
        //    val := flood(e.endIndex, e.startIndex)
        //    if val  ̸= ∅ then
        //          return val
        for (Edge e : allowedEdges) {  // line 7-10
            if (e.startNode == curNode && !e.endNode.isVisited()) { // fixme
                Request val = flood(e.endNode, e.startNode);        // we never get here, which might be why we have a problem
                if (val != null) return val;
            }
        }
        return null;                        // line 11
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
    private Vehicle reversePath(Request matchedPosition) {
        Node node = matchedPosition;
        while (node.getPrevious() != null) {
            reverseEdgeDirection(node, node.getPrevious());
            node = node.getPrevious();
        }
        if (node instanceof Vehicle) return (Vehicle) node;
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
            }
        }
    }

    private static void addDummyNodes(List<Node> list, int numDummies) {
        for (int i = 0; i < numDummies; i++) {
            list.add(new DummyNode());
        }
    }


    /**
     *
     */
    /*
    private class SCRAMEdge implements Comparable<SCRAMEdge> {
        SCRAMNode start;
        SCRAMNode end;
        int weight;

        public SCRAMEdge(Vehicle veh, Request req) {
            start = veh;
            end = req;
            weight = Utility.getDist(veh.getLocation(), req.getOrigin());
        }

        @Override
        public int compareTo(SCRAMEdge other) {
            if (this.weight < other.weight) return -1;
            else if (this.weight == other.weight) return 0;
            else return 1;
        }
    }
    */
}
