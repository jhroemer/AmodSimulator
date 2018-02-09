package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class AmodSimulator {

    private static String styleSheetPath = "styles/style.css";
    private static String graphPath = "data/graphs/random1.dgs";
    private static int timesteps = 10000000;
    private static int numVehicles = 10;
    static boolean IS_VISUAL = true;
    private static List<Vehicle> activeVehicles;
    private static List<Vehicle> idleVehicles;
    private static List<Request> requests;
    private static Map<Integer,List<Vehicle>> ETAMap = new HashMap<>();

    public static void main(String[] args) {

        Graph graph = parseGraph("test", graphPath);
        TripPlanner.init(graph);
        graph.display();
        SpriteManager sman = new SpriteManager(graph);

        //todo: test if we can safe a lookup-table like this:
        Map<Node,Map<Node, Double>> lookupTable = new HashMap<>();
        graph.setAttribute("lookupTable", lookupTable);

        if (IS_VISUAL) {
            String styleSheet = parseStylesheet(styleSheetPath);
            graph.addAttribute("ui.stylesheet", styleSheet);
        }

        activeVehicles = new ArrayList<>();
        idleVehicles = generateVehicles(graph, sman, numVehicles);
        requests = new ArrayList<>();

        for (int j = 0; j < 50; j++) sleep(); //Makes the simulation start after the graph is drawn.


        for (int i = 0; i < timesteps; i++) {
        //while (true) {
            tick(graph, i);
            //if (IS_VISUAL) for (Vehicle veh : vehicles) veh.advance();
            sleep();
        }


    }

    private static List<Vehicle> generateVehicles(Graph graph, SpriteManager sman, int numVehicles) {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        Random r = new Random();
        for (int i = 0; i < numVehicles; i++) {
            vehicles.add(new Vehicle("v" + i,graph.getNode(r.nextInt(graph.getNodeCount())), sman.addSprite("s" + i, AmodSprite.class)));
        }
        return vehicles;
    }

    /**
     *
     * @param graph
     */
    private static void tick(Graph graph, int timeStep) {

        //adding requests for the current timestep
        requests.addAll(RequestGenerator.generateRequests(graph,0.1));

        // adding new vacant vehicles to idlevehicles, if vehicle does not have more requests
        for (Vehicle veh : ETAMap.getOrDefault(timeStep, new ArrayList<>())) {
            // veh.arrive();
            if (veh.hasMoreRequests()) {
                int finishTime = veh.startRequest(timeStep);
                addToETAMap(finishTime, veh);
            }
            else {
                makeIdle(veh);
            }
        }

        List<Vehicle> assignedVehicles = assign();
        for (Vehicle veh : assignedVehicles) {
            int finishTime = veh.startRequest(timeStep);
            addToETAMap(finishTime, veh);
            makeActive(veh);
        }

        if (IS_VISUAL) drawSprites(timeStep);
    }

    private static void drawSprites(int timeStep) {
        // iterate over all vehicles and draw sprites
        for (Vehicle veh : activeVehicles) {
            
        }
    }

    private static List<Vehicle> assign() {
        // assigning vehicles to requests
        Iterator<Request> requestIterator = requests.iterator();
        List<Vehicle> assignedVehicles = new ArrayList<>();
        while (requestIterator.hasNext()) {
            if (!idleVehicles.isEmpty()) {
                Vehicle veh = idleVehicles.remove(0);
                veh.addRequest(requestIterator.next());
                assignedVehicles.add(veh);
                activeVehicles.add(veh);
            }
        }
        return assignedVehicles;
    }


    private static void addToETAMap(int finishTime, Vehicle veh) {
        if (ETAMap.containsKey(finishTime)) ETAMap.get(finishTime).add(veh);
        else {
            ArrayList<Vehicle> list = new ArrayList<Vehicle>();
            list.add(veh);
            ETAMap.put(finishTime, list);
        }
    }

    private static void makeIdle(Vehicle veh) {
        activeVehicles.remove(veh);
        idleVehicles.add(veh);
    }

    private static void makeActive(Vehicle veh) {
        idleVehicles.remove(veh);
        activeVehicles.add(veh);
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
     * Method that parses a stylesheet to use with the graph
     * @param path      path to the CSS file
     * @return String   containing the stylesheet
     */
    private static String parseStylesheet(String path) {
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
     * Makes thread sleep
     */
    protected static void sleep() {
        try { Thread.sleep(50); } catch (Exception e) {}
    }
}
