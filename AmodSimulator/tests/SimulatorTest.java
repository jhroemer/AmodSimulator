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
    private int simulation1Length = 20;
    private int simulation2Length = 20;

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
            graph.addEdge("EC", "E", "C");
            for (Edge edge : graph.getEdgeSet()) edge.setAttribute("layout.weight", 4);

            // vehicles
            vehicles = new ArrayList<>();
            Vehicle v1 = new Vehicle("v1", graph.getNode("A"));
            v1.setSpeed(1);
            Vehicle v2 = new Vehicle("v2", graph.getNode("D"));
            vehicles.add(v1);
            vehicles.add(v2);
            // requests
            requestMap = new HashMap<>();
            Request r1 = new Request(1, graph.getNode("C"), graph.getNode("A"), 0);
            // vehicle v2 gets a request with the same position as the vehicle
            Request r2 = new Request(2, graph.getNode("D"), graph.getNode("E"), 1);
//            Request r3 = new Request(3, graph.getNode("E"), graph.getNode("E"), 10); fixme : triggers a nullpointer from Vehicle.findAttachment() because currentEdge = null
            addToRequestMap(r1);
            addToRequestMap(r2);
//            addToRequestMap(r3);
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
                    edge.setAttribute("layout.weight", 1);
                    continue;
                }
                edge.setAttribute("layout.weight", 3);
            }

            // vehicles
            vehicles = new ArrayList<>();
            Vehicle v1 = new Vehicle("v1", graph.getNode("G"));
            Vehicle v2 = new Vehicle("v2", graph.getNode("B"));
            v1.setSpeed(2);
            v2.setSpeed(1);
            vehicles.add(v1);
            vehicles.add(v2);

            // requests
            requestMap = new HashMap<>();
            Request r1 = new Request(1, graph.getNode("E"), graph.getNode("D"), 1);
            Request r2 = new Request(2, graph.getNode("C"), graph.getNode("E"), 2);
            addToRequestMap(r1);
            addToRequestMap(r2);
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
        simulator = new AmodSimulator(graph, true, vehicles, requestMap);
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

    // todo : normal cases
    // 1. a vehicle gets a request at a certain timestep:
    //  - check that the position of the sprite is correct, at different timesteps especially ones where edge has changed
    // TODO : corner cases
    // 5. a set of requests are assigned within a timestep but one, that one is assigned in a later timestep to the first vehicle to finish

    /**
     * Test that is mainly concerned with checking if the vacancymap is updated correctly
     */
    @Test
    public void vacancyMapTest1() {
        setup(1);

        for (int timestep = 0; timestep < simulation1Length; timestep++) {
            simulator.tick(graph, timestep);

            if (timestep == 0) Assert.assertEquals("v1", simulator.getVacancyMap().get(16).get(0).getId());
            // fixme, currently its vacant at timestep 17
            // this is because the request has a destinationtime of 16 and vacantTime is destinationTime+1 - but is the +1 not wrong?

            if (timestep == 1) Assert.assertEquals("v2", simulator.getVacancyMap().get(10).get(0).getId());

            // todo : it takes one timestep to service a request with total length 0 - thats completely intended right?
            if (timestep == 10) Assert.assertEquals("v2", simulator.getVacancyMap().get(11).get(0).getId());
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

//            if ()
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
        Assert.assertEquals("A", simulator.getSman().getSprite("v1").getAttachment().getId());  // fixme : is attached to AB and not A
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

        for (int timestep = 0; timestep < simulation1Length; timestep++) {
            simulator.tick(graph, timestep);
            if (timestep == 5) Assert.assertEquals("AE", simulator.getSman().getSprite("v1").getAttachment().getId());
        }
    }
}
