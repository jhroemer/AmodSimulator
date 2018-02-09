package AmodSimulator;

import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

// todo: in Fagnant & Kockelman requests also have a departure time
public class Request {
    private int id;
    private Node origin;
    private Node destination;
    private int originTime;
    private int destinationTime;
    private int waitTime;
    // todo: private int departureTime; ??

    // only for visuals
    private Path pathToOrigin;
    private Path pathToDestination;

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

    public void setDestinationTime(int destinationTime) {
        this.destinationTime = destinationTime;
    }

    public int getDestinationTime() {
        return destinationTime;
    }

    public int getOriginTime() {
        return originTime;
    }

    public Path getPathToDestination() {
        return pathToDestination;
    }

    public Path getPathToOrigin() {
        return pathToOrigin;
    }
}
