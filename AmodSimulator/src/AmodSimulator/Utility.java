package AmodSimulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Utility {

    public static Map<Node,Map<Node,Integer>> produceLookupTable(Graph graph) {
        //todo
        return null;
    }

    /**
     * Generater vehicles and places them at random nodes in the graph
     * @param graph
     * @param numVehicles
     * @return
     */
    public static List<Vehicle> generateVehicles(Graph graph, int numVehicles) {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        Random r = new Random();
        for (int i = 0; i < numVehicles; i++) {
            vehicles.add(new Vehicle("v" + i, graph.getNode(r.nextInt(graph.getNodeCount()))));
        }
        return vehicles;
    }

    /**
     * Method that parses a stylesheet to use with the graph
     * @param path      path to the CSS file
     * @return String   containing the stylesheet
     */
    public static String parseStylesheet(String path) {
        String styleSheet = "";
        File file = new File(path);
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (sc != null) {
            sc.useDelimiter("\\Z");
            styleSheet = sc.next();
        } else {
            System.out.println("something with parsing went wrong");
        }
        if (styleSheet.equals("")) System.out.println("No stylesheet made"); //Todo make exception instead
        return styleSheet;
    }


    /**
     * Calculates the length of a path
     * @param path
     * @return
     */
    private int calcPathLength(Path path) {
        int length = 0;
        for (Edge e : path.getEdgeSet()) {
            length += (int) e.getAttribute("length");
        }
        return length;
    }
}
