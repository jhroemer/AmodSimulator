package SCRAM;

import AmodSimulator.Request;
import AmodSimulator.Vehicle;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Hungarian {
    private List<Edge> assignments;

    public Hungarian(List<Edge> edges, int n) {
        SimpleWeightedGraph<Node, Edge> graph = new SimpleWeightedGraph<Node, Edge>(Edge.class);

        Set<Node> vehicleNodes = new HashSet<>();
        Set<Node> requestNodes = new HashSet<>();

        // FIXME : this does not work with array-based
        for (Edge e : edges) {
            // turning the direction around if it's reversed, s.t. edge goes from veh --> req
            if (e.startNode instanceof Request || e.endNode instanceof Vehicle) {
                Node temp = e.startNode;
                e.startNode = e.endNode;
                e.endNode = temp;
            }

            graph.addVertex(e.startNode);
            graph.addVertex(e.endNode);
            graph.addEdge(e.startNode, e.endNode, e);
            graph.setEdgeWeight(e, e.getWeight()); // this has to be done, otherwise edge weight is not set!

            // TODO : check should in principle not be necessary, since we already reversed at l 24-29 s.t. Vehicle is always the startnode
            if (e.startNode instanceof Vehicle || e.endNode instanceof Request) {   // both checks have to be used because of dummy-nodes
                vehicleNodes.add(e.startNode);
                requestNodes.add(e.endNode);
            }
            else {
                vehicleNodes.add(e.endNode);
                requestNodes.add(e.startNode);
            }
        }

        KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, Edge> hungarian = new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(graph, vehicleNodes, requestNodes);
        MatchingAlgorithm.Matching<Node, Edge> matching = hungarian.getMatching();

        Set<Edge> assignmentSet = matching.getEdges();

        assignments = new ArrayList<>();
        assignments.addAll(assignmentSet);
    }

    public Hungarian(List<Edge> edges, int n, boolean isIndexBased) {
        SimpleWeightedGraph<Integer, Edge> graph = new SimpleWeightedGraph<Integer, Edge>(Edge.class);

        Set<Integer> vehicleNodes = new HashSet<>();
        Set<Integer> requestNodes = new HashSet<>();

        // FIXME : this does not work with array-based
        for (Edge e : edges) {
            graph.addVertex(e.startIndex);
            graph.addVertex(e.endIndex+n);
            graph.addEdge(e.startIndex, e.endIndex+n, e);
            graph.setEdgeWeight(e, e.getWeight()); // this has to be done, otherwise edge weight is not set!
            // todo: do I also have to set e.endIndex to +n?
            vehicleNodes.add(e.startIndex);
            requestNodes.add(e.endIndex+n);
        }

        KuhnMunkresMinimalWeightBipartitePerfectMatching<Integer, Edge> hungarian = new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(graph, vehicleNodes, requestNodes);
        MatchingAlgorithm.Matching<Integer, Edge> matching = hungarian.getMatching();

        Set<Edge> assignmentSet = matching.getEdges();

        assignments = new ArrayList<>();
        assignments.addAll(assignmentSet);
    }

    /**
     *
     * @return a list of edges that holds assigned vehicle-request pairs
     */
    public List<Edge> getAssignments() {
        return assignments;
    }
}
