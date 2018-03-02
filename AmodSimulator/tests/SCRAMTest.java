import AmodSimulator.Request;
import AmodSimulator.Vehicle;
import GraphCreator.Utility;
import SCRAM.Edge;
import SCRAM.Node;
import SCRAM.OldSCRAM;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SCRAMTest {

    @Test
    public void SCRAMTest1() {
        Graph graph = new MultiGraph("Graph #1");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        graph.addEdge("v1r1", "v1", "r1").setAttribute("layout.weight", 2);
        graph.addEdge("v1r2", "v1", "r2").setAttribute("layout.weight", 3);
        graph.addEdge("v1r3", "v1", "r3").setAttribute("layout.weight", 6);
        graph.addEdge("v2r1", "v2", "r1").setAttribute("layout.weight", 5);
        graph.addEdge("v2r2", "v2", "r2").setAttribute("layout.weight", 1);
        graph.addEdge("v2r3", "v2", "r3").setAttribute("layout.weight", 10);
        graph.addEdge("v3r1", "v3", "r1").setAttribute("layout.weight", 7);
        graph.addEdge("v3r2", "v3", "r2").setAttribute("layout.weight", 3);
        graph.addEdge("v3r3", "v3", "r3").setAttribute("layout.weight", 8);

        Vehicle v1 = new Vehicle("v1", graph.getNode("v1"));
        Vehicle v2 = new Vehicle("v2", graph.getNode("v2"));
        Vehicle v3 = new Vehicle("v3", graph.getNode("v3"));
        List<Node> vehicleList = new ArrayList<>();
        vehicleList.add(v1);
        vehicleList.add(v2);
        vehicleList.add(v3);

        Request r1 = new Request(1, graph.getNode("r1"), null, 0);
        Request r2 = new Request(2, graph.getNode("r2"), null, 0);
        Request r3 = new Request(3, graph.getNode("r3"), null, 0);
        List<Node> requestList = new ArrayList<>();
        requestList.add(r1);
        requestList.add(r2);
        requestList.add(r3);
//        Utility.setDistances(graph);

        graph.getNode("v1").setAttribute("distTor1", 2);
        graph.getNode("v1").setAttribute("distTor2", 3);
        graph.getNode("v1").setAttribute("distTor3", 6);
        graph.getNode("v2").setAttribute("distTor1", 5);
        graph.getNode("v2").setAttribute("distTor2", 1);
        graph.getNode("v2").setAttribute("distTor3", 10);
        graph.getNode("v3").setAttribute("distTor1", 7);
        graph.getNode("v3").setAttribute("distTor2", 3);
        graph.getNode("v3").setAttribute("distTor3", 8);

        OldSCRAM s = new OldSCRAM(vehicleList, requestList);
        Assert.assertEquals(6, s.getLongestEdgeWeight());
//        SCRAM s2 = new SCRAM(vehicleList, requestList);
//        Assert.assertEquals(3, s2.getLongestEdgeWeight());

        List<Edge> sortedAssignmentList = new ArrayList<>(s.getAssignments());
        Collections.sort(sortedAssignmentList);

        // object-based SCRAM
        Assert.assertEquals(v3, sortedAssignmentList.get(0).getStartNode());
        Assert.assertEquals(r2, sortedAssignmentList.get(0).getEndNode());
        Assert.assertEquals(v2, sortedAssignmentList.get(1).getStartNode());
        Assert.assertEquals(r1, sortedAssignmentList.get(1).getEndNode());
        Assert.assertEquals(v1, sortedAssignmentList.get(2).getStartNode());
        Assert.assertEquals(r3, sortedAssignmentList.get(2).getEndNode());

//        // TODO : array-based also
    }

    @Test
    public void SCRAMTest2() {
        Graph graph = new MultiGraph("Graph #2");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        graph.addEdge("v1r1", "v1", "r1").setAttribute("layout.weight", 1);
        graph.addEdge("v1r2", "v1", "r2").setAttribute("layout.weight", 5);
        graph.addEdge("v1r3", "v1", "r3").setAttribute("layout.weight", 9);
        graph.addEdge("v2r1", "v2", "r1").setAttribute("layout.weight", 3);
        graph.addEdge("v2r2", "v2", "r2").setAttribute("layout.weight", 7);
        graph.addEdge("v2r3", "v2", "r3").setAttribute("layout.weight", 10);
        graph.addEdge("v3r1", "v3", "r1").setAttribute("layout.weight", 13);
        graph.addEdge("v3r2", "v3", "r2").setAttribute("layout.weight", 15);
        graph.addEdge("v3r3", "v3", "r3").setAttribute("layout.weight", 17);
//        Utility.setDistances(graph);

        graph.getNode("v1").setAttribute("distTor1", 1);
        graph.getNode("v1").setAttribute("distTor2", 5);
        graph.getNode("v1").setAttribute("distTor3", 9);
        graph.getNode("v2").setAttribute("distTor1", 3);
        graph.getNode("v2").setAttribute("distTor2", 7);
        graph.getNode("v2").setAttribute("distTor3", 10);
        graph.getNode("v3").setAttribute("distTor1", 13);
        graph.getNode("v3").setAttribute("distTor2", 15);
        graph.getNode("v3").setAttribute("distTor3", 17);

        Vehicle v1 = new Vehicle("v1", graph.getNode("v1"));
        Vehicle v2 = new Vehicle("v2", graph.getNode("v2"));
        Vehicle v3 = new Vehicle("v3", graph.getNode("v3"));
        List<Node> vehicleList = new ArrayList<>();
        vehicleList.add(v1);
        vehicleList.add(v2);
        vehicleList.add(v3);

        Request r1 = new Request(1, graph.getNode("r1"), null, 0);
        Request r2 = new Request(2, graph.getNode("r2"), null, 0);
        Request r3 = new Request(3, graph.getNode("r3"), null, 0);
        List<Node> requestList = new ArrayList<>();
        requestList.add(r1);
        requestList.add(r2);
        requestList.add(r3);

        OldSCRAM s = new OldSCRAM(vehicleList, requestList);
        Assert.assertEquals(13, s.getLongestEdgeWeight());
//        SCRAM s2 = new SCRAM(vehicleList, requestList);
//        Assert.assertEquals(3, s2.getLongestEdgeWeight());

        List<Edge> sortedAssignmentList = new ArrayList<>(s.getAssignments());
        Collections.sort(sortedAssignmentList);

        // object-based SCRAM
        Assert.assertEquals(v1, sortedAssignmentList.get(0).getStartNode());
        Assert.assertEquals(r2, sortedAssignmentList.get(0).getEndNode());
        Assert.assertEquals(v2, sortedAssignmentList.get(1).getStartNode());
        Assert.assertEquals(r3, sortedAssignmentList.get(1).getEndNode());
        Assert.assertEquals(v3, sortedAssignmentList.get(2).getStartNode());
        Assert.assertEquals(r1, sortedAssignmentList.get(2).getEndNode());

        // TODO add tests for array-based SCRAM
    }

    @Test
    public void SCRAMTest3() {
        Graph graph = new MultiGraph("Graph #3");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        graph.addEdge("v1r1", "v1", "r1").setAttribute("layout.weight", 1);
        graph.addEdge("v1r2", "v1", "r2").setAttribute("layout.weight", 5);
        graph.addEdge("v1r3", "v1", "r3").setAttribute("layout.weight", 9);
        graph.addEdge("v2r1", "v2", "r1").setAttribute("layout.weight", 3);
        graph.addEdge("v2r2", "v2", "r2").setAttribute("layout.weight", 7);
        graph.addEdge("v2r3", "v2", "r3").setAttribute("layout.weight", 10);
        graph.addEdge("v3r1", "v3", "r1").setAttribute("layout.weight", 13);
        graph.addEdge("v3r2", "v3", "r2").setAttribute("layout.weight", 15);
        graph.addEdge("v3r3", "v3", "r3").setAttribute("layout.weight", 17);
//        Utility.setDistances(graph);

        graph.getNode("v1").setAttribute("distTor1", 1);
        graph.getNode("v1").setAttribute("distTor2", 4);
        graph.getNode("v1").setAttribute("distTor3", 6);
        graph.getNode("v2").setAttribute("distTor1", 2);
        graph.getNode("v2").setAttribute("distTor2", 5);
        graph.getNode("v2").setAttribute("distTor3", 5000);
        graph.getNode("v3").setAttribute("distTor1", 3);
        graph.getNode("v3").setAttribute("distTor2", 2000);
        graph.getNode("v3").setAttribute("distTor3", 3500);

        Vehicle v1 = new Vehicle("v1", graph.getNode("v1"));
        Vehicle v2 = new Vehicle("v2", graph.getNode("v2"));
        Vehicle v3 = new Vehicle("v3", graph.getNode("v3"));
        List<Node> vehicleList = new ArrayList<>();
        vehicleList.add(v1);
        vehicleList.add(v2);
        vehicleList.add(v3);

        Request r1 = new Request(1, graph.getNode("r1"), null, 0);
        Request r2 = new Request(2, graph.getNode("r2"), null, 0);
        Request r3 = new Request(3, graph.getNode("r3"), null, 0);
        List<Node> requestList = new ArrayList<>();
        requestList.add(r1);
        requestList.add(r2);
        requestList.add(r3);

        OldSCRAM s = new OldSCRAM(vehicleList, requestList);
        Assert.assertEquals(6, s.getLongestEdgeWeight());
//        SCRAM s2 = new SCRAM(vehicleList, requestList);
//        Assert.assertEquals(3, s2.getLongestEdgeWeight());

        List<Edge> sortedAssignmentList = new ArrayList<>(s.getAssignments());
        Collections.sort(sortedAssignmentList);

        // object-based SCRAM
        Assert.assertEquals(v3, sortedAssignmentList.get(0).getStartNode());
        Assert.assertEquals(r1, sortedAssignmentList.get(0).getEndNode());
        Assert.assertEquals(v2, sortedAssignmentList.get(1).getStartNode());
        Assert.assertEquals(r2, sortedAssignmentList.get(1).getEndNode());
        Assert.assertEquals(v1, sortedAssignmentList.get(2).getStartNode());
        Assert.assertEquals(r3, sortedAssignmentList.get(2).getEndNode());

        // TODO add tests for array-based SCRAM
    }

    @Test
    public void SCRAMTest4() {
        Graph graph = new MultiGraph("Graph #4");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        graph.addEdge("v1r1", "v1", "r1").setAttribute("layout.weight", 2);
        graph.addEdge("v1r2", "v1", "r2").setAttribute("layout.weight", 7);
        graph.addEdge("v1r3", "v1", "r3").setAttribute("layout.weight", 8);
        graph.addEdge("v2r1", "v2", "r1").setAttribute("layout.weight", 3);
        graph.addEdge("v2r2", "v2", "r2").setAttribute("layout.weight", 6);
        graph.addEdge("v2r3", "v2", "r3").setAttribute("layout.weight", 9);
        graph.addEdge("v3r1", "v3", "r1").setAttribute("layout.weight", 3);
        graph.addEdge("v3r2", "v3", "r2").setAttribute("layout.weight", 7);
        graph.addEdge("v3r3", "v3", "r3").setAttribute("layout.weight", 10);

        Vehicle v1 = new Vehicle("v1", graph.getNode("v1"));
        Vehicle v2 = new Vehicle("v2", graph.getNode("v2"));
        Vehicle v3 = new Vehicle("v3", graph.getNode("v3"));
        List<Node> vehicleList = new ArrayList<>();
        vehicleList.add(v1);
        vehicleList.add(v2);
        vehicleList.add(v3);

        Request r1 = new Request(1, graph.getNode("r1"), null, 0);
        Request r2 = new Request(2, graph.getNode("r2"), null, 0);
        Request r3 = new Request(3, graph.getNode("r3"), null, 0);
        List<Node> requestList = new ArrayList<>();
        requestList.add(r1);
        requestList.add(r2);
        requestList.add(r3);
        Utility.setDistances(graph);

        OldSCRAM s = new OldSCRAM(vehicleList, requestList);
        Assert.assertEquals(8, s.getLongestEdgeWeight());
//        SCRAM s2 = new SCRAM(vehicleList, requestList);
//        Assert.assertEquals(3, s2.getLongestEdgeWeight());

        List<Edge> sortedAssignmentList = new ArrayList<Edge>(s.getAssignments());
        Collections.sort(sortedAssignmentList);

        // checking if assignments are done correctly
        Assert.assertEquals(v3, sortedAssignmentList.get(0).getStartNode());
        Assert.assertEquals(r1, sortedAssignmentList.get(0).getEndNode());
        Assert.assertEquals(v2, sortedAssignmentList.get(1).getStartNode());
        Assert.assertEquals(r2, sortedAssignmentList.get(1).getEndNode());
        Assert.assertEquals(v1, sortedAssignmentList.get(2).getStartNode());
        Assert.assertEquals(r3, sortedAssignmentList.get(2).getEndNode());

        // TODO
    }
}
