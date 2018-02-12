package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.IOException;

public class ExperimentRunner {

    private static String graphPath = "data/graphs/AstridsTestGraph.dgs";

    public static void main(String[] args) {
        Graph graph = parseGraph("test", graphPath);
        runExperiment(graph,1000,false);
    }

    private static void runExperiment(Graph graph, int timesteps, boolean visual) {
        AmodSimulator simulator = new AmodSimulator(graph, visual);

        if (visual) for (int j = 0; j < 50; j++) sleep(); //Makes the simulation start after the graph is drawn.


        for (int i = 0; i < timesteps; i++) {
            simulator.tick(graph, i);
            if (visual) sleep();
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
    protected static void sleep() {
        try { Thread.sleep(50); } catch (Exception e) {}
    }


//        //todo: test if we can save a lookup-table like this:
//        Map<Node, Map<Node, Integer>> lookupTable = new HashMap<>();
//        graph.setAttribute("lookupTable", lookupTable);

}
