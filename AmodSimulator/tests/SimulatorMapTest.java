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
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimulatorMapTest {
    private Graph graph;
    private SpriteManager sman;

    @Before
    public void setup() {
        // some common thing to setup?
        // TODO: should we split up the tests, so that some tests are only concerned with sprites?
    }

    /**
     * Method that sets up the graph, spritemanager and initializes the tripplanner
     * @param no an int that decides which graph to initialize
     */
    private void setupGraph(int no) {
        if (no == 0) return;
        if (no == 1) {
            graph = new MultiGraph("graph #1");
            graph.setAutoCreate(true);
            graph.setStrict(false);
            graph.addEdge("AB", "A", "B");
            graph.addEdge("CB", "C", "B");
            for (Edge edge : graph.getEdgeSet()) edge.setAttribute("layout.weight", 2);
        }
        else if (no == 2) {
            graph = new MultiGraph("graph #2");
            graph.setAutoCreate(true);
            graph.setStrict(false);
            graph.addEdge("AB", "A", "B");
            graph.addEdge("CB", "C", "B");
            graph.addEdge("DC", "D", "C");
            graph.addEdge("EC", "C", "E");
            for (Edge edge : graph.getEdgeSet()) edge.setAttribute("layout.weight", 4);
        }
        else if (no == 3) {
            graph = new MultiGraph("graph #3");
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
        }

        TripPlanner.init(graph);
        sman = new SpriteManager(graph);
    }

    @After
    public void tearDown() {
        graph = null;
        sman = null;
    }

    // TODO : corner cases for testing:
    // 1. vehicle gets a request with the same position as the vehicle
    // 2. vehicle gets two requests that can be serviced within the same tick
    // 3. vehicle gets a request in the same tick as it becomes vacant again
    // 4. request is added with a wrong node?
    // 5.

    @Test
    public void positionTest1() {
        setupGraph(1);
        Vehicle v1 = new Vehicle("v1", graph.getNode("A"));
        Request r1 = new Request(1, graph.getNode("C"), graph.getNode("A"), 0);
        v1.setSpeed(1);
        v1.addRequest(r1);
        AmodSimulator simulator = new AmodSimulator(graph, false);

        Vehicle v2 = new Vehicle("v2", graph.getNode("A"));
        v2.addRequest(r1);
        v2.setSpeed(8);
    }

    @Test
    public void positionTest2() {
        setupGraph(2);
        Vehicle v2 = new Vehicle("v2", graph.getNode("B"));
        v2.setSpeed(1);
        Request r2 = new Request(2, graph.getNode("E"), graph.getNode("D"), 1);
        v2.addRequest(r2);

        for (int i = 1; i < 17; i++) {
        }
    }

    @Test
    public void positionTest3() {
        setupGraph(3);
        Vehicle v1 = new Vehicle("v1", graph.getNode("A")); //sman.addSprite("s1", AmodSprite.class));
        Vehicle v2 = new Vehicle("v2", graph.getNode("B"));
        v1.setSpeed(2);
        v2.setSpeed(2);
        Request r1 = new Request(1, graph.getNode("I"), graph.getNode("D"), 1);
        Request r2 = new Request(2, graph.getNode("C"), graph.getNode("G"), 1);
        v1.addRequest(r1);
        v2.addRequest(r2);
        List<Vehicle> vehicleList = new ArrayList<>();
        vehicleList.add(v1);
        vehicleList.add(v2);
    }

    @Test
    public void vehicleStatusTest1() {
        setupGraph(1);
        Vehicle v1 = new Vehicle("v2", graph.getNode("B"));
        v1.setSpeed(1);
        Request r1 = new Request(2, graph.getNode("E"), graph.getNode("D"), 1);
        // todo finish this test / or is it too simple and redundant? Rather make test 2 the first test case?
    }

    @Test
    public void vehicleStatusTest2() {
        setupGraph(2);
        Vehicle v1 = new Vehicle("v2", graph.getNode("B"));
        v1.setSpeed(1);
        Request r1 = new Request(2, graph.getNode("E"), graph.getNode("D"), 1);
        assertEquals(true, v1.getRequests().isEmpty());
        v1.addRequest(r1);
        assertEquals(false, v1.getRequests().isEmpty());
    }

    @Test
    public void currentPathTest1() {

        // TODO : move into a new test called lastnode/attachment test
        setupGraph(1);
        Vehicle v1 = new Vehicle("v1", graph.getNode("A"));
        v1.setSpeed(1);
        Request r1 = new Request(1, graph.getNode("A"), graph.getNode("C"), 1);
        v1.addRequest(r1);
    }

    @Test
    public void currentPathTest2() {

    }

    @Test
    public void currentPathTest3() {

    }
}
