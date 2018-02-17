import AmodSimulator.AmodSimulator;
import AmodSimulator.Request;
import AmodSimulator.TripPlanner;
import AmodSimulator.Vehicle;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulatorTest {
    private Graph graph;
    private SpriteManager sman;
    private List<Vehicle> vehicles;
    private Map<Integer, List<Request>> requestMap;
    private AmodSimulator simulator;

    @Before
    public void setup() {
    
    }

    @After
    public void tearDown() {
        graph = null;
        sman = null;
        vehicles = null;
        requestMap = null;
        simulator = null;
    }

    /**
     * Method that sets up the graph, spritemanager and initializes the tripplanner
     * @param no an int that decides which graph to initialize
     */
    private void setup(int no) {
        if (no == 1) {
            graph = new MultiGraph("graph #1");
            graph.setAutoCreate(true);
            graph.setStrict(false);
            graph.addEdge("AB", "A", "B");
            graph.addEdge("CB", "C", "B");
            graph.addEdge("DC", "D", "C");
            graph.addEdge("EC", "C", "E");
            for (Edge edge : graph.getEdgeSet()) edge.setAttribute("layout.weight", 4);

            // vehicles
            vehicles = new ArrayList<>();
            Vehicle v1 = new Vehicle("v1", graph.getNode("A"));
            v1.setSpeed(1);
            vehicles.add(v1);
            // requests
            requestMap = new HashMap<>();
            Request r1 = new Request(1, graph.getNode("C"), graph.getNode("A"), 0);
            requestMap.getOrDefault(r1.getGenerationTime(), new ArrayList<>()).add(r1);
        }
        else if (no == 2) {
            graph = new MultiGraph("graph #2");
            graph.setAutoCreate(true);
            graph.setStrict(false);
            graph.addEdge("AB", "A", "B");
            graph.addEdge("AE", "A", "E");
            graph.addEdge("DA", "D", "A");
            graph.addEdge("EF", "E", "F");
            graph.addEdge("FC", "F", "C");
            graph.addEdge("CB", "C", "B");
            graph.addEdge("BF", "B", "F");
            graph.addEdge("GC", "G", "C");
            graph.addEdge("FH", "F", "H");
            graph.addEdge("GH", "G", "H");
            graph.addEdge("IH", "I", "H");
            for (Edge edge : graph.getEdgeSet()) {
                if (edge == graph.getEdge("EF")) {
                    edge.setAttribute("layout.weight", 3);
                    continue;
                }
                edge.setAttribute("layout.weight", 5);
            }

            // vehicles
            vehicles = new ArrayList<>();
            Vehicle v2 = new Vehicle("v2", graph.getNode("B"));
            v2.setSpeed(1);
            vehicles.add(v2);

            // requests
            requestMap = new HashMap<>();
            Request r1 = new Request(2, graph.getNode("E"), graph.getNode("D"), 1);
            requestMap.getOrDefault(r1.getGenerationTime(), new ArrayList<>()).add(r1);
        }

        else try {
                throw new Exception("setup() method was not called correctly");
            } catch (Exception e) {
                e.printStackTrace();
            }

        TripPlanner.init(graph);
        sman = new SpriteManager(graph);
        simulator = new AmodSimulator(graph, false, vehicles, requestMap);
    }

    // TODO : corner cases for testing:
    // 1. vehicle gets a request with the same position as the vehicle
    // 2. vehicle gets two requests that can be serviced within the same tick
    // 3. vehicle gets a request in the same tick as it becomes vacant again
    // 4. request is added with a wrong node?
    // 5. a set of requests are assigned within a timestep but one, that one is assigned in a later timestep to the first vehicle to finish
    // 6.

    // test the normal cases
    @Test
    public void vacancyMapTest1() {
        setup(1);
        for (int i = 1; i < 21; i++) {

        }
    }

    // test cases with several vehicles and requests that arrive at a later time
    @Test
    public void vacancyMapTest2() {
        setup(2);
        for (int i = 1; i < 17; i++) {
//            simulator.tick(graph, i);
        }
    }

    @Test
    public void positionTest1() {
        setup(2);
    }

    @Test
    public void visualTest1() {
        setup(1);
    }

    @Test
    public void visualTest2() {
        setup(2);
    }
}
