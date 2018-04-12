package GraphCreator;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.generator.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static GraphCreator.GraphType.*;

public class RandomGraphGenerator {

    private static boolean DEBUG = false;

    public static void main(String[] args) {

        generateExperimentGraphs("chapter5");

        /*
        Graph lob = createLobsterGraph(10, "lobster", 120);
        Graph grid = createGridGraph(10, "grid", 10);
        Graph incomplete = createManualIncompleteGridGraph(10, "incomplete", 12);
        Graph ban = createBananatreeGraph(10, "banana", 11, 11, 10);

        System.out.println("banana: " + ban.getNodeCount());
        System.out.println("ban diameter " + Toolkit.diameter(ban, "layout.weight", false));
        System.out.println("ban density: " + Toolkit.density(ban));
        int banLength = 0;
        for (Edge e : ban.getEdgeSet()) banLength += (int) e.getAttribute("layout.weight");
        System.out.println("ban length: " + banLength);

        System.out.println("grid diameter " + Toolkit.diameter(grid, "layout.weight", false));
        System.out.println("grid density: " + Toolkit.density(grid));
        int gridLength = 0;
        for (Edge e : grid.getEdgeSet()) gridLength += (int) e.getAttribute("layout.weight");
        System.out.println("grid length: " + gridLength);

        System.out.println("incomplete: " + incomplete.getNodeCount());
        System.out.println("incomplete diameter " + Toolkit.diameter(incomplete, "layout.weight", false));
        System.out.println("incomplete density: " + Toolkit.density(incomplete));
        int incompleteLength = 0;
        for (Edge e : incomplete.getEdgeSet()) incompleteLength += (int) e.getAttribute("layout.weight");
        System.out.println("incomplete length: " + incompleteLength);

        System.out.println("lob nodecount: " + lob.getNodeCount());
        System.out.println("lob diameter " + Toolkit.diameter(lob));
        System.out.println("lob density: " + Toolkit.density(lob));
        System.out.println("lob distance: " + lob.getEdgeSet().size());
        int lobLength = 0;
        for (Edge e : lob.getEdgeSet()) lobLength += (int) e.getAttribute("layout.weight");
        System.out.println("lob length: " + lobLength);

        grid.display(false);
        ban.display();
        lob.display();
        incomplete.display(false);
        */

        /*
        saveGraphAsPicture(lob);
        saveGraphAsPicture(grid);
        saveGraphAsPicture(ban);
        */
    }

    /**
     *
     * @param chapter
     */
    private static void generateExperimentGraphs(String chapter) {
        List<GraphType> types = new ArrayList<>();
        types.add(GRID);
        types.add(INCOMPLETEGRID);
        types.add(LOBSTER);
        // types.add(BARABASI);
        // types.add(DOROGOVTSEV);
        // types.add(COUNTRYSIDE);
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
                int totalLength = 0;

                assert graph != null;
                for (Edge e : graph.getEdgeSet()) totalLength += (int) e.getAttribute("layout.weight");
                System.out.println(type + " had: " + graph.getNodeCount() + " nodes" + " and total length of: " + totalLength);

                Utility.setDistances(graph);
                Utility.saveCompleteGraph(graph.getId(), "data/graphs/" + chapter + "/" + type + "/", graph);
                System.out.println("TOOK: " + (System.currentTimeMillis() - start) + "ms to create graph");
            }
        }
    }

    /**
     *
     * @param type
     * @param seedInt
     * @param name
     * @return
     */
    private static Graph generateGraph(GraphType type, int seedInt, String name) {

        switch (type) {
            case GRID:
                return createGridGraph(seedInt, name, 15);
            case INCOMPLETEGRID:
                return createManualIncompleteGridGraph(seedInt, name, 16);
            case LOBSTER:   // TODO : lobster and barabasi are quite alike, lobster seems to perform worse than barabasi
                return createLobsterGraph(seedInt, name, 255);
            case BARABASI:
                return createBarabasiGraph(type, seedInt, name, 256);
            case DOROGOVTSEV:
                return createDorogovtsevGraph(type, seedInt, name, 256);
            case BANANATREE:
                return createBananatreeGraph(seedInt, name, 16, 16, 15);
            case COUNTRYSIDE:
                return createCountrysideGraph(type, seedInt, name);
        }

        try {
            throw new Exception("problem with generateGraph()");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
        return null;
    }

    /**
     *
     * @param seedInt
     * @param name
     * @param size
     * @return
     */
    private static Graph createGridGraph(int seedInt, String name, int size) {
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new GridGenerator();
        gen.setRandomSeed(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) e.setAttribute("layout.weight", 1);

        return graph;
    }

    /**
     *
     * @param seedInt
     * @param name
     * @param size
     * @return
     */
    private static Graph createManualIncompleteGridGraph(int seedInt, String name, int size) {
        boolean hasOnlyOneCC = false;

        Random rand = new Random();
        rand.setSeed(seedInt);
        Graph graph = null;

        while (!hasOnlyOneCC) {
            graph = new SingleGraph(name);
            BaseGenerator gen = new GridGenerator();
            gen.setRandomSeed(10);
            gen.addSink(graph);
            gen.begin();
            for (int i = 0; i < size; i++) gen.nextEvents();
            gen.end();

            for (int i = 0; i < 40; i++) {
                Node node = graph.removeNode(rand.nextInt(graph.getNodeCount()));
                for (Edge e : graph.getEdgeSet())
                    if (e.getSourceNode() == node || e.getTargetNode() == node) graph.removeEdge(e);
            }
            ConnectedComponents cc = new ConnectedComponents();
            cc.init(graph);
            if (cc.getConnectedComponentsCount() == 1) hasOnlyOneCC = true;
        }

        for (Edge e : graph.getEdgeSet()) e.setAttribute("layout.weight", 1);

        assert graph != null;
        return graph;
    }

    /**
     * Creates an incomplete grid graph
     *
     * @param seedInt
     * @param name
     * @param size
     * @return
     */
    private static Graph createIncompleteGridGraph(int seedInt, String name, int size) {
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new IncompleteGridGenerator(false, 0.5f, 5, 3);
        gen.setRandomSeed(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) e.setAttribute("layout.weight", 1);

        return graph;
    }

    /**
     *
     * @param seedInt
     * @param name
     * @param size
     * @return
     */
    private static Graph createLobsterGraph(int seedInt, String name, int size) {
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new LobsterGenerator(2, 3);
        gen.setRandomSeed(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("layout.weight", 1);
        }

        return graph;
    }

    /**
     *
     * @param type
     * @param seedInt
     * @param name
     * @param size
     * @return
     */
    private static Graph createBarabasiGraph(GraphType type, int seedInt, String name, int size) {
        int upperBound = 25;
        int lowerBound = 10;
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new BarabasiAlbertGenerator(2);
        gen.setRandomSeed(seedInt);
        Random rand = new Random(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < size; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) {
//            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
            e.setAttribute("layout.weight", 1);
        }

        return graph;
    }

    /**
     *
     * @param type
     * @param seedInt
     * @param name
     * @param size
     * @return
     */
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

//        for (Edge e : graph.getEdgeSet()) {
//            e.setAttribute("layout.weight", rand.nextInt((upperBound-lowerBound) + lowerBound));
//        }

        return graph;
    }

    /**
     *
     * @param seedInt
     * @param name
     * @param n
     * @param k
     * @return
     */
    private static Graph createBananatreeGraph(int seedInt, String name, int n, int k, int lengthOfLongRoads) {
        Graph graph = new SingleGraph(name);
        BaseGenerator gen = new BananaTreeGenerator(k);
        gen.setRandomSeed(seedInt);

        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < n; i++) gen.nextEvents();
        gen.end();

        for (Edge e : graph.getEdgeSet()) {
            if (e.getSourceNode().getId().equals("root") || e.getTargetNode().getId().equals("root")) {
                e.setAttribute("layout.weight", lengthOfLongRoads);
            }
            else e.setAttribute("layout.weight", 1);
        }

        return graph;
    }

    private static Graph createCountrysideGraph(GraphType type, int seedInt, String name) {
        return countrysideGraph(10, name);
    }

    /**
     * Builds a random graph that kinda resembles a rural areas with small cities
     */
    @Deprecated
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
//        for (Edge edge : roadNetwork.getEdgeSet()) System.out.println("edge weight is: " + edge.getAttribute("layout.weight") + " and the nodes are: " + edge.getSourceNode() + " and: " + edge.getTargetNode());

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
    @Deprecated
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

    /**
     *
     * @param graph
     */
    private static void saveGraphAsPicture(Graph graph) {
        FileSinkImages pic = new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.VGA);

//        pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
        pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.NO_LAYOUT);

        try {
            pic.writeAll(graph, "data/pictures/" + graph.getId() + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
