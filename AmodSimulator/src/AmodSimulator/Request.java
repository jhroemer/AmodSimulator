package AmodSimulator;

import org.graphstream.graph.Node;

public class Request {
    private int id;
    private Node location;
    private Node destination;

    public Request(int id, Node location, Node destination) {
        this.id = id;
        this.location = location;
        this.destination = destination;
    }

    public Node getLocation() {
        return location;
    }

    public Node getDestination() {
        return destination;
    }

    public int getId() {
        return id;
    }
}
