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

import static AmodSimulator.VehicleStatus.*;
import static org.junit.Assert.assertEquals;

public class VehicleMovementTest {
    // #1
    private Graph graph1;
    // #2
    private Graph graph2;

    @Before
    public void setup() {
        // is there something common to set up?
    }

    @After
    public void tearDown() {
        graph1 = null;
        graph2 = null;
    }

    private void buildGraph1() {
        // #1
        graph1 = new MultiGraph("graph #1");
        graph1.setAutoCreate(true);
        graph1.setStrict(false);
        graph1.addEdge("AB", "A", "B");
        graph1.addEdge("CB", "C", "B");
        for (Edge edge : graph1.getEdgeSet()) edge.setAttribute("layout.weight", 2.0);
    }

    private void buildGraph2() {
        // #2
        graph2 = new MultiGraph("graph #2");
        graph2.setAutoCreate(true);
        graph2.setStrict(false);
        graph2.addEdge("AB", "A", "B");
        graph2.addEdge("CB", "C", "B");
        graph2.addEdge("DC", "D", "C");
        graph2.addEdge("EC", "C", "E");
        for (Edge edge : graph2.getEdgeSet()) edge.setAttribute("layout.weight", 4.0);
    }

    @Test
    public void positionTest1() {
        buildGraph1();
        TripPlanner.init(graph1);
        SpriteManager sman = new SpriteManager(graph1);
        AmodSprite s1 = sman.addSprite("s1", AmodSprite.class);
        Vehicle v1 = new Vehicle("v1", graph1.getNode("A"), s1);
        Request r1 = new Request(1, graph1.getNode("C"), graph1.getNode("A"));
        v1.setSpeed(1.0);
        v1.addRequest(r1);

        for (int i = 1; i < 9; i++) {
            v1.advance();
            if (i == 2) {
                assertEquals("v1 lastnode #1", "B", v1.getLastNode().getId());
                assertEquals("Status check #1", MOVING_TOWARDS_REQUEST, v1.getStatus());
            }

            if (i == 3) {
                assertEquals("Sprite position #1", 0.5, s1.getX(), 0.01);
            }
        }

        assertEquals("Sprite last attachment #2" ,"A", s1.getAttachment().getId());
        assertEquals("V1 lastnode #2" ,"A", v1.getLastNode().getId());
    }

    @Test
    public void positionTest2() {
        buildGraph2();
        TripPlanner.init(graph2);
        SpriteManager sman = new SpriteManager(graph2);
        AmodSprite s2 = sman.addSprite("s2", AmodSprite.class);
        Vehicle v2 = new Vehicle("v2", graph2.getNode("B"), s2);
        v2.setSpeed(1.0);
        Request r2 = new Request(2, graph2.getNode("E"), graph2.getNode("D"));
        v2.addRequest(r2);

        for (int i = 1; i < 17; i++) {
            v2.advance();
            if (i == 1) assertEquals(0.75, s2.getX(), 0.01);
            if (i == 5) assertEquals(0.25, s2.getX(), 0.01);
            if (i == 13) assertEquals(0.75, s2.getX(), 0.01);
        }
    }

    @Test
    public void vehicleStatusTest1() {
        buildGraph1();
        TripPlanner.init(graph1);
        SpriteManager sman = new SpriteManager(graph1);
        AmodSprite s1 = sman.addSprite("s1", AmodSprite.class);
        Vehicle v1 = new Vehicle("v2", graph1.getNode("B"), s1);
        v1.setSpeed(1.0);
        Request r1 = new Request(2, graph2.getNode("E"), graph2.getNode("D"));


    }

    @Test
    public void vehicleStatusTest2() {
        buildGraph2();
        TripPlanner.init(graph2);
        SpriteManager sman = new SpriteManager(graph2);
        AmodSprite s1 = sman.addSprite("s1", AmodSprite.class);
        Vehicle v1 = new Vehicle("v2", graph2.getNode("B"), s1);
        v1.setSpeed(1.0);
        Request r1 = new Request(2, graph2.getNode("E"), graph2.getNode("D"));

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
}
