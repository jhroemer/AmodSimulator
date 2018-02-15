package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static AmodSimulator.AmodSimulator.PRINT;

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
     * Assigns vehicles to requests by simply matching first vehicle to first request, second vehicle
     * to second request and so on, until either vehicles or requests are used up.
     * @param vehicles
     * @param requests
     * @return
     */
    public static Map<Vehicle, Request> assign(List<Vehicle> vehicles, List<Request> requests) {

        Map<Vehicle, Request> assignment = new HashMap<>();

        int numToAssign = Math.min(vehicles.size(),requests.size());
        if (PRINT && numToAssign != 0) System.out.println("\nAssigning");

        for (int i = 0; i < numToAssign; i++) {
            assignment.put(vehicles.get(i),requests.get(i));
            if (PRINT) System.out.println("\tVehicle "+ vehicles.get(i).getId() + " <-- request " + requests.get(i).getId());
        }

        //for (int i = 0; i < numToAssign; i++) {
        //}

        return assignment;
    }


    public static int midlertidigPathLength() {return 0;}
}
