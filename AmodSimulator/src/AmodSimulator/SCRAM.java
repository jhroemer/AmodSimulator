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
        List<SCRAMEdge> minimalEdges = new ArrayList<>();
        for (SCRAMEdge e : edges) if (e.weight < longestEdge.weight) minimalEdges.add(e);
        return hungarian(minimalEdges); // todo : use approach from Utility.hungarian()?
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
        SCRAMEdge longestEdge = null;
        for (int i = 0; i < n; i++) {
            resetFlood();
            Request matchedPosition = null;
            while (matchedPosition == null) { // if matchedPosition is null it means we haven't found a matching yet
                longestEdge = edges.remove(0); // todo : indexoutofboundsexception
                allowedEdges.add(longestEdge);
                matchedPosition = flood(longestEdge.end, longestEdge.start);
            }
            Vehicle matchedAgent = reversePath(matchedPosition);
            unmatchedAgents.remove(matchedAgent);
            matchedAgents.add(matchedAgent);
        }
        return longestEdge;
    }

    /**
     *
     * @param curNode
     * @param prevNode
     * @return
     */
    private Request flood(SCRAMNode curNode, SCRAMNode prevNode) {
        curNode.setVisited(true);
        curNode.setPrevious(prevNode);

        // todo : curNode instanceof does not necessarily work, refer to pseudocode again if encountering problems
        if (curNode instanceof Request && !doesRequestAppearInAllowedEdges(curNode)) return (Request) curNode;

        for (SCRAMEdge e : allowedEdges) {
            if (e.start == curNode && !e.end.isVisited()) { // fixme
                Request val = flood(e.end, e.start);
                if (val != null) return val;
            }
        }
        return null;
    }

    private boolean doesRequestAppearInAllowedEdges(SCRAMNode curNode) {
        for (SCRAMEdge e : allowedEdges) if (e.end == curNode) return true;
        return false;
    }

    /**
     *
     */
    private void resetFlood() {
//        for () {
//        }
        for (Vehicle v : unmatchedAgents) {
        }
    }

    /**
     *
     * @param matchedPosition
     * @return
     */
    private Vehicle reversePath(Request matchedPosition) {
        return null;
    }




    /**
     *
     */
    private void createMatchingEdges() {
        for (Vehicle v : vehicles) {
            for (Request r : requests) {
                SCRAMEdge s = new SCRAMEdge(v, r);
                edges.add(s);
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

        public SCRAMEdge(Vehicle v, Request r) {
            start = v;
            end = r;
            weight = Utility.getDist(v.getLocation(), r.getOrigin());
        }

        @Override
        public int compareTo(SCRAMEdge other) {
            if (this.weight < other.weight) return -1;
            else if (this.weight == other.weight) return 0;
            else return 1;
        }
    }
}
