package AmodSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Based on MMD+MSD^2 algorithm by MacAlpine etc...
 */
public class SCRAM {
    List<Vehicle> vehicles;
    List<Request> requests;
    List<SCRAMEdge> allowedEdges;
    List<Vehicle> matchedAgents;
    List<SCRAMEdge> edges;
    List<Vehicle> unmatchedAgents;
    int n;

    public SCRAM(List<Vehicle> vehicles, List<Request> requests) {
        this.vehicles = vehicles;   // todo : remember to check for immutability
        this.requests = requests;   // todo : remember to check for immutability
        allowedEdges = new ArrayList<SCRAMEdge>();
        matchedAgents = new ArrayList<>();
        edges = new ArrayList<>();
        unmatchedAgents = new ArrayList<>(vehicles);
        n = vehicles.size();        // todo : SCRAM should add dummy vertices s.t. |vehicles| = |requests|
        createMatchingEdges();
    }

    public List<Assignment> match() {
        SCRAMEdge longestEdge = getMiniMalMaxEdgeInPerfectMatching();
        System.out.println("LONGEST EDGE WEIGHT IS: " + longestEdge.weight);
        List<SCRAMEdge> minimalEdges = new ArrayList<>();
        for (SCRAMEdge e : edges) if (e.weight < longestEdge.weight) minimalEdges.add(e);
        return null;
//        return hungarian(minimalEdges); // todo : use approach from Utility.hungarian()?
    }

    private List<Assignment> hungarian(List<SCRAMEdge> minimalEdges) {
        return null;
    }


    /**
     *
     * @return ..
     */
    private SCRAMEdge getMiniMalMaxEdgeInPerfectMatching() {
        Collections.sort(edges); // corresponds to edgeQ from pseudocode
//        List<SCRAMEdge> edgeQ = new ArrayList<>(edges);   todo: this is wrong right? we don't want to have copies of the edge-objects?
        List<SCRAMEdge> edgeQ = new ArrayList<>();       // rather we wan't the actual objects so that theyre reversed in both lists
        edgeQ.addAll(edges);
        SCRAMEdge longestEdge = null;

        for (int i = 0; i < n; i++) {
            resetFlood();
            Request matchedPosition = null;
            while (matchedPosition == null) { // if matchedPosition is null it means we haven't found a matching yet
                longestEdge = edgeQ.remove(0);
                allowedEdges.add(longestEdge);
                matchedPosition = flood(longestEdge.end, longestEdge.start);
            }
            Vehicle matchedAgent = reversePath(matchedPosition);
            unmatchedAgents.remove(matchedAgent);   // FIXME : is this always up-to date?
            matchedAgents.add(matchedAgent);
        }
        System.out.println("hey");
        return longestEdge;
    }

    /**
     *
     * @param curNode
     * @param prevNode
     * @return returns a request that has ... to be continued..
     */
    private Request flood(SCRAMNode curNode, SCRAMNode prevNode) {
        curNode.setVisited(true);
        curNode.setPrevious(prevNode);

        // if curNode ∈ Positions and  ̸∃ e ∈ allowedEdges, s.t. e.start = curNode
        //    then return currentNode
        // todo : curNode instanceof does not necessarily work, refer to pseudocode again if encountering problems
        if (curNode instanceof Request && !isThereOutgoingAllowedEdge(curNode)) return (Request) curNode;

        // for each e ∈ allowedEdges, s.t. (e.start = curNode and not e.end.visited) do
        //    val := flood(e.end, e.start)
        //    if val  ̸= ∅ then
        //          return val
        for (SCRAMEdge e : allowedEdges) {  // line 7-10
            if (e.start == curNode && !e.end.isVisited()) { // fixme
                Request val = flood(e.end, e.start);        // we never get here, which might be why we have a problem
                System.out.println("what is val? " + val);
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
    private boolean isThereOutgoingAllowedEdge(SCRAMNode curNode) {
        for (SCRAMEdge e : allowedEdges) if (e.start == curNode) return true; // possible fix: if (e.end == curNode.getPrevious()) return true;
        return false;
    }

    /**
     * Resetting stuff
     */
    private void resetFlood() {
        for (Vehicle veh : vehicles) {          // line 13-15
            veh.setVisited(false);
            veh.setPrevious(null);
        }

        for (Request req : requests) {          // line 13-15
            req.setVisited(false);
            req.setPrevious(null);
        }

        for (Vehicle veh : unmatchedAgents) {   // line 16-17
            flood(veh, null);
        }
    }

    /**
     *
     * @param matchedPosition
     * @return
     */
    private Vehicle reversePath(Request matchedPosition) {
        SCRAMNode node = matchedPosition;
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

    private void reverseEdgeDirection(SCRAMNode node, SCRAMNode previous) {
        // I think the problem is that we work with different edge objects

        for (SCRAMEdge edge : edges) {
            // FIXME: brute-force, has to be changed e.g. saved as a field in SCRAMNodes
            if ((edge.start == node && edge.end == previous) || (edge.start == previous && edge.end == node)) {
                SCRAMNode oldEnd = edge.end;
                edge.end = edge.start;
                edge.start = oldEnd;
            }
        }
    }


    /**
     *
     */
    private void createMatchingEdges() {
        for (Vehicle veh : vehicles) {
            for (Request req : requests) {
                SCRAMEdge s = new SCRAMEdge(veh, req);
                edges.add(s);
                System.out.println(s.weight);
            }
        }
    }

    /**
     *
     */
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
}
