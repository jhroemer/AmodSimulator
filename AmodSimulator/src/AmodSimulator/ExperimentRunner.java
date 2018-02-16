package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExperimentRunner {

    private static String graphPath = "data/graphs/AstridsTestGraph.dgs";

    public static void main(String[] args) {
        Graph graph = parseGraph("test", graphPath);
        runExperiment(graph,1000,false);
    }

    private static void runExperiment(Graph graph, int timesteps, boolean visual) {

        //--------------------//
        //generating controlled vehicles and requests
        //todo this is for testing the simulator. To be removed later.
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("v1", graph.getNode("A")));
        vehicles.add(new Vehicle("v2", graph.getNode("F")));

        List<Request> requests = new ArrayList<Request>();
        requests.add(new Request(1,graph.getNode("B"),graph.getNode("F"),0));
        requests.add(new Request(2,graph.getNode("E"),graph.getNode("A"),0));
        requests.add(new Request(3,graph.getNode("F"),graph.getNode("D"),0));
        requests.add(new Request(4,graph.getNode("A"),graph.getNode("B"),0));
        requests.add(new Request(5,graph.getNode("C"),graph.getNode("D"),0));
        requests.add(new Request(6,graph.getNode("D"),graph.getNode("C"),0));

        AmodSimulator simulator = new AmodSimulator(graph, visual, vehicles, requests);

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
