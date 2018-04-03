package AmodSimulator;

import SCRAM.DummyNode;
import SCRAM.Node;
import org.graphstream.graph.Path;

import static AmodSimulator.AmodSimulator.PRINT;
import static AmodSimulator.ExtensionType.EXTENSION1;
import static AmodSimulator.ExtensionType.EXTENSION1PLUS2;

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
    // for SCRAM
    private boolean visited;
    private Node previous;
    private int ticksWaitingToBeAssigned;

    /**
     *
     * @param id
     * @param location
     * @param destination
     * @param generationTime
     */
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

    /**
     *
     * @return
     */
    public org.graphstream.graph.Node getOrigin() {
        return origin;
    }

    /**
     *
     * @return
     */
    public org.graphstream.graph.Node getDestination() {
        return destination;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param destinationTime
     */
    public void setDestinationTime(int destinationTime) {
        this.destinationTime = destinationTime;
    }

    /**
     *
     * @return
     */
    public int getDestinationTime() {
        return destinationTime;
    }

    /**
     *
     * @return
     */
    public int getOriginTime() {
        return originTime;
    }

    /**
     *
     * @return
     */
    public Path getPathToDestination() {
        return pathToDestination;
    }

    /**
     *
     * @return
     */
    public Path getPathToOrigin() {
        return pathToOrigin;
    }

    /**
     *
     * @return
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     *
     * @return
     */
    public int getGenerationTime() {
        return generationTime;
    }

    /**
     *
     * @return
     */
    public int getWaitTime() {
        return waitTime;
    }

    /**
     *
     * @return
     */
    public int getOriginPathLength() {
        return originPathLength;
    }

    /**
     *
     * @return
     */
    public int getDestinationPathLength() {
        return destinationPathLength;
    }

    /**
     *
     * @param vehicleVacantTime
     * @param source
     * @param speed
     */
    public void setUp(int vehicleVacantTime, org.graphstream.graph.Node source, int speed) {
        // when does this request start being serviced
        // if vehicle has been vacant for some timesteps then it's generation time, otherwise (if vehicle has several requests) its vehicle vacant time
        startTime = Math.max(generationTime, vehicleVacantTime);   // math.max because of when finish time is lower than generationtime

        originPathLength = Utility.getDist(source, origin);
        destinationPathLength = Utility.getDist(origin, destination);

        // when is origin and destination reached?
        originTime = (origin == source) ? startTime : startTime + (int) Math.ceil(originPathLength / (double) speed) -1; //-1 because we also drive within the starttime-timestep
        destinationTime = startTime + (int) Math.ceil((originPathLength + destinationPathLength) / (double) speed) -1; //-1 because we also drive within the starttime-timestep

        // WAITtotal = WAITmatching + WAITtravel, min-cost-matching p. 4
        waitTime = originTime - generationTime;

        if (AmodSimulator.IS_VISUAL) {
            pathToOrigin = TripPlanner.getPath(source, origin);
            pathToDestination = TripPlanner.getPath(origin, destination);
        }

        if (PRINT) System.out.println("Request " + id + ": Start " + source.getId() + ", Origin " + origin.getId() + ", Dest " + destination.getId() + ", Time " + startTime + " to " + destinationTime);
    }

    /**
     * This is called from SCRAM, and is used to set the weight of the edge between the Vehicle- and Request node
     * in the bipartite matching graph.
     *
     * @param node
     * @param timeStep
     * @return
     */
    @Override
    public int getDistance(Node node, int timeStep) {
        if (node instanceof Vehicle) {
            // TODO : is this actually called on requests also?
            int distanceUntilVacant = 0;

            // for extension 1, if the vehicle is not vacant now, we add the distance that is left until the vehicle is vacant
            if (AmodSimulator.extensionType == EXTENSION1 || AmodSimulator.extensionType == EXTENSION1PLUS2) {
                if (timeStep <= ((Vehicle) node).getVacantTime()) distanceUntilVacant = ((Vehicle) node).getDistanceUntilVacant(timeStep);
            }
            return distanceUntilVacant + (int) origin.getAttribute("distTo" + ((Vehicle) node).getLocation().getId());
        }
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

    @Override
    public Node getType() {
        return null;
    }

    /**
     * Used for canceling requests
     */
    public void incrementWaitCounter() {
        ticksWaitingToBeAssigned++;
    }

    /**
     *
     * @return
     */
    public int getTicksWaitingToBeAssigned() {
        assert ticksWaitingToBeAssigned <= 6;
        return ticksWaitingToBeAssigned;
    }
}
