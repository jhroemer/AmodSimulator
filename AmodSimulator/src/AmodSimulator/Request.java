package AmodSimulator;

import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import static AmodSimulator.AmodSimulator.PRINT;

// todo: in Fagnant & Kockelman requests also have a departure time
public class Request {

    //original info:
    private int id;
    private Node origin;
    private Node destination;
    private final int generationTime;

    //info from setUp():
    private int startTime;
    private int originTime;
    private int destinationTime;
    private int waitTime;
    // todo: private int departureTime; ??

    // only for visuals
    private Path pathToOrigin;
    private Path pathToDestination;
    private int originPathLength;
    private int destinationPathLength;

    public Request(int id, Node location, Node destination, int generationTime) {
        this.id = id;
        this.origin = location;
        this.destination = destination;
        this.generationTime = generationTime;
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

    public void setUp(int vehicleVacantTime, Node source, int speed) {
        // when does this request start being serviced
        // if vehicle has been vacant for some timesteps then it's generation time, otherwise (if vehicle has several requests) its vehicle vacant time
        startTime = Math.max(generationTime, vehicleVacantTime);   // math.max because of when finish time is lower than generationtime

        pathToOrigin = TripPlanner.getPath(source, origin);
        pathToDestination = TripPlanner.getPath(origin, destination);

        //System.out.println("origin path weight is: " + pathToOrigin.getPathWeight("layout.weight"));
        // fixme : sometimes path weights are rounded to 0 which makes stuff fail
        originPathLength = (int) Math.round(pathToOrigin.getPathWeight("layout.weight"));
        destinationPathLength = (int) Math.round(pathToDestination.getPathWeight("layout.weight"));

        originTime = vehicleVacantTime + (int) Math.floor(originPathLength / speed);
        destinationTime = originTime + (int) Math.floor(destinationPathLength / speed);

        waitTime = originTime - generationTime;


        if (PRINT) System.out.println("Request " + id + ": Start " + source.getId() + ", Origin " + origin.getId() + ", Dest " + destination.getId() + ", Time " + startTime + " to " + originTime);
    }
}
