import AmodSimulator.Request;
import AmodSimulator.Vehicle;
import GraphCreator.Utility;
import SCRAM.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SCRAMTest {

    @Test
    public void SCRAMTest1() {
        Graph graph = new MultiGraph("Graph #1");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        graph.addEdge("v1r1", "v1", "r1").setAttribute("layout.weight", 2);
        graph.addEdge("v1r2", "v1", "r2").setAttribute("layout.weight", 3);
        graph.addEdge("v1r3", "v1", "r3").setAttribute("layout.weight", 5);
        graph.addEdge("v2r1", "v2", "r1").setAttribute("layout.weight", 5);
        graph.addEdge("v2r2", "v2", "r2").setAttribute("layout.weight", 1);
        graph.addEdge("v2r3", "v2", "r3").setAttribute("layout.weight", 2);
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
        //Utility.setDistances(graph);

        graph.getNode("v1").setAttribute("distTor1", 2);
        graph.getNode("v1").setAttribute("distTor2", 3);
        graph.getNode("v1").setAttribute("distTor3", 5);
        graph.getNode("v2").setAttribute("distTor1", 5);
        graph.getNode("v2").setAttribute("distTor2", 1);
        graph.getNode("v2").setAttribute("distTor3", 2);
        graph.getNode("v3").setAttribute("distTor1", 7);
        graph.getNode("v3").setAttribute("distTor2", 3);
        graph.getNode("v3").setAttribute("distTor3", 8);

//        OldSCRAM s = new OldSCRAM(vehicleList, requestList);
//        s.match();
        SCRAM s = new SCRAM(vehicleList, requestList);
        Assert.assertEquals(3, s.getLongestEdgeWeight());
    }

    @Test
    public void SCRAMTest2() {
        Graph graph = new MultiGraph("Graph #2");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        graph.addEdge("v2v1", "v2", "v1").setAttribute("layout.weight", 1);
        graph.addEdge("v2v3", "v2", "v3").setAttribute("layout.weight", 1);
        graph.addEdge("v1r1", "v1", "r1").setAttribute("layout.weight", 1);
        graph.addEdge("r2r3", "r2", "r3").setAttribute("layout.weight", 1);
        graph.addEdge("0r2", "0", "r2").setAttribute("layout.weight", 1);
        graph.addEdge("v30", "v3", "0").setAttribute("layout.weight", 1);
        graph.addEdge("r100", "r1", "00").setAttribute("layout.weight", 1);
        graph.addEdge("00r3", "00", "r3").setAttribute("layout.weight", 1);

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

//        OldSCRAM s = new OldSCRAM(vehicleList, requestList);
//        s.match();
        SCRAM s = new SCRAM(vehicleList, requestList);
        Assert.assertEquals(3, s.getLongestEdgeWeight());
    }

    @Test
    public void SCRAMTest3() {
        Graph graph = new MultiGraph("Graph #3");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        graph.addEdge("v10", "v1", "0").setAttribute("layout.weight", 1);
        graph.addEdge("v20", "v2", "0").setAttribute("layout.weight", 1);
        graph.addEdge("v3v2", "v3", "v2").setAttribute("layout.weight", 1);
        graph.addEdge("0r1", "0", "r1").setAttribute("layout.weight", 1);
        graph.addEdge("0r2", "0", "r2").setAttribute("layout.weight", 1);
        graph.addEdge("0r3", "0", "r3").setAttribute("layout.weight", 1);

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

        SCRAM s = new SCRAM(vehicleList, requestList);
        Assert.assertEquals(3, s.getLongestEdgeWeight());
    }
}
