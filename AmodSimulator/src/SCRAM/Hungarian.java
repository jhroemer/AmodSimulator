package SCRAM;

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


        //todo do we need to make sure that there are the same amount of vehicles and request, or does the algorithm work without this?


        //MultiGraph from jgrapht with nodes and edges from graphstream:
        SimpleGraph<Node, Edge> graph = new SimpleGraph<>(new ClassBasedEdgeFactory<>(Edge.class), true);

        Set<Node> vehicleNodes = new HashSet<>();
        Set<Node> requestNodes = new HashSet<>();

        //for (Request req : requests) {
        //    graph.addVertex(req);
        //    requestNodes.add(req);
        //}

        for (int i = 0; i < n; i++) {
            //graph.addVertex(i);
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
