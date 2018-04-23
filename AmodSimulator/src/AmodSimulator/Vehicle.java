package AmodSimulator;

import SCRAM.DummyNode;
import SCRAM.Node;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Path;

import java.util.ArrayList;
import java.util.Iterator;

import static AmodSimulator.ExtensionType.EXTENSION1;
import static AmodSimulator.ExtensionType.EXTENSION1PLUS2;
import static AmodSimulator.ExtensionType.EXTENSION2;

public class Vehicle implements SCRAM.Node {
    private String id;
    private ArrayList<Request> requests;
    private int speed = 5; //distance per timestep, 5*12 = 60kmh
    private org.graphstream.graph.Node location; // The vehicles location if idle. If not idle, this is the destination of it's last request.
    private int vacantTime;

    //summed info of vehicle
    private int emptyKilometersDriven = 0;
    private int occupiedKilometersDriven = 0;
    private int numRequestServiced = 0;
    private boolean visited;
    private Node previous;


    public Vehicle(String id, org.graphstream.graph.Node startNode) {
        this.id = id;
        requests = new ArrayList<>();
        location = startNode;
        vacantTime = 0;
    }

    
    public void serviceRequest(Request request, int timeStep) {
        if (!requests.isEmpty()) try {
            throw new Exception("Request added to a vehicle that already had a request!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // fixme: a vehicle had a vacanttime that was lower than timestep
        // somehow it ended up with a new vacant time that was lower than timestep, which shouldn't happen

//        System.out.println("vehicle " + id + " serviced request " + request.getId() + " w. previous vacant time: " + vacantTime);
        request.setUp(timeStep, vacantTime, location, speed);

        location = request.getDestination();
        vacantTime = request.getDestinationTime()+1;

        emptyKilometersDriven += request.getOriginPathLength();
        occupiedKilometersDriven += request.getDestinationPathLength();
//        System.out.println("new vacant time for vehicle is: " + vacantTime + " after driving a path of length: " + (request.getOriginPathLength() + request.getDestinationPathLength()));
        numRequestServiced++;
    }

    public ArrayList<Request> getRequests() {
        return requests;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }


    /**
     *
     * @return true if vehicle has more requests
     */
    public boolean hasMoreRequests() {
        return !requests.isEmpty();
    }


    public String getId() {
        return id;
    }

    /**
     *  findAttachment() only runs on vehicles that are in service.
     *  therefore it will never find a node as an attachment.
     *  If we're on the timestep when the vehicle is done, it will be on the idle list, and will be drawn at its nextDestination/nextNode field
     *
     * @return
     * @param timeStep current timestep in simulation
     */
    public SpritePosition findAttachment(int timeStep) {
        Request currentRequest = null;

        Iterator<Request> requestIterator = requests.iterator();
        while (requestIterator.hasNext()) {
            Request req = requestIterator.next();
            if (timeStep > req.getDestinationTime()) requests.remove(req);
            else {
                currentRequest = req;
                break;
            }
            // todo : is this necessary anymore? Don't we know that requests is not empty because otherwise findAttachment() wouldn't have been called?
            if (requests.isEmpty()) return new SpritePosition(location, 0.0, "idle");
        }

        Path path;
        String status;
        System.out.println("current request is: " + currentRequest.getId());
        int traversedOnCurrentPath;

        if (timeStep < currentRequest.getOriginTime()) {
            path = currentRequest.getPathToOrigin();
            status = "moving";
            traversedOnCurrentPath = (timeStep - currentRequest.getStartTime()) * speed + speed;
        } else {
            path = currentRequest.getPathToDestination();
            status = "occupied";
            // corner case when originpath has been traversed within the first timestep
            if (timeStep == currentRequest.getStartTime()) traversedOnCurrentPath = speed - currentRequest.getOriginPathLength();
            else traversedOnCurrentPath = (timeStep - currentRequest.getStartTime()) * speed + speed;
        }

        int edgeLength = 0;
        Edge currentEdge = null;

        // find out which element to attach to
        for (Edge edge : path.getEdgeSet()) {
            currentEdge = edge;
            edgeLength = edge.getAttribute("layout.weight");
            if (traversedOnCurrentPath >= edgeLength) traversedOnCurrentPath -= edgeLength;
            else return new SpritePosition(edge, convertToPercent(path, edge, traversedOnCurrentPath), status);
        }

        // fixme : when given a path request with same origin and destination and position as vehicle, currentEdge is null
        if (traversedOnCurrentPath >= 0) return new SpritePosition(currentEdge, convertToPercent(path, currentEdge, edgeLength), status);

        System.out.println("THIS SHOULDN'T HAPPEN! REWORK-RAT WILL BE ANGRY!");
        return null;
    }

    private double convertToPercent(Path path, Edge edge, int traversedSoFar) {
        double percent = ((double) traversedSoFar) / (int) edge.getAttribute("layout.weight");

        // if the source node of the edge has a lower index in path than the target node, then convert to percent normally
        if (path.getNodePath().indexOf(edge.getSourceNode()) < path.getNodePath().indexOf(edge.getTargetNode())) {
            return percent;
        }
        // else the percent has to be reversed
        else return 1.0 - percent;
    }

    public int getVacantTime() {
        return vacantTime;
    }

    public void removeRequest() {
        requests.remove(0);
    }

    public void addRequest(Request request) {
        requests.add(request);
    }

    public org.graphstream.graph.Node getLocation() {
        return location;
    }

    public int getEmptyKilometersDriven() {
        return emptyKilometersDriven;
    }

    public int getOccupiedKilometersDriven() {
        return occupiedKilometersDriven;
    }

    public int getNumRequestServiced() {
        return numRequestServiced;
    }

    @Override
    /**
     * Returns how far a vehicle is from a node.
     *
     * For extension 1, if the vehicle is currently active (timestep <= vacantTime) then we also get the exact distance
     * left to travel before the vehicle is vacant, because we want to consider it when assigning requests.
     */
    public int getDistance(Node node, int timeStep) {
        if (node instanceof Request) {
            int distanceUntilVacant = 0;
            double discountFactor = 1.0;

            if (AmodSimulator.extensionType == EXTENSION1 || AmodSimulator.extensionType == EXTENSION1PLUS2) {
                if (timeStep < vacantTime) {
                    distanceUntilVacant = getDistanceUntilVacant(timeStep);
                }
            }
            else if (AmodSimulator.extensionType == EXTENSION2) {
                int numTicks = ((Request) node).getTicksWaitingToBeAssigned();
                discountFactor = discountFactor - (numTicks * 0.15); //
            }
            int withoutDistount = distanceUntilVacant + (int) location.getAttribute("distTo" + ((Request) node).getOrigin().getId());
            int withDiscount = (int) Math.round(discountFactor * (double) withoutDistount);

            return withDiscount;
            // return distanceUntilVacant + (int) location.getAttribute("distTo" + ((Request) node).getOrigin().getId());
        }
        if (node instanceof DummyNode) return 0;
        try {
            throw new Exception("Vehicle.getDistance() called with a Vehicle as parameter");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return 0;
    }

    /**
     * How much distance is left to travel before this vehicle is vacant.
     * Should never be called on a vehicle that is idle!
     * 
     * @param timeStep
     * @return
     */
    public int getDistanceUntilVacant(int timeStep) {
        return (vacantTime - timeStep) * speed;
    }

    /**
     *
     * @return
     */
    public int getSpeed() {
        return speed;
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
        return "Vehicle " + id;
    }

    @Override
    public Node getType() {
        return null;
    }
}
