package GraphCreator;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.generator.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static GraphCreator.GraphType.*;

public class RandomGraphGenerator {

    private static boolean DEBUG = false;

    public static void main(String[] args) {

//        Graph graph = null;
//        ConnectedComponents cc = new ConnectedComponents();
//        while (graph == null || cc.getConnectedComponentsCount() != 1) {
//            graph = test();
//            cc.init(graph);
//        }
//        if (graph != null) graph.display();

//        Properties props = new Properties();
//        generateExperimentGraphs(props);

//        Graph graph = generateRandomGraph(DOROGOVTSEV, 10, 484, 5, 20, "DOROGOVTSEV_" + 1);
//        graph.display();
//        Graph graph = generateRandomGraph(BARABASI, 10, 30, 5, 20, "BARABASI_" + 1);
//        graph.display();

        /*
        for (int i = 1; i < 6; i++) {
            System.out.println("banana: " + i);
//            Graph graph = countrysideGraph(10);
            Graph graph = test2(i);
            Utility.setDistances(graph);
            Utility.saveCompleteGraph(graph.getId(), "data/graphs/chapter2/" + "BANANATREE" + "/", graph);
        }
        */
    }

    private static Graph test2(int i) {
        int lowerBound = 30;
        int upperBound = 30;
        Random rand = new Random();

        Graph graph = new SingleGraph("BANANATREE_" + i);
        BaseGenerator gen = new BananaTreeGenerator(15);
        gen.setRandomSeed(5);

        gen.addSink(graph);
        gen.begin();
        for (int j = 0; j < 20; j++) gen.nextEvents();
        gen.end();

        System.out.println("node count: " + graph.getNodeCount());

        // TODO : I need to set edge-weights in a more consistent manner
        for (Edge e : graph.getEdgeSet()) {
            if (e.getSourceNode().getId().equals("root") || e.getTargetNode().getId().equals("root")) {
                System.out.println("setting edge weight for root-edge");
                e.setAttribute("layout.weight", 250);
            }
            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
        }
        return graph;
    }

    private static Graph test() {
        Graph graph = new SingleGraph("test");
        BaseGenerator gen = new WattsStrogatzGenerator(200, 2, 0.6);
        gen.setRandomSeed(5);

        gen.addSink(graph);
        gen.begin();
        while (gen.nextEvents());
        gen.end();

        // TODO : I need to set edge-weights in a more consistent manner
//        for (Edge e : graph.getEdgeSet()) {
//            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
//        }
        return graph;
    }

    private static void generateExperimentGraphs(Properties props) {
        // TODO get params from props

        List<GraphType> types = new ArrayList<>();
        types.add(GRID);
        types.add(LOBSTER);
        types.add(BARABASI);
        types.add(DOROGOVTSEV);
        types.add(COUNTRYSIDE);
        types.add(BANANATREE);

        for (int i = 1; i < 6; i++) {
            String seedString = i + "0";
            int seedInt = Integer.valueOf(seedString);
            System.out.println("seed is: " + seedInt);

            for (GraphType type : types) {
                long start = System.currentTimeMillis();
                System.out.println("creating graph no. " + i + " of type: " + type);

//                Graph graph = generateRandomGraph(type, seedInt, 5, 20, type + "_" + i);

                Graph graph = generateGraph(type, seedInt, type + "_" + i);

                Utility.setDistances(graph); // fixme: this takes time, a graph w. 1000 nodes has to run dijkstra 1 million times
                Utility.saveCompleteGraph(graph.getId(), "data/graphs/chapter2/" + type + "/", graph);
                System.out.println("TOOK: " + (System.currentTimeMillis() - start) + "ms to create graph");
            }
        }
    }

    private static Graph generateGraph(GraphType type, int seedInt, String name) {
        switch (type) {
            case GRID:
                return createGridGraph(type, seedInt, name, 15);
            case LOBSTER:   // TODO : lobster and barabasi are quite alike, lobster seems to perform worse than barabasi
                return createLobsterGraph(type, seedInt, name, 256);
            case BARABASI:
                return createBarabasiGraph(type, seedInt, name, 256);
            case DOROGOVTSEV:
                return createDorogovtsevGraph(type, seedInt, name, 256);
            case BANANATREE:
                return createBananatreeGraph(type, seedInt, name, 20);
            case COUNTRYSIDE:
                return createCountrysideGraph(type, seedInt, name);
        }

        try {
            throw new Exception("problem with generateGraph()");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Graph createGridGraph(GraphType type, int seedInt, String name, int size) {
        int upperBound = 20;
        int lowerBound = 5;
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new GridGenerator();
        gen.setRandomSeed(seedInt);
        Random rand = new Random(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
        }

        System.out.println(type + " had: " + graph.getNodeCount() + " nodes with size: " + size);
        return graph;
    }

    private static Graph createLobsterGraph(GraphType type, int seedInt, String name, int size) {
        int upperBound = 20;
        int lowerBound = 5;
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new LobsterGenerator(2, 3);
        gen.setRandomSeed(seedInt);
        Random rand = new Random(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
        }

        return graph;
    }

    private static Graph createBarabasiGraph(GraphType type, int seedInt, String name, int size) {
        int upperBound = 20;
        int lowerBound = 5;
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new BarabasiAlbertGenerator(2);
        gen.setRandomSeed(seedInt);
        Random rand = new Random(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
        }

        return graph;
    }

    private static Graph createDorogovtsevGraph(GraphType type, int seedInt, String name, int size) {
        int upperBound = 20;
        int lowerBound = 5;
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new DorogovtsevMendesGenerator();
        gen.setRandomSeed(seedInt);
        Random rand = new Random(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
        }

        return graph;
    }

    private static Graph createBananatreeGraph(GraphType type, int seedInt, String name, int size) {
        int upperBound = 20;
        int lowerBound = 5;
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new BananaTreeGenerator(15);
        gen.setRandomSeed(seedInt);
        Random rand = new Random(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) {
            if (e.getSourceNode().getId().equals("root") || e.getTargetNode().getId().equals("root")) {
                e.setAttribute("layout.weight", 250);
            }
            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
        }

        return graph;
    }

    private static Graph createCountrysideGraph(GraphType type, int seedInt, String name) {
        return countrysideGraph(10, name);
    }

//    /**
//     *
//     * @param type
//     * @param randomSeed
//     * @param lowerBound
//     * @param upperBound
//     * @param name
//     * @return
//     */
//    private static Graph generateRandomGraph(GraphType type, long randomSeed, int lowerBound, int upperBound, String name) {
//        int size = (type == GRID) ? 15 : 256; // has to be lower for grid since it creates a lot of nodes
//
//        Graph graph = new SingleGraph(name);
//        BaseGenerator gen = null;
//        switch (type) {
//            case GRID:
//                gen = new GridGenerator();
//                break;
//            case LOBSTER:   // TODO : lobster and barabasi are quite alike, lobster seems to perform worse than barabasi
//                gen = new LobsterGenerator();
//                break;
//            case BARABASI:
//                gen = new BarabasiAlbertGenerator(2);
//                break;
//            case DOROGOVTSEV:
//                gen = new DorogovtsevMendesGenerator();
//                break;
//            case BANANATREE:
//                gen = new BananaTreeGenerator(15);
//                break;
//            case COUNTRYSIDE:
//                return countrysideGraph(10, name);
//        }
//        gen.setRandomSeed(randomSeed);
//
//        gen.addSink(graph);
//        gen.begin();
//        for (int i = 0; i < size; i++) gen.nextEvents();
//        gen.end();
//
//        setEdgeWeights(graph, type, lowerBound, upperBound, randomSeed);
//
//        System.out.println(type + " had: " + graph.getNodeCount() + " nodes with size: " + size);
//        return graph;
//    }

    private static void setEdgeWeights(Graph graph, GraphType type, int lowerBound, int upperBound, long randomSeed) {
        Random rand = new Random(randomSeed);

        if (type == COUNTRYSIDE) return; // fixme : not very pretty..
        if (type == BANANATREE) {
            for (Edge e : graph.getEdgeSet()) {
                if (e.getSourceNode().getId().equals("root") || e.getTargetNode().getId().equals("root")) {
                    e.setAttribute("layout.weight", 250);
                }
                e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
            }
            return;
        }

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
        }
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
        while (i < 5 || cc.getConnectedComponentsCount() != 1) { // needed to ensure graph is connected
            cc.init(graph);
            gen.nextEvents();
            i++;
        }
        gen.end();

        return graph;
    }

    /**
     * Builds a random graph that kinda resembles a rural areas with small cities
     */
    public static Graph countrysideGraph(long seed, String name) {
        Random rand = new Random(seed);

        int minCitySize = 5;
        int maxCitySize = 20;

        int minNumOfCities = 10;
        int maxNumOfCities = 30;

        int minRoadLengthCity = 1;
        int maxRoadLengthCity = 10;

        int minRoadLengthNetwork = 5;
        int maxRoadLengthNetwork = 100;

        double connectednessCity = 5.7; //svarer til 0.3 probability i algs4 udgaven
        double connectednessNetwork = 3;

        int numOfCities = (int) (rand.nextDouble() * (maxNumOfCities - minNumOfCities)) + minNumOfCities;
        List<Graph> cities = new ArrayList<>();

        //generating a graph for each city
        for (int i = 0; i < numOfCities; i++) {
            int citySize = (int) (rand.nextDouble() * (maxCitySize - minCitySize)) + minCitySize ;
            cities.add(ErdosRenyiConnectedGraph("City" + i, citySize, connectednessCity, minRoadLengthCity, maxRoadLengthCity, rand, seed));
        }

        //generating a graph for the road network between cities
        Graph roadNetwork = ErdosRenyiConnectedGraph(name, numOfCities, connectednessNetwork, minRoadLengthNetwork, maxRoadLengthNetwork, rand, seed);

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
//        Utility.setDistances(roadNetwork);

        //Utility.saveCompleteGraph("random1", "data/graphs/", roadNetwork);
        return roadNetwork;
    }

    /**
     *
     * @param name the name of the city
     * @param numVertices an int ...
     * @param degree
     * @param maxEdgeWeight
     * @param rand
     * @param seed
     * @return
     */
    public static Graph ErdosRenyiConnectedGraph(String name, int numVertices, double degree, int minEdgeWeight, int maxEdgeWeight, Random rand, long seed) {
        Graph graph = new SingleGraph(name);
        RandomGenerator gen = new RandomGenerator(degree); // GraphStream's impl. of Erdos Renyi
        gen.setRandomSeed(seed);

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
            return ErdosRenyiConnectedGraph(name, numVertices, degree, minEdgeWeight, maxEdgeWeight, rand, seed);
        }

        //setting random weights between minEdgeWeights and maxEdgeWeights on all edges
        for (Edge e : graph.getEdgeSet()) {
            int length = (int) (rand.nextDouble() * (maxEdgeWeight - minEdgeWeight)) + minEdgeWeight;
            e.setAttribute("layout.weight", length);
        }


        return graph;
    }
}
