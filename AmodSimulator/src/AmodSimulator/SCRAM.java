package AmodSimulator;

import org.graphstream.graph.Edge;

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
                matchedPosition = flood(longestEdge.r, longestEdge.v);
            }
            Vehicle matchedAgent = reversePath(matchedPosition);
            unmatchedAgents.remove(matchedAgent);
            matchedAgents.add(matchedAgent);
        }
        return longestEdge;
    }

    /**
     *
     * @param r
     * @param v
     * @return
     */
    private Request flood(Request r, Vehicle v) {
        return null;
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
        Vehicle v;
        Request r;
        int weight;

        public SCRAMEdge(Vehicle v, Request r) {
            this.v = v;
            this.r = r;
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
