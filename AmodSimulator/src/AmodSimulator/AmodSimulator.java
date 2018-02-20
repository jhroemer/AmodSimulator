package AmodSimulator;

import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmodSimulator {

    static final boolean PRINT = true;
    private static String styleSheetPath = "styles/style.css";
    private AssignmentType assignmentType;
    private boolean TEST = false;
    private int numVehicles;
    boolean IS_VISUAL = true;
    private List<Vehicle> activeVehicles;
    private List<Vehicle> idleVehicles;
    private List<Request> requests;
    private Map<Integer,List<Vehicle>> vacancyMap = new HashMap<>();
    private SpriteManager sman;
    private List<Request> assignedRequests = new ArrayList<>();
    private Map<Integer, List<Request>> predefinedRequestsMap;

    /**
     * Normal constructor used to initialize a simulator
     *
     * @param graph
     * @param visual
     * @param numVehicles
     */
    public AmodSimulator(Graph graph, boolean visual, int numVehicles, AssignmentType assignmentType) {

        //printing the distances in the graph for debugging
        Utility.printDistances(graph);

        this.assignmentType = assignmentType;
        IS_VISUAL = visual;
        TripPlanner.init(graph);
        this.numVehicles = numVehicles;
        activeVehicles = new ArrayList<>();
        idleVehicles = Utility.generateVehicles(graph, numVehicles);
        requests = new ArrayList<>();

        if (IS_VISUAL) {
            setupVisuals(graph, idleVehicles);
        }
    }

    /**
     * Constructor used for testing purposes with a predefined list of vehicles and requests
     *
     * @param graph the graph to run a simulation on
     * @param visual a boolean that decides if visuals are shown or not
     * @param vehicles a predefined list of vehicles
     * @param requestMap a mapping of timesteps -> list of requests for that timestep
     */
    public AmodSimulator(Graph graph, boolean visual, List<Vehicle> vehicles, Map<Integer, List<Request>> requestMap, AssignmentType assignmentType) {
        TEST = true;
        this.assignmentType = assignmentType;
        IS_VISUAL = visual;
        TripPlanner.init(graph);
        activeVehicles = new ArrayList<>();
        idleVehicles = vehicles;
        numVehicles = vehicles.size();
        requests = new ArrayList<>();
        predefinedRequestsMap = requestMap;

        if (IS_VISUAL) {
            setupVisuals(graph, idleVehicles);
        }
    }

    /**
     * Helper-method that sets up the visual components
     *
     * @param graph the graph to run a simulation on
     * @param idleVehicles
     */
    private void setupVisuals(Graph graph, List<Vehicle> idleVehicles) {
        sman = new SpriteManager(graph);
        for (Vehicle v : idleVehicles) {
            sman.addSprite(v.getId());
            drawSprites(0);
        }
        String styleSheet = Utility.parseStylesheet(styleSheetPath);
        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.display();
    }

    /**
     * What happens within a timestep:
     * 1. Check which vehicles have been set to idle
     * 2. Assign idle vehicles to requests
     * 3.
     *
     * @param graph
     */
    public void tick(Graph graph, int timeStep) {
        if (PRINT) System.out.println("\n\n//////// TICK " + timeStep + "/////////");
        // adding new vacant vehicles to idlevehicles, if vehicle does not have more requests

        if (PRINT && vacancyMap.containsKey(timeStep)) System.out.print("\nMaking idle: ");

        for (Vehicle veh : vacancyMap.getOrDefault(timeStep, new ArrayList<>())) {
            makeIdle(veh);
            if (PRINT) System.out.print(veh.getId() + ", ");
        }
        vacancyMap.remove(timeStep);
        if (PRINT) System.out.println();

        // adding requests for current time step:
        if (TEST) requests.addAll(predefinedRequestsMap.getOrDefault(timeStep, new ArrayList<>()));
        else requests.addAll(RequestGenerator.generateRequests(graph,0.1, timeStep));

        //assigning vehicles to requests //todo no need to call this if either idleVehicles or requests are empty
        List<Assignment> assignments = Utility.assign(assignmentType,idleVehicles,requests);

        // todo : move into method?
        for (Assignment a : assignments) {
            if (a.isDummy()) continue;
            System.out.println(a);
            Vehicle veh = a.getVehicle();
            Request req = a.getRequest();
            veh.serviceRequest(req);
            addToVacancyMap(veh);
            makeActive(veh);
            assignedRequests.add(req);
            requests.remove(req);
            if (IS_VISUAL) veh.addRequest(req);
        }

        if (PRINT) printVacancyMap();

        if (IS_VISUAL) drawSprites(timeStep);
    }


    /**
     *
     * @param timeStep
     */
    private void drawSprites(int timeStep) {
        // iterate over all vehicles and draw sprites
        for (Vehicle veh : activeVehicles) {
            SpritePosition spritePosition = veh.findAttachment(timeStep);
            Sprite s = sman.getSprite(veh.getId());
            System.out.println("Spriteposition is: " + spritePosition.getElement().getId() + " at: " + spritePosition.getPosition());
            attachIfNeeded(s, spritePosition.getElement());
            s.setPosition(spritePosition.getPosition());
            s.setAttribute("ui.class", spritePosition.getStatus());
        }

        // todo : fixing that sprites didn't attach to nodes. do we have to detach, or re-attach?
        for (Vehicle veh : idleVehicles) {
            Sprite s = sman.getSprite(veh.getId());
            s.setPosition(0.0);
            s.setAttribute("ui.class", "idle");
            s.attachToNode(veh.getLocation().getId());
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


    private void addToVacancyMap(Vehicle veh) {
        //todo Should also delete if the vehicle is already on the Map
        //todo (needs old finish time for this) but it is not necessary for
        //todo the one-request version.
        int vacancyTime = veh.getVacantTime();
        if (vacancyMap.containsKey(vacancyTime)) vacancyMap.get(vacancyTime).add(veh);
        else {
            ArrayList<Vehicle> list = new ArrayList<Vehicle>();
            list.add(veh);

            vacancyMap.put(vacancyTime, list);
        }
    }

    private void makeIdle(Vehicle veh) {
        activeVehicles.remove(veh);
        idleVehicles.add(veh);
        if (IS_VISUAL) veh.removeRequest();
    }

    private void makeActive(Vehicle veh) {
        idleVehicles.remove(veh);
        activeVehicles.add(veh);
    }

    /**
     * Adds a list of vehicles to the idle vehicles in the simulator
     *
     * @param listOfVehicles a list of vehicles that are added to the simulator
     */
    public void addVehicles(List<Vehicle> listOfVehicles) {
        idleVehicles.addAll(listOfVehicles);
    }

    /**
     * Primarily (so far) for testing purposes.
     *
     * @param request ..
     */
    public void addRequest(Request request) {
        requests.add(request);
    }

    public void printVacancyMap() {
        System.out.print("\n--- vacancyMap --");
        for (int i : vacancyMap.keySet()) {
            System.out.print("\n|\t" + i + "\t--> ");
            for (Vehicle v : vacancyMap.get(i)) {
                System.out.print(v.getId() + "\t|");
            }
            //System.out.println();
        }
        System.out.println("\n-----------------");
    }

    /**
     *
     * @return
     */
    public Map<Integer, List<Vehicle>> getVacancyMap() {
        return vacancyMap;
    }

    public SpriteManager getSman() {
        return sman;
    }
}
