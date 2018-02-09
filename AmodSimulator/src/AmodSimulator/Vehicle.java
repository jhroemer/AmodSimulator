package AmodSimulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.util.ArrayList;
import java.util.Observable;

import static AmodSimulator.VehicleEvent.ADVANCE_NEW_EDGE;
import static AmodSimulator.VehicleEvent.ADVANCE_SAME_EDGE;
import static AmodSimulator.VehicleEvent.TRIP_COMPLETED;
import static AmodSimulator.VehicleStatus.*;

public class Vehicle extends Observable{

    private String id;
    private VehicleStatus status;
    private ArrayList<Request> requests;
    private Path currentPath;
    private Node lastNode;
    private double currentEdgeDist;
    private double speed = 0.002; //distance per timestep

    //info from last tick
    private Element lastElement;
    private int lastCurrentEdgeDist;
    private VehicleStatus lastStatus;
    private Request lastRequest;

    // Fields relevant for visual-mode
    private int startedAt;              // when did the current request start?
    private int originTimestep;         // when do we reach the current origin-node?
    private int destinationTimestep;    // when do we reach the current destination-node?
    private Path pathToOrigin;          // the path from last vehicle position to origin-node
    private Path pathToDestination;     // the path from origin-node to destination-node

    public Vehicle(String id, Node startNode, AmodSprite sprite) {
        this.id = id;
        status = IDLE;
        requests = new ArrayList<>();
        lastNode = startNode;
        currentEdgeDist = 0;

        if (AmodSimulator.IS_VISUAL) {
            sprite.setAttribute("ui.class", "idle");
            sprite.setPosition(0.0);
            sprite.attachToNode(startNode.getId());
            addObserver(sprite);
        }

    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
        setChanged();
        notifyObservers(status);
    }

    public void addRequest(Request request) {
        requests.add(request);
        request.setDestinationTime(5); // todo
    }

    private void popRequest() {
        requests.remove(0);
        currentPath = null;
    }

    private Path calcPath() {
        //todo
        return null;
    }


    /**
     * Should only run when vehicles have a current request
     *
     */
    public VehicleStatus advance() {

        if (requests.isEmpty()) try {
            throw new Exception("advance() called on a vehicle with no request");
        } catch (Exception e) { //todo implement NoRequestException
            e.printStackTrace();
        }

        currentEdgeDist += speed;
        boolean finished = false;

        Edge oldCurrent = getCurrentEdge();

        while (!finished) {
            if (this.status == IDLE) {
                currentPath = TripPlanner.getPath(lastNode, requests.get(0).getOrigin());
                setStatus(MOVING_TOWARDS_REQUEST);
            }


            double pathLength = currentPath.getPathWeight("layout.weight");

            if (this.status == MOVING_TOWARDS_REQUEST) {
                if (currentEdgeDist < pathLength) {
                    traverse();
                    finished = true;
                } else {
                    lastNode = requests.get(0).getOrigin();
                    currentEdgeDist -= pathLength;
                    currentPath = TripPlanner.getPath(lastNode, requests.get(0).getDestination());
                    setStatus(OCCUPIED);
                }


            } else if (this.status == OCCUPIED) {
                if (currentEdgeDist < pathLength) {
                    traverse();
                    finished = true;
                } else {
                    lastNode = requests.get(0).getDestination();
                    requests.remove(0);
                    if (requests.isEmpty()) {
                        setStatus(IDLE);
                        currentEdgeDist = 0.0;
                        finished = true;
                    } else {
                        currentEdgeDist -= pathLength;
                        setStatus(MOVING_TOWARDS_REQUEST);
                    }
                }
            }
        }

        if (AmodSimulator.IS_VISUAL) {
            VehicleEvent event = ADVANCE_SAME_EDGE; //putting event back to it's start status

            if (oldCurrent != getCurrentEdge() || lastRequest != getCurrentRequest()) {
                event = ADVANCE_NEW_EDGE;
            }
            if (requests.isEmpty()) { // todo: test for nullpointer
                event = TRIP_COMPLETED;
            }
            setChanged();
            notifyObservers(event);
        }

        lastRequest = getCurrentRequest();
        return this.status;
    }

    /**
     * Only to be called when currentEdgeDist is less than total length of the current path.
     */
    private void traverse() {
        while (currentEdgeDist >= (double) currentPath.getEdgePath().get(0).getAttribute("layout.weight")) {
            Edge e = currentPath.getEdgePath().remove(0);
            lastNode = e.getOpposite(lastNode);
            currentEdgeDist -= (double) e.getAttribute("layout.weight");
        }
    }

    public Node getLastNode() {
        return lastNode;
    }

    public double getCurrentEdgeDist() {
        return currentEdgeDist;
    }

    public Edge getCurrentEdge() {
        if (currentPath == null || currentPath.empty()) {
            return null;
        }
        return currentPath.getEdgePath().get(0); //returns first edge on currentPath
    }

    public Request getCurrentRequest() {
        if (requests.isEmpty()) {
            System.out.println("No requests");
            return null;
        }
        return requests.get(0);
    }

    public ArrayList<Request> getRequests() {
        return requests;
    }

    public VehicleStatus getStatus(){
        return status;
    }

    public void setSpeed(double speed) {
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
     * @param timeStep
     * @return
     */
    public int startRequest(int timeStep) {
        return 0;
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
        Request currentRequest = requests.get(0);

        // find current request
        for (Request req : requests) {
            if (timeStep > req.getDestinationTime()) continue;
            currentRequest = req;
            break;
        }

        Path path = (timeStep < currentRequest.getOriginTime()) ? currentRequest.getPathToOrigin() : currentRequest.getPathToDestination();

        double traversedSoFar = (timeStep - startedAt) * speed;

        // find out which element to attach to
        for (Edge edge : path.getEdgeSet()) {
            double edgeLength = edge.getAttribute("layout.weight");
            if (traversedSoFar > edgeLength) {
                traversedSoFar -= edgeLength;
            }
            else {
                return new SpritePosition(edge, convertToPercent(path, edge, traversedSoFar));
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
}
