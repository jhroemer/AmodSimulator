package GraphCreator;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.generator.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomGraphGenerator {

    private static boolean DEBUG = false;

    @Test
    public static void main(String[] args) {
        // countrysideGraph();

//        Graph graph = randomGraphWithSeed("test", 3.0, 100, 2);
//        graph.display();

//        Graph doro = doroGraph(20);
//        doro.display(true);

        Graph euclid = randomEuclideanGraph(30);
        System.out.println(euclid);
        euclid.display();
    }

    private static Graph randomEuclideanGraph(long randomSeed) {
        Graph graph = new SingleGraph("random euclidean");
        BaseGenerator gen = new RandomEuclideanGenerator();
        gen.addSink(graph);
        gen.setRandomSeed(randomSeed);

        gen.begin();
        ConnectedComponents cc = new ConnectedComponents();
        cc.init(graph);

        int i = 0;
        while (i < 5 || cc.getConnectedComponentsCount() != 1) {
            cc.init(graph);
            gen.nextEvents();
            i++;
        }
        gen.end();

        return graph;
    }

    private static Graph gridGraph(long randomSeed, String name) {
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new IncompleteGridGenerator();
        gen.setRandomSeed(randomSeed);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < 40; i++) gen.nextEvents();
        gen.end();
//        graph.display();

        return graph;
    }

    private static Graph doroGraph(long randomSeed) {
        Graph graph = new SingleGraph("Dorogovtsev mendes");
        BaseGenerator gen = new DorogovtsevMendesGenerator();
        Random rand = new Random();

        gen.setRandomSeed(randomSeed);
        rand.setSeed(randomSeed);

        gen.addSink(graph);
        gen.begin();
        for(int i = 0; i < 50; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) e.setAttribute("layout.weight", rand.nextInt(20));

        return graph;
    }

    private static Graph randomGraphWithSeed(String name, double avgDegree, int numVertices, long randomSeed) {
        Graph graph = new SingleGraph(name);
        RandomGenerator gen = new RandomGenerator(avgDegree); // GraphStream's impl. of Erdos Renyi
        gen.setRandomSeed(randomSeed);

        //making the graph
        gen.addSink(graph);
        gen.begin();
//        while (graph.getNodeCount() < numVertices && gen.nextEvents());
        for (int i = 0; i < 100; i++) gen.nextEvents();
        gen.end();

        return graph;
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
