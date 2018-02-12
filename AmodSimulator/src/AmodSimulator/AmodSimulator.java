package AmodSimulator;

import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class AmodSimulator {

    static final boolean PRINT = false;
    private static String styleSheetPath = "styles/style.css";
    private static String graphPath = "data/graphs/AstridsTestGraph.dgs";
    private static int timesteps = 10000000;
    private static int numVehicles = 10;
    static boolean IS_VISUAL = true;
    private static List<Vehicle> activeVehicles;
    private static List<Vehicle> idleVehicles;
    private static List<Request> requests;
    private static Map<Integer,List<Vehicle>> vacancyMap = new HashMap<>();
    private static SpriteManager sman;
    private static List<Request> assignedRequests = new ArrayList<>();

    public static void main(String[] args) {

        Graph graph = parseGraph("test", graphPath);
        TripPlanner.init(graph);
        sman = new SpriteManager(graph);

//        //todo: test if we can save a lookup-table like this:
//        Map<Node, Map<Node, Integer>> lookupTable = new HashMap<>();
//        graph.setAttribute("lookupTable", lookupTable);

        // fixme : we should ensure this when building the graph already
        //for (Edge e : graph.getEdgeSet()) {
        //    if ((double) e.getAttribute("layout.weight") < 1.0) e.setAttribute("layout.weight", 1.0);
        //}

        if (IS_VISUAL) {
            graph.display();
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
            vehicles.add(new Vehicle("v" + i, graph.getNode(r.nextInt(graph.getNodeCount()))));
            sman.addSprite("v" + i);
        }
        return vehicles;
    }

    /**
     * What happens within a timestep:
     * 1. Check which vehicles have been set to idle
     * 2. Assign idle vehicles to requests
     * 3.
     *
     * @param graph
     */
    private static void tick(Graph graph, int timeStep) {
        if (PRINT) System.out.println("\n\n//////// TICK " + timeStep + "/////////");
        // adding new vacant vehicles to idlevehicles, if vehicle does not have more requests

        if (PRINT && vacancyMap.containsKey(timeStep)) System.out.print("\nMaking idle: ");
        for (Vehicle veh : vacancyMap.getOrDefault(timeStep, new ArrayList<>())) {
            makeIdle(veh);
            if (PRINT) System.out.print(veh.getId() + ", ");
        }
        vacancyMap.remove(timeStep);
        if (PRINT) System.out.println();

        //adding requests for the current timestep
        requests.addAll(RequestGenerator.generateRequests(graph,0.3, timeStep));

        List<Vehicle> assignedVehicles = assign(timeStep);

        for (Vehicle veh : assignedVehicles) {
            addToVacancyMap(veh);
            makeActive(veh);
        }

        if (PRINT) printVacancyMap();

        if (IS_VISUAL) drawSprites(timeStep);
    }

    private static void drawSprites(int timeStep) {
        // iterate over all vehicles and draw sprites
        for (Vehicle veh : activeVehicles) {
            SpritePosition spritePosition = veh.findAttachment(timeStep);
            Sprite s = sman.getSprite(veh.getId());
            System.out.println("Spriteposition is: " + spritePosition);
            attachIfNeeded(s, spritePosition.getElement());
            s.setPosition(spritePosition.getPosition());
            s.setAttribute("ui.class", spritePosition.getStatus());
        }
    }

    /**
     * Attaches this Sprite to this Element, if it is not already attached to that same element
     * @param sprite
     * @param element
     */
    private static void attachIfNeeded(Sprite sprite, Element element) {
        if (sprite.getAttachment() == element) return;

        if (element instanceof Node) {
            System.out.println("attaching to node");
            sprite.attachToNode(element.getId());
        }
        else {
            System.out.println("attaching to edge");
            sprite.attachToEdge(element.getId());
        }

    }

    private static List<Vehicle> assign(int timeStep) {

        List<Vehicle> assignedVehicles = new ArrayList<>();

        int numToAssign = Math.min(idleVehicles.size(),requests.size());
        if (PRINT && numToAssign != 0) System.out.println("\nAssigning");

        for (int i = 0; i < numToAssign; i++) {
            Vehicle veh = idleVehicles.get(i);
            if (PRINT) System.out.println("\tVehicle "+ veh.getId() + " <-- request " + requests.get(i).getId());
            veh.serviceRequest(requests.get(i));
            assignedRequests.add(requests.get(i));
            assignedVehicles.add(veh);
            if (IS_VISUAL) veh.addRequest(requests.get(i));
        }

        for (int i = 0; i < numToAssign; i++) {
            requests.remove(0);
        }

        return assignedVehicles;
    }


    private static void addToVacancyMap(Vehicle veh) {
        //todo Should also delete if the vehicle is already on the Map
        //todo (needs old finish time for this) but it is not necessary for
        //todo the one-request version.
        int vacancyTime = veh.getFinishTime() + 1;
        if (vacancyMap.containsKey(vacancyTime)) vacancyMap.get(vacancyTime).add(veh);
        else {
            ArrayList<Vehicle> list = new ArrayList<Vehicle>();
            list.add(veh);

            vacancyMap.put(vacancyTime, list);
        }
    }

    private static void makeIdle(Vehicle veh) {
        activeVehicles.remove(veh);
        idleVehicles.add(veh);
        if (IS_VISUAL) veh.removeRequest();
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


    public static void printVacancyMap() {
        System.out.println("\n--- vacancyMap ---");
        for (int i : vacancyMap.keySet()) {
            System.out.print("  " + i + " --> ");
            for (Vehicle v : vacancyMap.get(i)) {
                System.out.print(v.getId() + ", ");
            }
            System.out.println();
        }
        System.out.println("\n------------------");
    }
}
