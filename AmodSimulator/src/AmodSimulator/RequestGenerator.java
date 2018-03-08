package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RequestGenerator {
    private static int idCounter = 0;

    /**
     * ..
     *
     * @return a list of new request to be serviced (might be an empty list)
     * @param graph the graph we're working with
     * @param lambda average number of events per interval
     */
    public static List<Request> generateRequests(Graph graph, double lambda, int timeStep) {
        List<Request> reqList = new ArrayList<>();
        Random r = new Random();
        int noOfRequests = getPoisson(lambda);

        for (int i = 0; i < noOfRequests; i++) {
            Node originNode = graph.getNode(r.nextInt(graph.getNodeCount()));
            Node destinationNode = graph.getNode(r.nextInt(graph.getNodeCount()));

            while (originNode.equals(destinationNode)) destinationNode = graph.getNode(r.nextInt(graph.getNodeCount()));

            Request req = new Request(idCounter++, originNode, destinationNode, timeStep);
            reqList.add(req);
        }

        System.out.println(reqList.size() + " requests were generated");

        return reqList;
    }

    //TODO the poisson process does not work for large numbers (maybe the problem is overflow)
    /**
     * Knuth poisson process - runs in O(k) which can be improved
     *
     * @param lambda average number of events per interval
     * @return
     */
    private static int getPoisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        while (p > L) {
            k++;
            p *= Math.random();
        }

        return k - 1;
    }
}
