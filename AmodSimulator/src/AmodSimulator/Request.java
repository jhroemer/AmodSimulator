package AmodSimulator;

import org.graphstream.graph.Node;

public class Request {
    private int id;
    private Node origin;
    private Node destination;

    public Request(int id, Node location, Node destination) {
        this.id = id;
        this.origin = location;
        this.destination = destination;
    }

    public Node getOrigin() {
        return origin;
    }

    public Node getDestination() {
        return destination;
    }

    public int getId() {
        return id;
    }
}
