import AmodSimulator.AmodSimulator;
import AmodSimulator.Request;
import AmodSimulator.TripPlanner;
import AmodSimulator.Vehicle;
import GraphCreator.Utility;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static AmodSimulator.AssignmentType.BRUTE_FORCE;

public class SimulatorTest {
    private Graph graph;
    private SpriteManager sman;
    private List<Vehicle> vehicles;
    private Map<Integer, List<Request>> requestMap;
    private AmodSimulator simulator;
    private int simulation1Length = 20;
    private int simulation2Length = 20;

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
            graph.addEdge("EC", "E", "C");
            for (Edge edge : graph.getEdgeSet()) edge.setAttribute("layout.weight", 4);

            // vehicles
            vehicles = new ArrayList<>();
            Vehicle v1 = new Vehicle("v1", graph.getNode("A"));
            Vehicle v2 = new Vehicle("v2", graph.getNode("D"));
            v1.setSpeed(1);
            v2.setSpeed(1);
            vehicles.add(v1);
            vehicles.add(v2);

            // requests
            requestMap = new HashMap<>();
            Request r1 = new Request(1, graph.getNode("C"), graph.getNode("A"), 0);
            // vehicle v2 gets a request with the same position as the vehicle
            Request r2 = new Request(2, graph.getNode("D"), graph.getNode("E"), 1);
            addToRequestMap(r1);
            addToRequestMap(r2);
        }
        else if (no == 2) {
            graph = new MultiGraph("graph #2");
            graph.setAutoCreate(true);
            graph.setStrict(false);
            graph.addEdge("AB", "A", "B").setAttribute("layout.weight", 1);
            graph.addEdge("CA", "C", "A").setAttribute("layout.weight", 3);
            graph.addEdge("BC", "B", "C").setAttribute("layout.weight", 2);
            graph.addEdge("CD", "C", "D").setAttribute("layout.weight", 2);

            // vehicles
            vehicles = new ArrayList<>();
            Vehicle v1 = new Vehicle("v1", graph.getNode("A"));
            Vehicle v2 = new Vehicle("v2", graph.getNode("C"));
            v1.setSpeed(2);
            v2.setSpeed(3);
            vehicles.add(v1);
            vehicles.add(v2);

            // requests
            // corner cases covered:
            // 1. very short request that can be serviced completely within one tick
            requestMap = new HashMap<>();
            Request r1 = new Request(1, graph.getNode("A"), graph.getNode("B"), 0);
            Request r2 = new Request(2, graph.getNode("D"), graph.getNode("B"), 0);
            Request r3 = new Request(3, graph.getNode("B"), graph.getNode("C"), 1);
            Request r4 = new Request(4, graph.getNode("A"), graph.getNode("C"), 3);
            addToRequestMap(r1);
            addToRequestMap(r2);
            addToRequestMap(r3);
            addToRequestMap(r4);
        }

        else try {
                throw new Exception("setup() method was not called correctly");
            } catch (Exception e) {
                e.printStackTrace();
            }

        Utility.setDistances(graph);
        TripPlanner.init(graph);
        sman = new SpriteManager(graph);
        for (Vehicle v : vehicles) sman.addSprite(v.getId());
        simulator = new AmodSimulator(graph, true, vehicles, requestMap, BRUTE_FORCE);
    }

    /**
     * Helper-method that adds a request to the request map
     *
     * @param r
     */
    private void addToRequestMap(Request r) {
        if (requestMap.containsKey(r.getGenerationTime())) {
            requestMap.get(r.getGenerationTime()).add(r);
        }
        else {
            List<Request> list = new ArrayList<>();
            list.add(r);
            requestMap.put(r.getGenerationTime(), list);
        }
    }

    /**
     * Test that is mainly concerned with checking if the vacancymap is updated correctly
     */
    @Test
    public void vacancyMapTest1() {
        setup(1);

        for (int timestep = 0; timestep < simulation1Length; timestep++) {
            simulator.tick(graph, timestep);

            if (timestep == 0) Assert.assertEquals("v1", simulator.getVacancyMap().get(16).get(0).getId());
            if (timestep == 1) Assert.assertEquals("v2", simulator.getVacancyMap().get(9).get(0).getId());

            simulator.printVacancyMap();
        }
    }

    /**
     * Test that is mainly concerned with checking if the vacancymap is updated correctly
     */
    @Test
    public void vacancyMapTest2() {
        setup(2);
        for (int timestep = 0; timestep < simulation2Length; timestep++) {
            simulator.tick(graph, timestep);
            if (timestep == 0) {
                // v1
                Assert.assertEquals("v1", simulator.getVacancyMap().get(1).get(0).getId());
                // v2
                Assert.assertEquals("v2", simulator.getVacancyMap().get(2).get(0).getId());
            }
            if (timestep == 1) Assert.assertEquals("v1", simulator.getVacancyMap().get(2).get(1).getId());
            if (timestep == 3) Assert.assertEquals("v2", simulator.getVacancyMap().get(5).get(0).getId());
        }
    }

    /**
     * Test that is mainly concerned with checking if sprites are positioned correctly
     */
    @Test
    public void spritePositionTest1() {
        setup(1);

        for (int timestep = 0; timestep < simulation1Length; timestep++) {
            simulator.tick(graph, timestep);
            if (timestep == 0) Assert.assertEquals("AB", simulator.getSman().getSprite("v1").getAttachment().getId());
            if (timestep == 3) {
                // v1 position
                Assert.assertEquals("CB", simulator.getSman().getSprite("v1").getAttachment().getId());
                Assert.assertEquals(1.0, simulator.getSman().getSprite("v1").getX(), 0.01);
                // v2 position
                Assert.assertEquals("DC", simulator.getSman().getSprite("v2").getAttachment().getId());
                Assert.assertEquals(0.75, simulator.getSman().getSprite("v2").getX(), 0.01);
            }
            if (timestep == 5) {
                // v2 position
                Assert.assertEquals("EC", simulator.getSman().getSprite("v2").getAttachment().getId());
                Assert.assertEquals(0.75, simulator.getSman().getSprite("v2").getX(), 0.01);
            }
        }
        // v1
        Assert.assertEquals("A", simulator.getSman().getSprite("v1").getAttachment().getId());
        Assert.assertEquals(0.0, simulator.getSman().getSprite("v1").getX(), 0.01);
        // v2
        Assert.assertEquals("E", simulator.getSman().getSprite("v2").getAttachment().getId());
        Assert.assertEquals(0.0, simulator.getSman().getSprite("v2").getX(), 0.01);
    }

    /**
     * Test that is mainly concerned with checking if sprites are positioned correctly
     */
    @Test
    public void spritePositionTest2() {
        setup(2);
        Assert.assertEquals("A", simulator.getSman().getSprite("v1").getAttachment().getId());
        Assert.assertEquals("C", simulator.getSman().getSprite("v2").getAttachment().getId());

        for (int timestep = 0; timestep < simulation2Length; timestep++) {
            simulator.tick(graph, timestep);

            if (timestep == 0) {
                // v1
                Assert.assertEquals("AB", simulator.getSman().getSprite("v1").getAttachment().getId());
                Assert.assertEquals(1.0, simulator.getSman().getSprite("v1").getX(), 0.01);
                // v2
                Assert.assertEquals("BC", simulator.getSman().getSprite("v2").getAttachment().getId());
                Assert.assertEquals(0.5, simulator.getSman().getSprite("v2").getX(), 0.01);
            }

            if (timestep == 1) {
                Assert.assertEquals("BC", simulator.getSman().getSprite("v2").getAttachment().getId());
                Assert.assertEquals(0.0, simulator.getSman().getSprite("v2").getX(), 0.01);
            }

            // remember, sprites are drawn in the end of a timestep, so the logic is a bit different from the vacancy thing..
            if (timestep == 3) {
                // v1
                Assert.assertEquals("C", simulator.getSman().getSprite("v1").getAttachment().getId());
                Assert.assertEquals(0.0, simulator.getSman().getSprite("v1").getX(), 0.01);
                // v2
                Assert.assertEquals("CA", simulator.getSman().getSprite("v2").getAttachment().getId());
                // fixme : position is 0.0 even though it should 0.33, maybe the problem is that it completes the trip to origin and comes further within the same timestep
                // the problem prob. comes from findattachment(), where traversedSoFar is not counted when originpath is surpassed within one timestep
                Assert.assertEquals(0.33, simulator.getSman().getSprite("v2").getX(), 0.01);
            }
            if (timestep == 4) {
                // v1
                Assert.assertEquals("C", simulator.getSman().getSprite("v1").getAttachment().getId());
                Assert.assertEquals(0.0, simulator.getSman().getSprite("v1").getX(), 0.01);
                // v2
                Assert.assertEquals("CA", simulator.getSman().getSprite("v2").getAttachment().getId());
                Assert.assertEquals(0.0, simulator.getSman().getSprite("v2").getX(), 0.01);
            }
        }
    }
}
