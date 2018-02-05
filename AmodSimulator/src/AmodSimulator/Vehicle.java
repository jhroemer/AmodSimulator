package AmodSimulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.util.ArrayList;
import java.util.Observable;

import static AmodSimulator.VehicleEvent.*;
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
    private int speed = 1; //distance per timestep


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
            sprite.addAttribute("ui.label", id); // todo label is positioned weirdly atm, should be fixed
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
        if (requests.isEmpty()) try {
            throw new Exception("advance() called on a vehicle with no request");
        } catch (Exception e) { //todo implement NoRequestException
            e.printStackTrace();
        }

        VehicleStatus status = null;
        VehicleEvent event = null;

        currentEdgeDist += speed;

        if (this.status == IDLE) {


            currentPath = TripPlanner.getPath(lastNode, requests.get(0).getOrigin());
        }

        TRIP_STARTED:
        ADVANCE_NEW_EDGE:
        ADVANCE_SAME_EDGE:
        PICKED_UP:
        TRIP_COMPLETED:
        //todo

        if (AmodSimulator.IS_VISUAL) {
            setChanged();
            notifyObservers(event);
        }
        return status;
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
        return requests.get(0);
    }
}
