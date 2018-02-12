package AmodSimulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.util.ArrayList;
import java.util.Observable;

import static AmodSimulator.AmodSimulator.IS_VISUAL;

public class Vehicle extends Observable{

    private String id;
    private ArrayList<Request> requests;
    private int speed = 1; //distance per timestep
    private Node location; // The vehicles location if idle. If not idle, this is the destination of it's last request.
    private int finishTime;
    
    //summed info of vehicle
    private int emptyKilometersDriven;
    private int occupiedKilometersDriven;
    private int numRequestServiced;
    

    public Vehicle(String id, Node startNode) {
        this.id = id;
        requests = new ArrayList<>();
        location = startNode;
    }

    
    public int addRequest(Request request) {
        if (!requests.isEmpty()) try {
            throw new Exception("Request added to a vehicle that already had a request!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        requests.add(request);

        request.setUp(finishTime, location, speed);

        location = request.getDestination();
        finishTime = request.getDestinationTime();
        
        emptyKilometersDriven += (int) Math.round(request.getPathToOrigin().getPathWeight("layout.weight"));
        occupiedKilometersDriven += (int) Math.round(request.getPathToDestination().getPathWeight("layout.weight"));
        numRequestServiced++;
        
        
        if (IS_VISUAL) {
            // todo Add info to request
        }
        
        //todo calc and return arrivaltime
        return 0;
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
        Request currentRequest = requests.get(0);

        // find current request
        for (Request req : requests) {
            if (timeStep > req.getDestinationTime()) continue;
            currentRequest = req;
            break;
        }

        Path path = (timeStep < currentRequest.getOriginTime()) ? currentRequest.getPathToOrigin() : currentRequest.getPathToDestination();

        double traversedSoFar = (timeStep - currentRequest.getStartTime()) * speed;

        // find out which element to attach to
        for (Edge edge : path.getEdgeSet()) {
            double edgeLength = edge.getAttribute("layout.weight");
            if (traversedSoFar > edgeLength) {
                traversedSoFar -= edgeLength;
            }
            else {
                // todo : set status dynamically
                return new SpritePosition(edge, convertToPercent(path, edge, traversedSoFar), "occupied");
            }
        }

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

    public int getFinishTime() {
        return finishTime;
    }
}
