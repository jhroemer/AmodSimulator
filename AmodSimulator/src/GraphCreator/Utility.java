package GraphCreator;

import AmodSimulator.TripPlanner;
import org.graphstream.graph.Graph;
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

    //todo Er det lidt noget rod at denne metode skal bruge TripPlanner og AmodSimulator.Utility?
    public static int[][] createPathLengthLookupTable(Graph graph) {
        int n = graph.getNodeCount();
        int[][] table = new int[n][n];

        TripPlanner tripPlanner = new TripPlanner();
        tripPlanner.init(graph);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Path path = tripPlanner.getPath(graph.getNode(i),graph.getNode(j));
                //table[i][j] = Utility.midlertidigPathLenght(path);
                //TODO
            }
        }

        return table;
    }

}
