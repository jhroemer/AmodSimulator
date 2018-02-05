package AmodSimulator;

import org.graphstream.graph.Edge;
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
    //private AmodSprite sprite;

    //private boolean hasPassenger;
    //private boolean isActive;

    private ArrayList<Request> requests;
    private Path currentPath;
    private Node lastNode; // hvis isActive == false, så position = lastNode
    private double currentEdgeDist;
    private double speed = 0.07; //distance per timestep

    //Astrid: prøver lige at flytte VehicleEvent som bruges i advance herop
    private VehicleEvent event = ADVANCE_SAME_EDGE;

    public Vehicle(String id, Node startNode, AmodSprite sprite) {
        this.id = id;

        //this.hasPassenger = false;
        //this.isActive = false;
        this.status = IDLE;

        this.requests = new ArrayList<>();
        this.lastNode = startNode;

        this.currentEdgeDist = 0;

        if (AmodSimulator.IS_VISUAL) {
            //this.sprite = sprite;
            sprite.setAttribute("ui.class", "idle");
            //sprite.addAttribute("ui.label", id); // todo label is positioned weirdly atm, should be fixed
            sprite.setPosition(0.0);
            sprite.attachToNode(lastNode.getId());
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
    public VehicleStatus advance() { //todo consider implementing using Enum VehicleStatus
        //System.out.println("In advance()");


        if (requests.isEmpty()) try {
            throw new Exception("advance() called on a vehicle with no request");
        } catch (Exception e) { //todo implement NoRequestException
            e.printStackTrace();
        }

        currentEdgeDist += speed;
        //VehicleStatus status = null;
        //VehicleEvent event = ADVANCE_SAME_EDGE;
        boolean finished = false;


        while (!finished) {
            //System.out.println("Not finished. Status = " + status);
            if (this.status == IDLE) {
                currentPath = TripPlanner.getPath(lastNode, requests.get(0).getOrigin());
                setStatus(MOVING_TOWARDS_REQUEST);
                event = ADVANCE_NEW_EDGE;
            }
//            System.out.println("pathweight: " + currentPath.getPathWeight("layout.weight"));

            double pathLength = currentPath.getPathWeight("layout.weight");

            //System.out.println("| Whats left of currentPath:");
            //for (Edge e : currentPath.getEdgeSet()) {
            //    System.out.println("| \tEdge "+ e.getId()+ ", weight = " + e.getAttribute("layout.weight"));
            //}


            if (this.status == MOVING_TOWARDS_REQUEST) {
                if (currentEdgeDist < pathLength) {
                    traverse();
                    finished = true;
                } else {
                    lastNode = requests.get(0).getOrigin();
                    currentEdgeDist -= pathLength;
                    currentPath = TripPlanner.getPath(lastNode,requests.get(0).getDestination()); //Astrid: Dette var den manglende linie
                    setStatus(OCCUPIED);
                }


            } else if (this.status == OCCUPIED) {
                //System.out.println("currentEdgeDist = " + currentEdgeDist);
                //System.out.println("pathLength = " + pathLength);
                //System.out.println("currentPath = " + currentPath);
                if (currentEdgeDist < pathLength) {
                    traverse();
                    finished = true;
                } else {
                    lastNode = requests.get(0).getDestination();
                    requests.remove(0);
                    if (requests.isEmpty()) {
                        setStatus(IDLE);
                        currentEdgeDist = 0.0;
                        event = TRIP_COMPLETED;
                        finished = true;
                    } else {
                        currentEdgeDist -= pathLength;
                        setStatus(MOVING_TOWARDS_REQUEST);
                    }
                }
            }
        }


        /*
            if (currentPath.empty()) { //todo test if the method empty() returns what we believe
                if (this.status == MOVING_TOWARDS_REQUEST) {
                    currentPath = TripPlanner.getPath(lastNode, requests.get(0).getDestination());
                    this.status = OCCUPIED;
                }
                else if (this.status == OCCUPIED) {
                    requests.remove(0);
                    if (requests.isEmpty()) {
                        this.status = IDLE;
                        currentEdgeDist = 0.0;
                    }
                    else {
                        currentPath = TripPlanner.getPath(lastNode,requests.get(0).getOrigin());
                        this.status = MOVING_TOWARDS_REQUEST;
                    }

                }
            }
        */

        if (AmodSimulator.IS_VISUAL) {
            setChanged();
            notifyObservers(event);
        }
        event = ADVANCE_SAME_EDGE; //putting event back to it's start status
        return this.status;
    }

    /**
     * Only to be called when currentEdgeDist is less than total length of the current path.
     */
    private void traverse() {
        while (currentEdgeDist >= (double) currentPath.getEdgePath().get(0).getAttribute("layout.weight")) {
            //System.out.println("--------------current path before remove: " + currentPath);
            //System.out.println("--------------current EdgeSet before remove: " + currentPath.getEdgeSet());
            Edge e = currentPath.getEdgePath().remove(0);
            //System.out.println("--------------removed edge: " + e.getId());
            //System.out.println("--------------current path after remove: " + currentPath);
            //System.out.println("--------------current EdgeSet after remove: " + currentPath.getEdgeSet());
            lastNode = e.getOpposite(lastNode);
            currentEdgeDist -= (double) e.getAttribute("layout.weight");
            this.event = ADVANCE_NEW_EDGE;
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

    public VehicleStatus getStatus(){
        return status;
    }
}
