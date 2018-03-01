package SCRAM;

import AmodSimulator.Vehicle;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Hungarian {
    List<Edge> assignments;

    public Hungarian(List<Edge> edges, int n) {


        // todo : do we need to make sure that there are the same amount of vehicles and request, or does the algorithm work without this?
        // todo : dummy vertices should already have been added

        SimpleGraph<Node, Edge> graph = new SimpleGraph<>(new ClassBasedEdgeFactory<>(Edge.class), true);

        Set<Node> vehicleNodes = new HashSet<>();
        Set<Node> requestNodes = new HashSet<>();

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


        SimpleGraph<Integer, Edge> graph2 = new SimpleGraph<Integer, Edge>(new ClassBasedEdgeFactory<>(Edge.class), true);
        for (int i = 0; i < n; i++) { // n = assignments we have to make
            graph2.addVertex(i);
            //todo add all vertices to graph
            //todo add all vertices to vehicleNodes and requestNodes
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

        /*
        System.out.println("Nodes:");
        for (Node h : graph.vertexSet()) {
            System.out.println(h.getInfo());
        }

        System.out.println("Edges ");
        for (Assignment a : graph.edgeSet()) {
            System.out.println("Vehicle " + a.getVehicle().getId() + " --> Request " + a.getRequest().getId());
        }
        */

        //addDummyNodes(graph, vehicleNodes, requestNodes);

        KuhnMunkresMinimalWeightBipartitePerfectMatching<Node, Edge> hungarian = new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(graph, vehicleNodes, requestNodes);
        MatchingAlgorithm.Matching<Node, Edge> matching = hungarian.getMatching();

        Set<Edge> assignmentSet = matching.getEdges();

        assignments = new ArrayList<>();
        assignments.addAll(assignmentSet);
    }

    public List<Edge> getAssignments() {
        return assignments;
    }
}
