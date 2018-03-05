package GraphCreator;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.generator.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.List;

public class RandomGraphGenerator {

    private static boolean DEBUG = false;

    public static void main(String[] args) {
        countrysideGraph();
    }

    /**
     * Builds a random graph that kinda resembles a rural areas with small cities
     */
    public static Graph countrysideGraph() {

        int minCitySize = 1;
        int maxCitySize = 10;

        int minNumOfCities = 5;
        int maxNumOfCities = 5;

        int minRoadLengthCity = 1;
        int maxRoadLengthCity = 10;

        int minRoadLengthNetwork = 1;
        int maxRoadLengthNetwork = 100;

        double connectednessCity = 5.7; //svarer til 0.3 probability i algs4 udgaven
        double connectednessNetwork = 3;

        int numOfCities = (int) (Math.random() * (maxNumOfCities - minNumOfCities)) + minNumOfCities;
        List<Graph> cities = new ArrayList<>();

        //generating a graph for each city
        for (int i = 0; i < numOfCities; i++) {
            int citySize = (int) (Math.random() * (maxCitySize - minCitySize)) + minCitySize ;
            cities.add(ErdosRenyiConnectedGraph("City" + i, citySize, connectednessCity, minRoadLengthCity, maxRoadLengthCity));
        }

        //generating a graph for the road network between cities
        Graph roadNetwork = ErdosRenyiConnectedGraph("RoadNetwork", numOfCities, connectednessNetwork, minRoadLengthNetwork, maxRoadLengthNetwork);

        //adding cities to the network of roads
        for (int i = 0; i < numOfCities; i++) {

            //adding all nodes from city "i" to the total road network
            Graph city = cities.get(i);

            for (Node n : city) {
                roadNetwork.addNode(i + "-" + n.getId());
            }
            for (Edge e : city.getEachEdge()) {
                int weight = e.getAttribute("layout.weight");
                roadNetwork.addEdge(i + "-" + e.getId(),i + "-" + e.getSourceNode().getId(),i + "-" + e.getTargetNode().getId());
                roadNetwork.getEdge(i + "-" + e.getId()).setAttribute("layout.weight", weight);
            }

            //merging a node from the city with a node from the original road network
            Node mergeNodeNetwork = roadNetwork.getNode(i);         // node "i" on the "inter-city road network"
            Node mergeNodeCity = roadNetwork.getNode(i + "-0"); // node 0 in city "i"

            List<Edge> mergeEdges = new ArrayList<>();
            for (Edge e : mergeNodeCity.getEachEdge()) mergeEdges.add(e);

            for (Edge e : mergeEdges) {
                int weight = e.getAttribute("layout.weight");
                roadNetwork.removeEdge(e);
                roadNetwork.addEdge(e.getId(), mergeNodeNetwork.getId(), e.getTargetNode().getId());
                roadNetwork.getEdge(e.getId()).setAttribute("layout.weight", weight);
            }
            roadNetwork.removeNode(mergeNodeCity);
        }

        //for (Node n : roadNetwork) n.setAttribute("ui.label", n.getId());
        for (Edge e : roadNetwork.getEdgeSet()) e.setAttribute("ui.label", e.getAttribute("layout.weight").toString());

        // TODO: currently the weights from the city-clusters are null -- Astrid: I think we fixed this, right?
        for (Edge edge : roadNetwork.getEdgeSet()) System.out.println("edge weight is: " + edge.getAttribute("layout.weight") + " and the nodes are: " + edge.getSourceNode() + " and: " + edge.getTargetNode());

        //setting the distances between all nodes in the graph
        Utility.setDistances(roadNetwork);

//        roadNetwork.display();
        //Utility.saveCompleteGraph("random1", roadNetwork);

        return roadNetwork;
    }

    /**
     *
     * @param name the name of the city
     * @param numVertices an int ...
     * @param degree
     * @param maxEdgeWeight
     * @return
     */
    public static Graph ErdosRenyiConnectedGraph(String name, int numVertices, double degree, int minEdgeWeight, int maxEdgeWeight) {
        Graph graph = new SingleGraph(name);
        RandomGenerator gen = new RandomGenerator(degree); // GraphStream's impl. of Erdos Renyi

        //making the graph
        gen.addSink(graph);
        gen.begin();
        while (graph.getNodeCount() < numVertices && gen.nextEvents());
        gen.end();

        //making sure the graph is connected
        ConnectedComponents cc = new ConnectedComponents();
        cc.init(graph);
        if (cc.getConnectedComponentsCount() != 1) {
            System.out.printf("Graph %s is not connected. Number of components are %d. Calculating new graph.\n", name, cc.getConnectedComponentsCount());
            return ErdosRenyiConnectedGraph(name, numVertices, degree, minEdgeWeight, maxEdgeWeight);
        }

        //setting random weights between minEdgeWeights and maxEdgeWeights on all edges
        for (Edge e : graph.getEdgeSet()) {
            int length = (int) (Math.random() * (maxEdgeWeight - minEdgeWeight)) + minEdgeWeight;
            e.setAttribute("layout.weight", length);
        }


        return graph;
    }
}
