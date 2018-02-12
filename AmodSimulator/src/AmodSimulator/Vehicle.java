package AmodSimulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

public class Vehicle extends Observable{

    private String id;
    private ArrayList<Request> requests;
    private int speed = 5; //distance per timestep
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
        vacantTime = request.getDestinationTime()+1;
        
        emptyKilometersDriven += (int) Math.round(request.getPathToOrigin().getPathWeight("layout.weight"));
        occupiedKilometersDriven += (int) Math.round(request.getPathToDestination().getPathWeight("layout.weight"));
        numRequestServiced++;
    }

    public ArrayList<Request> getRequests() {
        return requests;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    //////////NEW METHODS FOR NON-VISUAL EXPERIMENTS///////////////

    public void arrive() {
        System.out.println("arrive() is not implemented!");
        //TODO should arrive set some more fields to the vehicles
    }

    /**
     *
     * @return true if vehicle has more requests
     */
    public boolean hasMoreRequests() {
        return !requests.isEmpty();
    }

    /**
     * Takes a timestep
     *
     * @param //timeStep
     * @return
     */
    //public int startRequest(int timeStep) {
        
    //    return 0;
    //}

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
            if (requests.isEmpty()) {
                return new SpritePosition(location, 0.0, "idle");
            }
        }

//        Path path = (timeStep < currentRequest.getOriginTime()) ? currentRequest.getPathToOrigin() : currentRequest.getPathToDestination();

        Path path;
        String status;
        System.out.println("current request is: " + currentRequest);
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
        traversedSoFar += speed; // because the current timestep is also counted
        int edgeLength = 0;
        Edge currentEdge = null;

        if (traversedSoFar > path.getPathWeight("layout.weight")) {
            System.out.println("HEY: traversedSoFar: " + traversedSoFar + " path weight: " + path.getPathWeight("layout.weight"));
        }

        // find out which element to attach to
        for (Edge edge : path.getEdgeSet()) {
            currentEdge = edge;
            edgeLength = (int) Math.round((double) edge.getAttribute("layout.weight"));
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
        if (traversedSoFar >= 0) return new SpritePosition(currentEdge, convertToPercent(path, currentEdge, edgeLength), status);

        System.out.println("THIS SHOULDN'T HAPPEN! REWORK-RAT WILL BE ANGRY!");
        return null;
    }

    private double convertToPercent(Path path, Edge edge, double traversedSoFar) {
        double percent = traversedSoFar / (double) edge.getAttribute("layout.weight");
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
}
