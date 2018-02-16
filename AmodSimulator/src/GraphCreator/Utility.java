package GraphCreator;

import AmodSimulator.TripPlanner;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.stream.file.FileSinkDGS;

import java.io.IOException;

public class Utility {

    public static void saveCompleteGraph(String file, Graph graph) {
        FileSinkDGS fs = new FileSinkDGS();
        try {
            fs.writeAll(graph, "data/graphs/" + file + ".dgs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Gives all nodes in the graph an attribute for each node in the graph containing the distance from the node to
     * all other nodes. The attributes has the name "distTo[NodeId]", eg if two nodes with id "A" and "B" have a
     * distance of 10 between them, then Node "A" gets the attribute "distToB" with a value of 10 and Node "B" likewise
     * gets the attribute "distToA" with a value of 10.
     * @param graph
     */
    //todo - Er det noget rod at denne metode bruger Tripplanner som er fra den anden package?
    public static void setDistances(Graph graph) {
        TripPlanner.init(graph);
        for (Node origin : graph) {
            for (Node destination : graph) {
                origin.setAttribute("distTo" + destination.getId(), calcPathLength(TripPlanner.getPath(origin, destination)));
            }
        }
    }


    /**
     * Calculates the length of a path
     * @param path
     * @return
     */
    public static int calcPathLength(Path path) {
        int length = 0;
        for (Edge e : path.getEdgeSet()) {
            length += (int) e.getAttribute("layout.weight");
        }
        return length;
    }
}

