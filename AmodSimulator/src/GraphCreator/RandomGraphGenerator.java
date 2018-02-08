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
        countryGraph();
    }

    /**
     * Builds a random graph that kinda resembles a rural areas with small cities
     */
    public static void countryGraph() {
        int minCitySize = 5;
        int maxCitySize = 50;
        int minNumOfCities = 10;
        int maxNumOfCities = 20;
        double maxRoadLengthCity = 0.01;
        double maxRoadLengthNetwork = 10;
        double connectednessCity = 5.7; //svarer til 0.3 probability i algs4 udgaven
        double connectednessNetwork = 3;

        int numOfCities = (int) (Math.random() * (maxNumOfCities - minNumOfCities)) + minNumOfCities;
        List<Graph> cities = new ArrayList<>();

        //generating a graph for each city
        for (int i = 0; i < numOfCities; i++) {
            int citySize = (int) (Math.random() * (maxCitySize - minCitySize)) + minCitySize ;
            cities.add(ErdosRenyiConnectedGraph("City" + i, citySize, connectednessCity, maxRoadLengthCity));
        }

        //generating a graph for the road network between cities
        Graph roadNetwork = ErdosRenyiConnectedGraph("RoadNetwork", numOfCities, connectednessNetwork, maxRoadLengthNetwork);

        //adding cities to the network of roads
        for (int i = 0; i < numOfCities; i++) {

            //adding all nodes from city "i" to the total road network
            Graph city = cities.get(i);

            for (Node n : city) {
                roadNetwork.addNode(i + "-" + n.getId());
            }
            for (Edge e : city.getEachEdge()) {
                double weight = e.getAttribute("layout.weight");
                roadNetwork.addEdge(i + "-" + e.getId(),i + "-" + e.getSourceNode().getId(),i + "-" + e.getTargetNode().getId());
                roadNetwork.getEdge(i + "-" + e.getId()).setAttribute("layout.weight", weight);
            }

            //merging a node from the city with a node from the original road network
            Node mergeNodeNetwork = roadNetwork.getNode(i);         // node "i" on the "inter-city road network"
            Node mergeNodeCity = roadNetwork.getNode(i + "-0"); // node 0 in city "i"

            List<Edge> mergeEdges = new ArrayList<>();
            for (Edge e : mergeNodeCity.getEachEdge()) mergeEdges.add(e);

            for (Edge e : mergeEdges) {
                double weight = e.getAttribute("layout.weight");
                roadNetwork.removeEdge(e);
                roadNetwork.addEdge(e.getId(), mergeNodeNetwork.getId(), e.getTargetNode().getId());
                roadNetwork.getEdge(e.getId()).setAttribute("layout.weight", weight);
            }
            roadNetwork.removeNode(mergeNodeCity);
        }

        roadNetwork.display();

        // TODO: currently the weights from the city-clusters are null
        for (Edge edge : roadNetwork.getEdgeSet()) System.out.println("edge weight is: " + edge.getAttribute("layout.weight") + " and the nodes are: " + edge.getSourceNode() + " and: " + edge.getTargetNode());

    }

    /**
     *
     * @param name the name of the city
     * @param numVertices an int ...
     * @param degree
     * @param maxEdgeWeight
     * @return
     */
    public static Graph ErdosRenyiConnectedGraph(String name, int numVertices, double degree, double maxEdgeWeight) {
        Graph graph = new SingleGraph(name);
        RandomGenerator gen = new RandomGenerator(degree); // GraphStream's impl. of Erdos Renyi

        //setting random weights between 0 and maxEdgeWeights on all edges
        gen.setEdgeAttributesRange(0.0, maxEdgeWeight);
        gen.addEdgeAttribute("layout.weight");

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
            return ErdosRenyiConnectedGraph(name, numVertices, degree, maxEdgeWeight);
        }

        return graph;
    }
}
