import AmodSimulator.AmodSprite;
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
import java.util.Iterator;
import java.util.List;

import static AmodSimulator.VehicleStatus.*;
import static org.junit.Assert.assertEquals;

public class VehicleMovementTest {
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
            for (Edge edge : graph.getEdgeSet()) edge.setAttribute("layout.weight", 2.0);
        }
        else if (no == 2) {
            graph = new MultiGraph("graph #2");
            graph.setAutoCreate(true);
            graph.setStrict(false);
            graph.addEdge("AB", "A", "B");
            graph.addEdge("CB", "C", "B");
            graph.addEdge("DC", "D", "C");
            graph.addEdge("EC", "C", "E");
            for (Edge edge : graph.getEdgeSet()) edge.setAttribute("layout.weight", 4.0);
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
                    edge.setAttribute("layout.weight", 3.0);
                    continue;
                }
                edge.setAttribute("layout.weight", 5.0);
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

    @Test
    public void positionTest1() {
        setupGraph(1);
        AmodSprite s1 = sman.addSprite("s1", AmodSprite.class);
        AmodSprite s2 = sman.addSprite("s2", AmodSprite.class);
        Vehicle v1 = new Vehicle("v1", graph.getNode("A"), s1);
        Request r1 = new Request(1, graph.getNode("C"), graph.getNode("A"));
        v1.setSpeed(1.0);
        v1.addRequest(r1);

        Vehicle v2 = new Vehicle("v2", graph.getNode("A"), s2);
        v2.addRequest(r1);
        v2.setSpeed(8.0);

        for (int i = 1; i < 10; i++) {
            if (!v1.getRequests().isEmpty()) v1.advance();
            if (!v2.getRequests().isEmpty()) v2.advance();

            if (i == 1) {
                assertEquals("v2 lastnode #1", "A", v2.getLastNode().getId());
                assertEquals("v2 status check #1", IDLE, v2.getStatus());
                assertEquals("v2 sprite last attachment", graph.getNode("A"), s2.getAttachment()); // fixme
            }

            if (i == 2) {
                assertEquals("v1 lastnode #1", "B", v1.getLastNode().getId());
                assertEquals("v1 status check #1", MOVING_TOWARDS_REQUEST, v1.getStatus());
            }

            if (i == 3) {
                assertEquals("sprite s1 position #1", 0.5, s1.getX(), 0.01);
            }
        }

        assertEquals("sprite s1 last attachment #2" ,"A", s1.getAttachment().getId());
        assertEquals("v1 lastnode #2" ,"A", v1.getLastNode().getId());
    }

    @Test
    public void positionTest2() {
        setupGraph(2);
        AmodSprite s2 = sman.addSprite("s2", AmodSprite.class);
        Vehicle v2 = new Vehicle("v2", graph.getNode("B"), s2);
        v2.setSpeed(1.0);
        Request r2 = new Request(2, graph.getNode("E"), graph.getNode("D"));
        v2.addRequest(r2);

        for (int i = 1; i < 17; i++) {
            v2.advance();
            if (i == 1) assertEquals(0.75, s2.getX(), 0.01);
            if (i == 5) assertEquals(0.25, s2.getX(), 0.01);
            if (i == 13) assertEquals(0.75, s2.getX(), 0.01);
        }
    }

    @Test
    public void positionTest3() {
        setupGraph(3);
        AmodSprite s1 = sman.addSprite("s1", AmodSprite.class);
        AmodSprite s2 = sman.addSprite("s2", AmodSprite.class);
        Vehicle v1 = new Vehicle("v1", graph.getNode("A"), s1); //sman.addSprite("s1", AmodSprite.class));
        Vehicle v2 = new Vehicle("v2", graph.getNode("B"), s2);
        v1.setSpeed(2.0);
        v2.setSpeed(2.0);
        Request r1 = new Request(1, graph.getNode("I"), graph.getNode("D"));
        Request r2 = new Request(2, graph.getNode("C"), graph.getNode("G"));
        v1.addRequest(r1);
        v2.addRequest(r2);
        List<Vehicle> vehicleList = new ArrayList<>();
        vehicleList.add(v1);
        vehicleList.add(v2);

        for (int i = 1; i < 501; i++) {
            Iterator<Vehicle> vehicleIterator = vehicleList.iterator();
            while (vehicleIterator.hasNext()) {
                Vehicle veh = vehicleIterator.next();
                if (veh.getCurrentRequest() == null) vehicleIterator.remove();
                else veh.advance();
            }

            if (i == 2) {
                assertEquals(graph.getEdge("AE"), s1.getAttachment());
                assertEquals(graph.getEdge("CB"), s2.getAttachment());
            }
            if (i == 3) {
                // #1
                assertEquals(0.33, s1.getX(), 0.01);
                // #2
                assertEquals(graph.getNode("C"), v2.getLastNode());
                assertEquals(graph.getEdge("GC"), s2.getAttachment());
                assertEquals(0.8, s2.getX(), 0.01);
            }
            if (i == 4) {
                // #1
                assertEquals(graph.getNode("F"), v1.getLastNode());
                assertEquals(0.0, s1.getX(), 0.01);
                // #2
                assertEquals(0.4, s2.getX(), 0.01);
            }
        }
    }

    @Test
    public void vehicleStatusTest1() {
        setupGraph(1);
        AmodSprite s1 = sman.addSprite("s1", AmodSprite.class);
        Vehicle v1 = new Vehicle("v2", graph.getNode("B"), s1);
        v1.setSpeed(1.0);
        Request r1 = new Request(2, graph.getNode("E"), graph.getNode("D"));
        // todo finish this test / or is it too simple and redundant? Rather make test 2 the first test case?
    }

    @Test
    public void vehicleStatusTest2() {
        setupGraph(2);
        AmodSprite s1 = sman.addSprite("s1", AmodSprite.class);
        Vehicle v1 = new Vehicle("v2", graph.getNode("B"), s1);
        v1.setSpeed(1.0);
        Request r1 = new Request(2, graph.getNode("E"), graph.getNode("D"));

        assertEquals(true, v1.getRequests().isEmpty());
        v1.addRequest(r1);
        assertEquals(false, v1.getRequests().isEmpty());
        assertEquals(IDLE, v1.getStatus());

        for (int i = 1; i < 17; i++) {
            v1.advance();
            if (i == 7) assertEquals(MOVING_TOWARDS_REQUEST, v1.getStatus());
            if (i == 8) assertEquals(OCCUPIED, v1.getStatus());
             if (i == 16) assertEquals(IDLE, v1.getStatus());
        }
    }

    @Test
    public void currentPathTest1() {

        // TODO : move into a new test called lastnode/attachment test
        setupGraph(1);
        AmodSprite s1 = sman.addSprite("s1", AmodSprite.class);
        Vehicle v1 = new Vehicle("v1", graph.getNode("A"), s1);
        v1.setSpeed(1.0);
        Request r1 = new Request(1, graph.getNode("A"), graph.getNode("C"));
        v1.addRequest(r1);

        for (int i = 1; i < 10; i++) {
            if (i == 5) v1.addRequest(new Request(2, graph.getNode("C"), graph.getNode("B")));

            if (!v1.getRequests().isEmpty()) v1.advance();

            if (i == 4) {
                assertEquals(graph.getNode("C"), v1.getLastNode());
                assertEquals(graph.getNode("C"), s1.getAttachment());
                assertEquals(v1.getStatus(), IDLE);
            }

            if (i == 5) {
                assertEquals(graph.getEdge("CB"), v1.getCurrentEdge());
                assertEquals(graph.getEdge("CB"), s1.getAttachment());
            }
        }
    }

    @Test
    public void currentPathTest2() {

    }

    @Test
    public void currentPathTest3() {

    }
}
