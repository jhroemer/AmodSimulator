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
    public static ExtensionType extensionType = BASIC;
    private AssignmentType assignmentType;
    private boolean TEST = false;
    private List<Request> requests;
    private Map<Integer, List<Vehicle>> vacancyMap = new HashMap<>();
    private List<Request> assignedRequests = new ArrayList<>();
    private List<Request> unservedRequests = new ArrayList<>();
    private Map<Integer, List<Request>> predefinedRequestsMap;
    private double lambda = 0.1;
    private int ticksDone;
    private int idleVehiclesCounter = 0; // keeps track of the amount of idlevehicles in simulation
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

        // use two- or a single list for vehicles, depending on extension
        if (extensionType == BASIC || extensionType == EXTENSION2) {
            activeVehicles = new ArrayList<Vehicle>();
            idleVehicles = Utility.generateVehicles(graph, numVehicles);
        }
        else if (extensionType == EXTENSION1 || extensionType == EXTENSION1PLUS2) {
            allVehicles = Utility.generateVehicles(graph, numVehicles);
        }

        if (IS_VISUAL) { // TODO : update w. allvehicles
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
        idleVehicles = vehicles;
        activeVehicles = new ArrayList<>();
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
        // adding requests for current time step:
        if (TEST) requests.addAll(predefinedRequestsMap.getOrDefault(timeStep, new ArrayList<>())); // TODO ALL
        else requests.addAll(RequestGenerator.generateRequests(graph, lambda, timeStep));


        switch (AmodSimulator.extensionType) {
            case BASIC:
                tickBasic(graph, timeStep);
                break;
            case EXTENSION1:
                tickExt1(graph, timeStep);
                break;
            case EXTENSION2:
                tickExt2(graph, timeStep);
                break;
            case EXTENSION1PLUS2:
                tickExt1Plus2(graph, timeStep);
                break;
        }

        if (PRINT) printVacancyMap();
        if (IS_VISUAL) drawSprites(timeStep);

        ticksDone++;
    }

    /**
     *
     * @param graph
     * @param timeStep
     */
    private void tickBasic(Graph graph, int timeStep) {
        // adding new vacant allVehicles to idlevehicles, if vehicle does not have more requests
        for (Vehicle veh : vacancyMap.getOrDefault(timeStep, new ArrayList<>())) {
            makeIdle(veh); // TODO ONLY BASIC AND EXT 2
        }

        vacancyMap.remove(timeStep);

        // assigning allVehicles to requests
        List<Edge> assignments = Utility.assign(assignmentType, idleVehicles, requests, timeStep);

        // make allVehicles serve the requests they are assigned
        for (Edge e : assignments) {
            // indexbased check for dummynode // TODO DIFFERENT BETWEEN EXTENSIONS
            if (e.getStartIndex() >= idleVehicles.size() || e.getEndIndex() >= requests.size()) continue;
            Vehicle veh = idleVehicles.get(e.getStartIndex());
            Request req = requests.get(e.getEndIndex());

            veh.serviceRequest(req);
            addToVacancyMap(veh, 0, timeStep);

            makeActive(veh);
            assignedRequests.add(req);
            requests.remove(req);
            if (IS_VISUAL) veh.addRequest(req);
        }

        // tracking that a request waited
        trackRequestsWaiting();

        idleVehiclesCounter += idleVehicles.size();
    }

    /**
     *
     * @param graph
     * @param timeStep
     */
    private void tickExt1(Graph graph, int timeStep) {
        vacancyMap.remove(timeStep);

        // assigning allVehicles to requests
        List<Edge> assignments = Utility.assign(assignmentType, allVehicles, requests, timeStep);

        // make allVehicles serve the requests they are assigned
        for (Edge e : assignments) {
            // indexbased check for dummynode
            // if the nodeindex is larger than the size of it's original list, then it was added = is a dummynode
            if (e.getStartIndex() >= allVehicles.size() || e.getEndIndex() >= requests.size()) continue;
            Vehicle veh = allVehicles.get(e.getStartIndex());
            Request req = requests.get(e.getEndIndex());

            int oldVacancyTime = veh.getVacantTime();
            veh.serviceRequest(req);
            addToVacancyMap(veh, oldVacancyTime, timeStep);

            assignedRequests.add(req);
            requests.remove(req);
            if (IS_VISUAL) veh.addRequest(req);
        }

        // tracking that a request waited TODO: for extension 2 we probably don't wan't to throw out requests anymore
        trackRequestsWaiting();

        // todo : do I still wan't to check idlevehicles?
        // idleVehiclesCounter += idleVehicles.size();
    }

    /**
     *
     */
    private void trackRequestsWaiting() {
        ListIterator<Request> requestIterator = requests.listIterator();
        while (requestIterator.hasNext()) {
            Request r = requestIterator.next();
            r.incrementWaitCounter();
            if (r.getTicksWaitingToBeAssigned() >= 6) { // todo: only if we're not on extension 2
                requestIterator.remove();
                unservedRequests.add(r);
            }
        }
    }

    /**
     *
     * @param graph
     * @param timeStep
     */
    private void tickExt2(Graph graph, int timeStep) {
        throw new RuntimeException("tickExt2() method not implemented yet!");
    }

    /**
     *
     * @param graph
     * @param timeStep
     */
    private void tickExt1Plus2(Graph graph, int timeStep) {
        throw new RuntimeException("tickExt1Plus2() method not implemented yet!");
    }

    /**
     *
     * @return
     */
    private List<Edge> assign(int timeStep) {
        switch (AmodSimulator.extensionType) {
            case BASIC:
                return Utility.assign(assignmentType, idleVehicles, requests, timeStep);
            case EXTENSION1:
                return Utility.assign(assignmentType, allVehicles, requests, timeStep);   // todo: this does not work yet
            case EXTENSION2:
                return Utility.assign(assignmentType, idleVehicles, requests, timeStep);  // todo: this does not work yet
            case EXTENSION1PLUS2:
                return Utility.assign(assignmentType, allVehicles, requests, timeStep);   // todo: this does not work yet
        }
        throw new RuntimeException("AmodSimulator.assign() should not have gotten here");
    }

    /**
     *
     * @param graph
     */
    public void drawSpritesDebug(Graph graph) {
        sman = new SpriteManager(graph);
        String styleSheet = Utility.parseStylesheet(styleSheetPath);
        graph.addAttribute("ui.stylesheet", styleSheet);

        int num = 0;
        for (Vehicle veh : allVehicles) {
            num++;
            Sprite s = sman.addSprite(String.valueOf(num));
            s.setAttribute("ui.class", "idle");
            s.attachToNode(veh.getLocation().getId());
        }
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
    private void addToVacancyMap(Vehicle veh, int oldVacancyTime, int timeStep) {
        // if the old vacancy time was larger than timestep, it means its still active - therefore we have to remove it from the vacancymap
        if (AmodSimulator.extensionType == EXTENSION1 || AmodSimulator.extensionType == EXTENSION1PLUS2) {
            if (oldVacancyTime > timeStep) vacancyMap.get(oldVacancyTime).remove(veh);
        }

        int updatedVacancyTime = veh.getVacantTime();

        if (vacancyMap.containsKey(updatedVacancyTime)) vacancyMap.get(updatedVacancyTime).add(veh);
        else {
            ArrayList<Vehicle> list = new ArrayList<Vehicle>();
            list.add(veh);

            vacancyMap.put(updatedVacancyTime, list);
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

        if (AmodSimulator.extensionType == EXTENSION1 || AmodSimulator.extensionType == EXTENSION1PLUS2) {
            for (Vehicle v : allVehicles) result += v.getEmptyKilometersDriven();
        }
        else {
            for (Vehicle v : idleVehicles) result += v.getEmptyKilometersDriven();
            for (Vehicle v : activeVehicles) result += v.getEmptyKilometersDriven(); // TODO : should not count km's that are not within the simulation
        }

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
        int numVehicles;
        double result = 0.0;
        boolean debug = false;

        if (AmodSimulator.extensionType == EXTENSION1 || AmodSimulator.extensionType == EXTENSION1PLUS2) {
            numVehicles = allVehicles.size();
            for (Vehicle v : allVehicles) {
                if (debug) {
                    System.out.println("v.getEmpty: " + (double) v.getEmptyKilometersDriven() + " v.getOccupied: " + (double) v.getOccupiedKilometersDriven());
                    System.out.println("percentage: " + (double) v.getEmptyKilometersDriven() / ((double) v.getEmptyKilometersDriven() + (double) v.getOccupiedKilometersDriven()));
                }

                // if the vehicle didn't drive at all, it is not counted
                if (v.getEmptyKilometersDriven() == 0 && v.getOccupiedKilometersDriven() == 0) {
                    numVehicles--;
                    continue;
                }
                                   // no. of empty km               <<<<<<<<<<<<<<<<<<<<<<<<<<   total km driven      >>>>>>>>>>>>>>>>>>>>>>>>>>
                result += (double) v.getEmptyKilometersDriven() / ((double) v.getEmptyKilometersDriven() + (double) v.getOccupiedKilometersDriven());
                System.out.println("and result: " + result);
            }
        }
        else {
            numVehicles = idleVehicles.size() + activeVehicles.size();

            for (Vehicle v : idleVehicles) {
                if (v.getEmptyKilometersDriven() == 0 && v.getOccupiedKilometersDriven() == 0) {
                    numVehicles--;
                    continue;
                }
                System.out.println();
                result += (double) v.getEmptyKilometersDriven() / ((double) v.getEmptyKilometersDriven() + (double) v.getOccupiedKilometersDriven());
            }
            for (Vehicle v : activeVehicles) {
                if (v.getEmptyKilometersDriven() == 0 && v.getOccupiedKilometersDriven() == 0) {
                    numVehicles--;
                    continue;
                }
                result += (double) v.getEmptyKilometersDriven() / ((double) v.getEmptyKilometersDriven() + (double) v.getOccupiedKilometersDriven());
            }
        }
        if (debug) System.out.println("numvehicles is: " + numVehicles);

        if (numVehicles == 0.0 || Double.isNaN((double) numVehicles) || Double.isNaN(result) || Double.isNaN(result / (double) numVehicles)) {
            double test = result / (double) numVehicles;
            System.out.println("numVehicles: " + numVehicles + " result: " + result + " percentage: " + test);
            throw new RuntimeException("something went wrong w. getAvgUnoccupiedPercentage");
        }

        return result / (double) numVehicles;
    }

    public List<Request> getUnservedRequests() {
        return unservedRequests;
    }

    /**
     * Calculates the variance in waiting time
     *
     *
     * @param avgWait
     * @param intervalSizeMinutes
     * @return
     */
    public double getWaitVariance(double avgWait, int intervalSizeMinutes) {
        double waitVariance = 0.0;
        for (Request r : assignedRequests) {
            // multiply by the interval-size to get values in minutes instead of ticks
            double waitTimeInMinutes = (double) r.getWaitTime() * (double) intervalSizeMinutes;
            double difference = waitTimeInMinutes - avgWait;
            // System.out.println("Wait time: " + waitTimeInMinutes + " is different from mean: " + avgWait + " by:" + difference);
            double squaredDifference = difference * difference;
            // System.out.println("squared difference is: " + squaredDifference);
            waitVariance += squaredDifference;
        }

        // System.out.println("IM AVERAGING : " + waitVariance + " OVER " + assignedRequests.size() + " NUMBER OF REQUESTS");

        waitVariance = waitVariance / (double) assignedRequests.size();
        // System.out.println("return wait variance of: " + waitVariance + " minutes");
        // System.out.println("If I square it, then the value is: " + Math.sqrt(waitVariance));
        return waitVariance;
    }
}
