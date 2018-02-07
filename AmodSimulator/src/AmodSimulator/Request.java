package AmodSimulator;

import org.graphstream.graph.Node;

// todo: in Fagnant & Kockelman requests also have a departure time
public class Request {
    private int id;
    private Node origin;
    private Node destination;
    // todo: private int departureTime; ??

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
