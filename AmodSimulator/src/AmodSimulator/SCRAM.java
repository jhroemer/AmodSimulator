package AmodSimulator;

import org.graphstream.graph.Edge;

import java.util.ArrayList;
import java.util.List;

/**
 * Based on MMD+MSD^2 algorithm by MacAlpine etc...
 */
public class SCRAM {
    List<Edge> allowedEdges;
    List<Vehicle> matchedAgents;

    public SCRAM() {
        allowedEdges = new ArrayList<>();
        matchedAgents = new ArrayList<>();
    }

    public List<Assignment> match(List<Vehicle> vehicles, List<Request> requests) {
        List<Edge> edges = new ArrayList<>();
        Edge longestEdge = getMiniMalMaxEdgeInPerfectMatching(edges);
        int longestEdgeWeight = longestEdge.getAttribute("layout.weight");
        List<Edge> minimalEdges = new ArrayList<>();
        for (Edge e : edges) if ((int) e.getAttribute("layout.weight") < longestEdgeWeight) minimalEdges.add(e);
        return hungarian(minimalEdges); // todo : use approach from Utility.hungarian()?
    }

    /**
     * big ass complicated method
     * @param edges ..
     * @return ..
     */
    private Edge getMiniMalMaxEdgeInPerfectMatching(List<Edge> edges) {
        return new Edge;
    }

}
