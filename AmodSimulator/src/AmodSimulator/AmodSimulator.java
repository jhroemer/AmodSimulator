package AmodSimulator;

import SCRAM.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.util.*;

import static AmodSimulator.AssignmentType.IndexSCRAM;
import static AmodSimulator.ExtensionType.*;

public class AmodSimulator {
    static boolean PRINT = false;
    private ExtensionType extensionType;
    private AssignmentType assignmentType;
    private boolean TEST = false;
    private List<Request> requests;
    private Map<Integer, List<Vehicle>> vacancyMap = new HashMap<>();
    private List<Request> assignedRequests = new ArrayList<>();
    private List<Request> unservedRequests = new ArrayList<>();
    private Map<Integer, List<Request>> predefinedRequestsMap;
    private double lambda = 0.1;
    private int ticksDone;
    private int idleVehiclesCounter; // keeps track of the amount of idlevehicles in simulation
    // visual stuff
    static boolean IS_VISUAL = true;
    private SpriteManager sman;
    private static String styleSheetPath = "styles/style.css";
    // for basic without extensions
    private List<Vehicle> activeVehicles;
    private List<Vehicle> idleVehicles;
    // for extension 1
    private List<Vehicle> allVehicles;

    /**
     * Normal constructor used to initialize a simulator for running experiments
     *  @param graph
     * @param visual
     * @param numVehicles
     * @param lambda
     */
    public AmodSimulator(Graph graph, boolean visual, int numVehicles, ExtensionType extensionType, double lambda) {
        this.assignmentType = IndexSCRAM;
        this.extensionType = extensionType;
        IS_VISUAL = visual;
        TripPlanner.init(graph);
        this.lambda = lambda;
        requests = new ArrayList<>();

        if (extensionType == BASIC || extensionType == EXTENSION2) {
            activeVehicles = new ArrayList<Vehicle>();
            idleVehicles = Utility.generateVehicles(graph, numVehicles);
        }
        else if (extensionType == EXTENSION1 || extensionType == EXTENSION1PLUS2) {
            allVehicles = Utility.generateVehicles(graph, numVehicles);
        }

        if (IS_VISUAL) {
            setupVisuals(graph, idleVehicles);
        }
    }

    @Deprecated // should at least be updated to also use allVehicles depending on extensiontype
    /**
     * Constructor used for testing purposes with a predefined list of allVehicles and requests
     *
     * @param graph the graph to run a simulation on
     * @param visual a boolean that decides if visuals are shown or not
     * @param vehicles a predefined list of allVehicles
     * @param requestMap a mapping of timesteps -> list of requests for that timestep
     */
    public AmodSimulator(Graph graph, boolean visual, List<Vehicle> vehicles, Map<Integer, List<Request>> requestMap, AssignmentType assignmentType) {
        TEST = true;
        PRINT = false;
        this.assignmentType = assignmentType;
        IS_VISUAL = visual;
        TripPlanner.init(graph);
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
        }
        drawSprites(0);
        String styleSheet = Utility.parseStylesheet(styleSheetPath);
        graph.addAttribute("ui.stylesheet", styleSheet);
        if (!TEST) graph.display();
    }

    /**
     * What happens within a timestep:
     * 1. Check which allVehicles have been set to idle
     * 2. Assign idle allVehicles to requests
     * 3.
     *
     * @param graph
     */
    public void tick(Graph graph, int timeStep) {
        // adding new vacant allVehicles to idlevehicles, if vehicle does not have more requests
        for (Vehicle veh : vacancyMap.getOrDefault(timeStep, new ArrayList<>())) {
            makeIdle(veh);
        }
        vacancyMap.remove(timeStep);

        // adding requests for current time step:
        if (TEST) requests.addAll(predefinedRequestsMap.getOrDefault(timeStep, new ArrayList<>()));
        else requests.addAll(RequestGenerator.generateRequests(graph, lambda, timeStep));

        // assigning allVehicles to requests
        List<Edge> assignments = assign();

        // make allVehicles serve the requests they are assigned
        for (Edge e : assignments) {
            // object oriented check for dummynode
//            if (e.hasDummyNode()) continue; // if an edge has a dummynode in it, then we skip it
//            Vehicle veh = e.getVehicle();
//            Request req = e.getRequest();

            // indexbased check for dummynode
            if (e.getStartIndex() >= idleVehicles.size() || e.getEndIndex() >= requests.size()) continue;
            Vehicle veh = idleVehicles.get(e.getStartIndex());
            Request req = requests.get(e.getEndIndex());
            veh.serviceRequest(req);
            addToVacancyMap(veh);
            makeActive(veh);
            assignedRequests.add(req);
            requests.remove(req);
            if (IS_VISUAL) veh.addRequest(req);
        }

        // tracking that a request waited TODO: for extension 2 we probably don't wan't to throw out requests anymore
        ListIterator<Request> requestIterator = requests.listIterator();
        while (requestIterator.hasNext()) {
            Request r = requestIterator.next();
            r.incrementWaitCounter();
            if (r.getTicksWaitingToBeAssigned() >= 6) {
                requestIterator.remove();
                unservedRequests.add(r);
            }
        }

        if (PRINT) printVacancyMap();
        if (IS_VISUAL) drawSprites(timeStep);

        ticksDone++;
        idleVehiclesCounter += idleVehicles.size();
    }

    /**
     *
     * @return
     */
    private List<Edge> assign() {
        switch (this.extensionType) {
            case BASIC:
                return Utility.assign(assignmentType, idleVehicles, requests);
            case EXTENSION1:
                return Utility.assign(assignmentType, allVehicles, requests);   // todo: this does not work yet
            case EXTENSION2:
                return Utility.assign(assignmentType, idleVehicles, requests);  // todo: this does not work yet
            case EXTENSION1PLUS2:
                return Utility.assign(assignmentType, allVehicles, requests);   // todo: this does not work yet
        }
        throw new RuntimeException("AmodSimulator.assign() should not have gotten here");
    }

    /**
     *
     * @param timeStep
     */
    private void drawSprites(int timeStep) {
        // iterate over all allVehicles and draw sprites
        for (Vehicle veh : activeVehicles) {
            SpritePosition spritePosition = veh.findAttachment(timeStep);
            Sprite s = sman.getSprite(veh.getId());
            System.out.println("Spriteposition is: " + spritePosition.getElement().getId() + " at: " + spritePosition.getPosition());
            attachIfNeeded(s, spritePosition.getElement());
            s.setPosition(spritePosition.getPosition());
            s.setAttribute("ui.class", spritePosition.getStatus());
        }

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

    /**
     *
     * @param veh
     */
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

    /**
     *
     * @param veh
     */
    private void makeIdle(Vehicle veh) {
        activeVehicles.remove(veh);
        idleVehicles.add(veh);
        if (IS_VISUAL) veh.removeRequest();
    }

    /**
     *
     * @param veh
     */
    private void makeActive(Vehicle veh) {
        idleVehicles.remove(veh);
        activeVehicles.add(veh);
    }

    /**
     * Adds a list of allVehicles to the idle allVehicles in the simulator
     *
     * @param listOfVehicles a list of allVehicles that are added to the simulator
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

    /**
     *
     * @return
     */
    public List<Vehicle> getActiveVehicles() {
        return activeVehicles;
    }

    /**
     *
     * @return
     */
    public List<Vehicle> getIdleVehicles() {
        return idleVehicles;
    }

    /**
     *
     * @return
     */
    public int getUnoccupiedKmDriven() {
        int result = 0;

        for (Vehicle v : idleVehicles) result += v.getEmptyKilometersDriven();
        for (Vehicle v : activeVehicles) result += v.getEmptyKilometersDriven(); // TODO : should not count km's that are not within the simulation

        return result;
    }

    /**
     *
     * @return
     */
    public int getWaitingTime() {
        int result = 0;

        for (Request r : assignedRequests) result += r.getWaitTime();

        return result;
    }

    /**
     *
     * @return
     */
    public List<Request> getAssignedRequests() {
        return assignedRequests;
    }

    /**
     *
     * @return
     */
    public double getAverageIdleVehicles() {
        return (double) idleVehiclesCounter / (double) ticksDone;
    }

    /**
     *
     * @return
     */
    public List<Request> getRequests() {
        return requests;
    }

    /**
     * Gets the average percentage-wise unoccupied distance travelled by allVehicles.
     *
     * @return
     */
    public double getAvgUnoccupiedPercentage() {
        int number = 0;
        double result = 0.0;

        // TODO this needs to be updated st. it works with extension 1 and variable allVehicles

        for (Vehicle v : idleVehicles) {
            number++;
            result += (double) v.getEmptyKilometersDriven() / ((double) v.getEmptyKilometersDriven() + (double) v.getOccupiedKilometersDriven());
        }
        for (Vehicle v : activeVehicles) {
            number++;
            result += (double) v.getEmptyKilometersDriven() / ((double) v.getEmptyKilometersDriven() + (double) v.getOccupiedKilometersDriven());
        }

        boolean isNan = Double.isNaN(result);
        assert !isNan;
        boolean isNan3 = Double.isNaN(result / number);
        assert !isNan3;

        return result / (double) number;
    }

    public List<Request> getUnservedRequests() {
        return unservedRequests;
    }

    /**
     * Calculates the variance in waiting time
     *
     * @param avgWait
     * @return
     */
    public double getWaitVariance(double avgWait) {
        double waitVariance = 0.0;
        for (Request r : assignedRequests) {
            double difference = ((double) r.getWaitTime()) - avgWait;
            // System.out.println("Wait time: " + r.getWaitTime() + " is different from mean: " + avgWait + " by:" + difference);
            waitVariance += difference * difference;
        }

        // System.out.println("IM AVERAGING : " + waitVariance + " OVER " + assignedRequests.size() + " NUMBER OF REQUESTS");

        waitVariance = waitVariance / ((double) assignedRequests.size());
        // System.out.println("return wait variance of: " + waitVariance + " ticks");
        return waitVariance;
    }
}
