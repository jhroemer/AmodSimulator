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

    static final boolean PRINT = true;
    private static String styleSheetPath = "styles/style.css";
    private int numVehicles = 10;
    private boolean IS_VISUAL = true;
    private List<Vehicle> activeVehicles;
    private List<Vehicle> idleVehicles;
    private List<Request> requests;
    private Map<Integer,List<Vehicle>> vacancyMap = new HashMap<>();
    private SpriteManager sman;
    private List<Request> assignedRequests = new ArrayList<>();

    public AmodSimulator(Graph graph, boolean visual) {

        IS_VISUAL = visual;
        TripPlanner.init(graph);

        activeVehicles = new ArrayList<>();
        idleVehicles = Utility.generateVehicles(graph, numVehicles);
        requests = new ArrayList<>();

        if (IS_VISUAL) {
            sman = new SpriteManager(graph);
            for (Vehicle v : idleVehicles) sman.addSprite(v.getId());
            String styleSheet = Utility.parseStylesheet(styleSheetPath);
            graph.addAttribute("ui.stylesheet", styleSheet);
            graph.display();
        }
        //--------------------//
        //generating controlled vehicles and requests
        //todo this is for testing the simulator. To be removed later.
        idleVehicles = new ArrayList<>();
        idleVehicles.add(new Vehicle("v1", graph.getNode("A")));
        idleVehicles.add(new Vehicle("v2", graph.getNode("F")));
        if (IS_VISUAL) {
            sman.addSprite("v1");
            sman.addSprite("v2");
        }
        requests.add(new Request(1,graph.getNode("B"),graph.getNode("F"),0));
        requests.add(new Request(2,graph.getNode("E"),graph.getNode("A"),0));
        requests.add(new Request(3,graph.getNode("F"),graph.getNode("D"),0));
        requests.add(new Request(4,graph.getNode("A"),graph.getNode("B"),0));
        requests.add(new Request(5,graph.getNode("C"),graph.getNode("D"),0));
        requests.add(new Request(6,graph.getNode("D"),graph.getNode("C"),0));


    }



    /**
     * What happens within a timestep:
     * 1. Check which vehicles have been set to idle
     * 2. Assign idle vehicles to requests
     * 3.
     *
     * @param graph
     */
    void tick(Graph graph, int timeStep) {
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
        requests.addAll(RequestGenerator.generateRequests(graph,0.1, timeStep));

        //assigning vehicles to requests
        Map<Vehicle, Request> assignments = Utility.assign(idleVehicles,requests);
        
        for (Vehicle veh : assignments.keySet()) {
            Request req = assignments.get(veh);
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

    private void drawSprites(int timeStep) {
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




    public void printVacancyMap() {
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
