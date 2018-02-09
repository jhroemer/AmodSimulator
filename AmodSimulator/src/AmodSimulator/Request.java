package AmodSimulator;

import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

// todo: in Fagnant & Kockelman requests also have a departure time
public class Request {

    //original info:
    private int id;
    private Node origin;
    private Node destination;

    //info from setUp():
    private int startTime;
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

    public int getStartTime() {
        return startTime;
    }

    public void setUp(int startTime, Node source, double speed) {
        this.startTime = startTime;

        pathToOrigin = TripPlanner.getPath(source,origin);
        pathToDestination = TripPlanner.getPath(origin,destination);

        //originTime
        //destinationTIme

        //waitTime NB: waittime consists of both empty driving time AND time from requestgeneration and request assignment

    }
}
