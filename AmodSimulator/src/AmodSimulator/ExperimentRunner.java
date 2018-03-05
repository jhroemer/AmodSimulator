package AmodSimulator;

import GraphCreator.RandomGraphGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.*;
import java.util.*;

import static AmodSimulator.AssignmentType.SCRAM;

public class ExperimentRunner {
    private static String graphPath = "data/graphs/AstridsTestGraph.dgs";
    private static AssignmentType assignmentMethod = SCRAM;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the path for a properties file as argument");
            System.exit(1);
        }
        Properties props = Utility.loadProps(args[0]);

        Graph graph = parseGraph("test", graphPath); // todo: should graphPath be in props also?

        Graph randomGraph = RandomGraphGenerator.countrysideGraph();

//        runPredefinedExperiment(graph, 1000, true);
        runExperiment(randomGraph, props);
    }

    /**
     * Runs a single experiment
     *
     * @param graph
     */
    private static void runExperiment(Graph graph, Properties props) {
        int numVehicles = Integer.parseInt(props.getProperty("numVehicles"));
        int iterations = Integer.parseInt(props.getProperty("iterations"));
        int timeSteps = Integer.parseInt(props.getProperty("timeSteps"));
        boolean visual = Boolean.parseBoolean(props.getProperty("isVisual"));

        double totalAvgUnoccupied = 0.0;
        double totalAvgWait = 0.0;

        for (int i = 0; i < iterations; i++) {
            AmodSimulator simulator = new AmodSimulator(graph, visual, numVehicles, assignmentMethod);

            if (visual) sleep(2500); //Makes the simulation start after the graph is drawn.

            for (int j = 0; j < timeSteps; j++) {
                simulator.tick(graph, j);
                if (visual) sleep(50);
            }

            // results for this iteration
            int unoccupied = simulator.getUnoccupiedKmDriven();
            double avgUnoccupied = (double) unoccupied / (double) numVehicles;
            int wait = simulator.getWaitingTime();
            double avgWait = (double) wait / (double) simulator.getAssignedRequests().size();
            // add to total results
            totalAvgUnoccupied += avgUnoccupied;
            totalAvgWait += avgWait;

            // done with simulation, get the results
            System.out.println("unoccupied km's driven: " + unoccupied);
            System.out.println("unoccupied km's avg: " + avgUnoccupied);
            System.out.println("waiting time: " + simulator.getWaitingTime());

            props.setProperty(i + "-wait", String.valueOf(simulator.getWaitingTime()));
            props.setProperty(i + "-unoccupied", String.valueOf(simulator.getUnoccupiedKmDriven()));
        }

        totalAvgUnoccupied = totalAvgUnoccupied / iterations;
        totalAvgWait = totalAvgWait / iterations;

        System.out.println("<<<<<<<<<<<< RESULTS >>>>>>>>>>>>>");
        System.out.println("total avg unoccupied: " + totalAvgUnoccupied);
        System.out.println("total avg wait: " + totalAvgWait);

        Utility.saveResultsAsFile(props);
    }

    /**
     *
     * @param graph
     * @param timesteps
     * @param visual
     */
    private static void runPredefinedExperiment(Graph graph, int timesteps, boolean visual) {
        //generating controlled vehicles and requests
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("v1", graph.getNode("A")));
        vehicles.add(new Vehicle("v2", graph.getNode("F")));

        List<Request> requests = new ArrayList<Request>();
        requests.add(new Request(1, graph.getNode("F"), graph.getNode("B"),0));
        requests.add(new Request(2, graph.getNode("A"), graph.getNode("E"),0));
        requests.add(new Request(3, graph.getNode("F"), graph.getNode("D"),0));
        requests.add(new Request(4, graph.getNode("A"), graph.getNode("B"),0));
        requests.add(new Request(5, graph.getNode("C"), graph.getNode("D"),0));
        requests.add(new Request(6, graph.getNode("D"), graph.getNode("C"),0));
        Map<Integer, List<Request>> requestMap = new HashMap<>();
        requestMap.put(0, requests);

        AmodSimulator simulator = new AmodSimulator(graph, visual, vehicles, requestMap, assignmentMethod);

        if (visual) sleep(2500); //Makes the simulation start after the graph is drawn.

        for (int i = 0; i < timesteps; i++) {
            simulator.tick(graph, i);
            if (visual) sleep(50);
        }

        //simulator.getResults()
        //printResults()
        //saveResultsAsFile()
    }

    /**
     * Constructs a <Code>Graph</Code> from an dgs-file
     * @param fileId    Id to give the graph
     * @param filePath  Path to a file containing a graph in dgs-format
     * @return
     */
    public static Graph parseGraph(String fileId, String filePath) {
        Graph graph = new MultiGraph(fileId);
        FileSource fs = new FileSourceDGS() {
        };

        fs.addSink(graph);

        try {
            fs.readAll(filePath);
        } catch( IOException e) {
        } finally {
            fs.removeSink(graph);
        }

        return graph;
    }

    /**
     * Makes thread sleep
     */
    protected static void sleep(int duration) {
        try { Thread.sleep(duration); } catch (Exception e) {}
    }

//        //todo: test if we can save a lookup-table like this:
//        Map<Node, Map<Node, Integer>> lookupTable = new HashMap<>();
//        graph.setAttribute("lookupTable", lookupTable);

}
