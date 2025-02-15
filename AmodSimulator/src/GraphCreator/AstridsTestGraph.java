package GraphCreator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class AstridsTestGraph {

    public static void main(String[] args) {


        Graph graph = new SingleGraph("Tutorial 1");

        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addNode("F");
        graph.addEdge("AB", "A", "B");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("BD", "B", "D");
        graph.addEdge("CE", "C", "E");
        graph.addEdge("DE", "D", "E");
        graph.addEdge("EF", "E", "F");

        graph.getEdge("AB").setAttribute("layout.weight", 20);
        graph.getEdge("BC").setAttribute("layout.weight", 30);
        graph.getEdge("BD").setAttribute("layout.weight", 30);
        graph.getEdge("CE").setAttribute("layout.weight", 30);
        graph.getEdge("DE").setAttribute("layout.weight", 30);
        graph.getEdge("EF").setAttribute("layout.weight", 20);

        for (Node n : graph) n.setAttribute("ui.label", n.getId());
        for (Edge e : graph.getEdgeSet()) e.setAttribute("ui.label", e.getAttribute("layout.weight").toString());

        //setting the distances between all nodes in the graph
        Utility.setDistances(graph);

        //graph.display();
        Utility.saveCompleteGraph("AstridsTestGraph", "data/graphs", graph);
    }
}
