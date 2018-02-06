package AmodSimulator;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

public class TripPlanner {
    private static Dijkstra dijkstra;
    private static boolean DEBUG = false;

    public static Path getPath(Node source, Node destination) {
        dijkstra.setSource(source);
        dijkstra.compute();
        if (DEBUG) System.out.println("tripplanner returning path: " + dijkstra.getPath(destination));
        return dijkstra.getPath(destination);
    }

    public static void init(Graph graph) {
        dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "layout.weight");
        dijkstra.init(graph);
    }
}
