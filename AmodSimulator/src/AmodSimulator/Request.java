package AmodSimulator;

import SCRAM.DummyNode;
import SCRAM.Node;
import org.graphstream.graph.Path;

import static AmodSimulator.AmodSimulator.PRINT;

// todo: in Fagnant & Kockelman requests also have a departure time
public class Request implements SCRAM.Node {

    //original info:
    private int id;
    private org.graphstream.graph.Node origin;
    private org.graphstream.graph.Node destination;
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

    boolean visited;
    Node previous;

    public Request(int id, org.graphstream.graph.Node location, org.graphstream.graph.Node destination, int generationTime) {
        this.id = id;
        this.origin = location;
        this.destination = destination;
        this.generationTime = generationTime;

        if (origin == destination) try {
            throw new Exception("Request added with same origin and destination");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public org.graphstream.graph.Node getOrigin() {
        return origin;
    }

    public org.graphstream.graph.Node getDestination() {
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

    public int getGenerationTime() {
        return generationTime;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getOriginPathLength() {
        return originPathLength;
    }

    public int getDestinationPathLength() {
        return destinationPathLength;
    }

    public void setUp(int vehicleVacantTime, org.graphstream.graph.Node source, int speed) {
        // when does this request start being serviced
        // if vehicle has been vacant for some timesteps then it's generation time, otherwise (if vehicle has several requests) its vehicle vacant time
        startTime = Math.max(generationTime, vehicleVacantTime);   // math.max because of when finish time is lower than generationtime

        originPathLength = Utility.getDist(source, origin);
        destinationPathLength = Utility.getDist(origin, destination);

        originTime = (origin == source)? startTime : startTime + (int) Math.ceil(originPathLength / (double) speed) -1; //-1 because we also drive within the starttime-timestep
        //destinationTime = originTime + (int) Math.floor(destinationPathLength / speed);
        destinationTime = startTime + (int) Math.ceil((originPathLength + destinationPathLength) / (double) speed) -1; //-1 because we also drive within the starttime-timestep


        waitTime = originTime - generationTime;

        if (AmodSimulator.IS_VISUAL) {
            pathToOrigin = TripPlanner.getPath(source, origin);
            pathToDestination = TripPlanner.getPath(origin, destination);
        }

        if (PRINT) System.out.println("Request " + id + ": Start " + source.getId() + ", Origin " + origin.getId() + ", Dest " + destination.getId() + ", Time " + startTime + " to " + destinationTime);
    }

    @Override
    public int getDistance(Node node) {
        if (node instanceof Vehicle) return origin.getAttribute("distTo" + ((Vehicle) node).getLocation().getId());
        if (node instanceof DummyNode) return 0;
        try {
            throw new Exception("Request.getDistance() called with a Request as parameter");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return 0;
    }

    @Override
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    @Override
    public boolean isVisited() {
        return visited;
    }

    @Override
    public Node getPrevious() {
        return previous;
    }

    @Override
    public void setPrevious(Node previous) {
        this.previous = previous;
    }


    @Override
    public String getInfo() {
        return "Request " + id;
    }
}
