package AmodSimulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.util.ArrayList;
import java.util.Iterator;

public class Vehicle implements HungarianNode{

    private String id;
    private ArrayList<Request> requests;
    private int speed = 1; //distance per timestep
    private Node location; // The vehicles location if idle. If not idle, this is the destination of it's last request.
    private int vacantTime;

    
    //summed info of vehicle
    private int emptyKilometersDriven;
    private int occupiedKilometersDriven;
    private int numRequestServiced;
    

    public Vehicle(String id, Node startNode) {
        this.id = id;
        requests = new ArrayList<>();
        location = startNode;
        vacantTime = 0;
    }

    
    public void serviceRequest(Request request) {
        if (!requests.isEmpty()) try {
            throw new Exception("Request added to a vehicle that already had a request!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setUp(vacantTime, location, speed);

        location = request.getDestination();
        // fixme : is +1 wrong now? r1 in timestep 0 with pathlength 16 is currently vaccant in timestep 17
        // but it should get the request and start driving in timestep 0, then timestep 16 should actually be the timestep in which is is vacant again, right?
        vacantTime = request.getDestinationTime()+1;

        emptyKilometersDriven += request.getOriginPathLength();
        occupiedKilometersDriven += request.getDestinationPathLength();

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
            // TODO : remember corner-case where a vehicle can service two requests within a timestep
            if (timeStep > req.getDestinationTime()) requests.remove(req);
            else {
                currentRequest = req;
                break;
            }
            if (requests.isEmpty()) { // todo : is this necessary anymore? Don't we know that requests is not empty because otherwise findAttachment() wouldn't have been called?
                return new SpritePosition(location, 0.0, "idle");
            }
        }

//        Path path = (timeStep < currentRequest.getOriginTime()) ? currentRequest.getPathToOrigin() : currentRequest.getPathToDestination();

        Path path;
        String status;
        System.out.println("current request is: " + currentRequest.getId());
        if (timeStep < currentRequest.getOriginTime()) {
            path = currentRequest.getPathToOrigin();
            status = "moving";
        }
        else {
            path = currentRequest.getPathToDestination();
            status = "occupied";
        }

        // FIXME: traversedSoFar is sometimes larger than the last edge, which shouldn't happen.
        int traversedSoFar = (timeStep - currentRequest.getStartTime()) * speed;

        // todo : this could be a fix to the problem, when path to origin has been surpassed within the first tick, because before it wasn't included
        // fixme : but in it's current form it introduces new problems with attachments
        // if (path == currentRequest.getPathToDestination()) traversedSoFar -= currentRequest.getOriginPathLength();

        traversedSoFar += speed; // because the current timestep is also counted
        int edgeLength = 0;
        Edge currentEdge = null;


        //TODO - Dette print virker ikke pt fordi calcPathLength er flyttet til GraphCreator.Utility
        //if (traversedSoFar > Utility.calcPathLength(path)) {
        //    System.out.println("HEY: traversedSoFar: " + traversedSoFar + " path weight: " + Utility.calcPathLength(path));
        //}

        // find out which element to attach to
        for (Edge edge : path.getEdgeSet()) {
            currentEdge = edge;
            edgeLength = edge.getAttribute("layout.weight");
            if (traversedSoFar >= edgeLength) {
                traversedSoFar -= edgeLength;
            }
            else {
                return new SpritePosition(edge, convertToPercent(path, edge, traversedSoFar), status);
            }
            // if we have reached originNode exactly
            System.out.println("traversed so far is: " + traversedSoFar);
        }

        // FIXME : sometimes traverSoFar is also larger than 0
        // fixme : when given a path request with same origin and destination and position as vehicle, currentEdge is null
        if (traversedSoFar >= 0) return new SpritePosition(currentEdge, convertToPercent(path, currentEdge, edgeLength), status);

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

    public Node getLocation() {
        return location;
    }

    @Override
    public String getInfo() {
        return "Vehicle " + id;
    }
}
