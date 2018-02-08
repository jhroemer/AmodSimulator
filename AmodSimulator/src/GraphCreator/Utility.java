package GraphCreator;

import org.graphstream.graph.Graph;
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

}
