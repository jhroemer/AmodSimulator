package AmodSimulator;

import SCRAM.DummyNode;
import SCRAM.Node;
import SCRAM.SCRAM;
import SCRAM.Edge;
import org.graphstream.graph.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static AmodSimulator.AmodSimulator.PRINT;

public class Utility {

    public static Map<org.graphstream.graph.Node,Map<org.graphstream.graph.Node,Integer>> produceLookupTable(Graph graph) {
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

    static List<Edge> assign(AssignmentType type, List<Vehicle> vehicles, List<Request> requests) {

        // done to avoid major and annoying generic refactoring in AmodSimulator - adds some (linear) running time
        List<Node> vehicleNodeList = new ArrayList<>(vehicles);
        List<Node> requestNodeList = new ArrayList<>(requests);

        switch (type) {
            case SCHWACHSINN:
                return schwachsinnAssign(vehicleNodeList, requestNodeList);
            case HUNGARIAN:
//                return hungarianAssign(vehicles,requests);
            case SCRAM:
                SCRAM s = new SCRAM(vehicleNodeList, requestNodeList);
                return s.getAssignments();
        }

        try {
            throw new Exception(type + " is not a valid assignment method");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Assigns vehicles to requests by simply matching first vehicle to first request, second vehicle
     * to second request and so on, until either vehicles or requests are used up.
     * @param vehicles
     * @param requests
     * @return
     */
    private static List<Edge> schwachsinnAssign(List<Node> vehicles, List<Node> requests) {

        List<Edge> assignments = new ArrayList<>();

        int numToAssign = Math.min(vehicles.size(),requests.size());

        if (PRINT && numToAssign != 0) System.out.println("\nAssigning");

        for (int i = 0; i < numToAssign; i++) {
            assignments.add(new Edge(vehicles.get(i), requests.get(i)));
            if (PRINT) System.out.println("\tVehicle "+ vehicles.get(i).getInfo() + " <-- request " + requests.get(i).getInfo());
        }

        return assignments;
    }

    public static int getDist(org.graphstream.graph.Node origin, org.graphstream.graph.Node destination) {
        return origin.getAttribute("distTo"+ destination.getId());
    }

    public static void printDistances(Graph graph) {
        int n = graph.getNodeCount();
        System.out.print("\t\t");
        for (int j = 0; j < n; j++) System.out.print(graph.getNode(j).getId() + "\t");
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.print(i + "(" + graph.getNode(i).getId() + ") :");
            for (int j = 0; j < n; j++) {
                System.out.print("\t" + Utility.getDist(graph.getNode(i), graph.getNode(j)));
            }
            System.out.println();
        }
    }


    private static void addDummyNodes(org.jgrapht.Graph<Node, Assignment> graph, Set<Node> vehicles, Set<Node> requests) {

        int numVeh = vehicles.size();
        int numReq = requests.size();
        if (numVeh == numReq) return; //no need for dummy nodes

        //making dummies
        int numDummies = Math.abs(numVeh - numReq);
        Set<Node> dummies = new HashSet<>();
        for (int i = 0; i < numDummies; i++) {
            DummyNode dummy = new DummyNode();
            dummies.add(dummy);
            graph.addVertex(dummy);
        }

        //setting edges from dummies to the vehicles/request in the biggest of the Sets
        Set<Node> smallest = (numVeh < numReq)? vehicles : requests;
        Set<Node> biggest = (numVeh > numReq)? vehicles : requests;

        for (Node dummy : dummies) {
            for (Node real : biggest) {
                Assignment assignmentEdge = graph.addEdge(dummy,real); //assignments containing a dummy does not contain either a vehicle or a request
                assignmentEdge.setToDummy();
                graph.setEdgeWeight(assignmentEdge, Integer.MAX_VALUE);
            }
        }

        //adding the dummies to the smallest of the Sets
        smallest.addAll(dummies);

    }
}
