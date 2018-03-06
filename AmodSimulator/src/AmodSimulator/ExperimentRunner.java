package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Class used to run experiments.
 * Uses java Properties files.
 */
public class ExperimentRunner {

    /**
     *
     * @param args should be a path to a folder containing properties for experiments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the path for a properties file folder as argument");
            System.exit(1);
        }
        if (args[0].equals("predefined")) {
            System.out.println("running predefined - but still nothing to run");
            // Graph randomGraph = RandomGraphGenerator.countrysideGraph();
            // runPredefinedExperiment(graph, 1000, true);
            System.exit(1);
        }

        List<Properties> propertiesList = getPropertiesFromFolder(args[0]);

        for (Properties props : propertiesList) {
            runExperiment(props);
        }

        // TODO : fetch/parse results into latex table
    }

    /**
     *
     * @param props the properties object that holds all the info the experiment needs
     */
    private static void runExperiment(Properties props) {
        int numVehicles = Integer.parseInt(props.getProperty("numVehicles"));
        int trials = Integer.parseInt(props.getProperty("trials"));
        int timeSteps = Integer.parseInt(props.getProperty("timeSteps"));
        boolean visual = Boolean.parseBoolean(props.getProperty("isVisual"));
        List<Graph> graphList = getGraphsFromFolder(props.getProperty("graphFolder"));
        AssignmentType assignmentMethod = AssignmentType.valueOf(props.getProperty("assignment"));

        double totalAvgUnoccupied = 0.0;
        double totalAvgWait = 0.0;

        // for each graph-type, do i simulations and collect data
        for (Graph graph : graphList) {

            for (int i = 0; i < trials; i++) {

                AmodSimulator simulator = new AmodSimulator(graph, visual, numVehicles, assignmentMethod);
                if (visual) sleep(2500); //Makes the simulation start after the graph is drawn.
                // running the simulation
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

                props.setProperty(graph.getId() + "_" + i + "_wait", String.valueOf(simulator.getWaitingTime()));
                props.setProperty(graph.getId() + "_" + i + "_unoccupied", String.valueOf(simulator.getUnoccupiedKmDriven()));
            }

            // todo : check the double-int-division of totalAvgWait and trials

            // after i trials, get the average
            props.setProperty("TOTAL_" + graph.getId() + "_avgUnoccupied", String.valueOf(totalAvgUnoccupied / trials));
            props.setProperty("TOTAL_" + graph.getId() + "_avgWait", String.valueOf(totalAvgWait / trials));
        }

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

        AmodSimulator simulator = new AmodSimulator(graph, visual, vehicles, requestMap, AssignmentType.SCRAM);

        if (visual) sleep(2500); //Makes the simulation start after the graph is drawn.

        for (int i = 0; i < timesteps; i++) {
            simulator.tick(graph, i);
            if (visual) sleep(50);
        }
    }

    /**
     *
     * @param folderPath
     * @return
     */
    private static List<Graph> getGraphsFromFolder(String folderPath) {
        List<Graph> graphList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> graphList.add(parseGraph("G" + String.valueOf(graphList.size()+1), path.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graphList;
    }

    /**
     *
     * @param folderPath
     * @return
     */
    private static List<Properties> getPropertiesFromFolder(String folderPath) {
        List<Properties> propertiesList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> propertiesList.add(Utility.loadProps(String.valueOf(path))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propertiesList;
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
