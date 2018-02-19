package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.SimpleGraph;

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

    //todo make ENUM
    public static List<Assignment> assign(String type, List<Vehicle> vehicles, List<Request> requests) {

        switch (type) {
            case "brute":
                return bruteForceAssign(vehicles,requests);
            case "hungarian":
                return hungarianAssign(vehicles,requests);
        }

        try {
            throw new Exception(type + " is not a valid assignment method");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Assigns vehicles to requests by simply matching first vehicle to first request, second vehicle
     * to second request and so on, until either vehicles or requests are used up.
     * @param vehicles
     * @param requests
     * @return
     */
    public static List<Assignment> bruteForceAssign(List<Vehicle> vehicles, List<Request> requests) {

        List<Assignment> assignments = new ArrayList<>();

        int numToAssign = Math.min(vehicles.size(),requests.size());
        if (PRINT && numToAssign != 0) System.out.println("\nAssigning");

        for (int i = 0; i < numToAssign; i++) {
            assignments.add(new Assignment(vehicles.get(i), requests.get(i)));
            if (PRINT) System.out.println("\tVehicle "+ vehicles.get(i).getId() + " <-- request " + requests.get(i).getId());
        }

        return assignments;
    }

    public static int getDist(Node origin, Node destination) {
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

    //todo do we need to make sure that there are the same amount of vehicles and request, or does the algorithm work without this?
    public static List<Assignment> hungarianAssign(List<Vehicle> vehicles, List<Request> requests) {


        //MultiGraph from jgrapht with nodes and edges from graphstream:
        SimpleGraph<HungarianNode,Assignment> graph = new SimpleGraph<>(new ClassBasedEdgeFactory<>(Assignment.class),true);

        Set<HungarianNode> vehicleNodes = new HashSet<>();
        Set<HungarianNode> requestNodes = new HashSet<>();

        for (Request req : requests) {
            graph.addVertex(req);
            requestNodes.add(req);
        }

        for (Vehicle veh : vehicles) {
            //Node vehNode = veh.getLocation(); //vehicles current location
            graph.addVertex(veh);
            vehicleNodes.add(veh);
            for (Request req : requests) {
                //Node reqNode = req.getOrigin(); //request pick-up location
                int intWeight = veh.getLocation().getAttribute("distTo" + req.getOrigin().getId()); //distance between the two locations
                double weight = (double) intWeight;
                //System.out.println("Adding edge from " + vehNode.getId() + " to " + reqNode.getId());
                Assignment assignmentEdge = graph.addEdge(veh,req); //info to jgrapht
                graph.setEdgeWeight(assignmentEdge, weight); //info to jgrapht

                assignmentEdge.setVehicle(veh); //info to graphstream
                assignmentEdge.setRequest(req); //info to graphstream
            }
        }

        /*
        System.out.println("Nodes:");
        for (HungarianNode h : graph.vertexSet()) {
            System.out.println(h.getInfo());
        }

        System.out.println("Edges ");
        for (Assignment a : graph.edgeSet()) {
            System.out.println("Vehicle " + a.getVehicle().getId() + " --> Request " + a.getRequest().getId());
        }
        */

        addDummyNodes(graph, vehicleNodes,requestNodes);

        KuhnMunkresMinimalWeightBipartitePerfectMatching<HungarianNode,Assignment> hungarian = new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(graph, vehicleNodes, requestNodes);
        Matching<HungarianNode, Assignment> matching = hungarian.getMatching();

        Set<Assignment> assignmentSet = matching.getEdges();

        List<Assignment> assignments = new ArrayList<>();
        assignments.addAll(assignmentSet);

        return assignments;
    }

    private static void addDummyNodes(org.jgrapht.Graph<HungarianNode,Assignment> graph, Set<HungarianNode> vehicles, Set<HungarianNode> requests) {

        int numVeh = vehicles.size();
        int numReq = requests.size();
        if (numVeh == numReq) return; //no need for dummy nodes

        //making dummies
        int numDummies = Math.abs(numVeh - numReq);
        Set<HungarianNode> dummies = new HashSet<>();
        for (int i = 0; i < numDummies; i++) {
            DummyNode dummy = new DummyNode();
            dummies.add(dummy);
            graph.addVertex(dummy);
        }

        //setting edges from dummies to the vehicles/request in the biggest of the Sets
        Set<HungarianNode> smallest = (numVeh < numReq)? vehicles : requests;
        Set<HungarianNode> biggest = (numVeh > numReq)? vehicles : requests;

        for (HungarianNode dummy : dummies) {
            for (HungarianNode real : biggest) {
                Assignment assignmentEdge = graph.addEdge(dummy,real); //assignments containing a dummy does not contain either a vehicle or a request
                assignmentEdge.setToDummy();
                graph.setEdgeWeight(assignmentEdge, Integer.MAX_VALUE);
            }
        }

        //adding the dummies to the smallest of the Sets
        smallest.addAll(dummies);

    }
}
