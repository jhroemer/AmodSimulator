import org.graphstream.graph.Edge;

/**
 * Created by Jens on 12/02/2018.
 */
public class TimeTest {
    @Test
    public void 
    graph = new MultiGraph("graph #2");
            graph.setAutoCreate(true);
            graph.setStrict(false);
            graph.addEdge("AB", "A", "B");
            graph.addEdge("CB", "C", "B");
            graph.addEdge("DC", "D", "C");
            graph.addEdge("EC", "C", "E");
            for (Edge edge : graph.getEdgeSet()) edge.setAttribute("layout.weight", 4.0);


}
