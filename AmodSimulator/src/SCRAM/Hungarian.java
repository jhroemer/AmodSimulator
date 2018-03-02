package SCRAM;

import AmodSimulator.Request;
import AmodSimulator.Vehicle;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Hungarian {
    private List<Edge> assignments;

    public Hungarian(List<Edge> edges, int n) {
        SimpleGraph<Node, Edge> graph = new SimpleGraph<>(Edge.class);

        Set<Node> vehicleNodes = new HashSet<>();
        Set<Node> requestNodes = new HashSet<>();

        for (Edge e : edges) {
            if (e.startNode instanceof Request) {
                Node temp = e.startNode;
                e.startNode = e.endNode;
                e.endNode = temp;
            }
        }

        // FIXME : this does not work with array-based
        for (Edge e : edges) {
            graph.addVertex(e.startNode);
            graph.addVertex(e.endNode);
            graph.addEdge(e.startNode, e.endNode, e);

            if (e.startNode instanceof Vehicle) {
                vehicleNodes.add(e.startNode);
                requestNodes.add(e.endNode);
            }
            else {
                vehicleNodes.add(e.endNode);
                requestNodes.add(e.startNode);
            }
        }

        //todo add all edges
        /*
        for (Vehicle veh : vehicles) {
            //Node vehNode = veh.getLocation(); //vehicles current location
            graph.addVertex(veh);
            vehicleNodes.add(veh);
            for (Request req : requests) {
                //Node reqNode = req.getOrigin(); //request pick-up location
                int intWeight = veh.getLocation().getAttribute("distTo" + req.getOrigin().getId()); //distance between the two locations
                double weight = (double) intWeight;
                //System.out.println("Adding edge from " + vehNode.getId() + " to " + reqNode.getId());
                Assignment assignmentEdge = graph.addEdge(veh, req); //info to jgrapht
                graph.setEdgeWeight(assignmentEdge, weight); //info to jgrapht

                assignmentEdge.setVehicle(veh); //info to graphstream
                assignmentEdge.setRequest(req); //info to graphstream
            }
        }
        */


        KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, Edge> hungarian = new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(graph, vehicleNodes, requestNodes);
        MatchingAlgorithm.Matching<Node, Edge> matching = hungarian.getMatching();

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
