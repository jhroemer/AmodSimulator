package AmodSimulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.util.ArrayList;
import java.util.Observable;

public class Vehicle extends Observable{

    private String id;
    private AmodSprite sprite;

    private boolean hasPassenger;
    private boolean isActive;

    private ArrayList<Request> requests;
    private Path currentPath;
    private Node lastNode; // hvis isActive == false, så position = lastNode
    private double currentEdgeDist;
    private int speed = 1;


    public Vehicle(String id, Node startNode, AmodSprite sprite) {
        this.id = id;

        this.hasPassenger = false;
        this.isActive = false;

        this.requests = new ArrayList<>();
        this.lastNode = startNode;

        this.currentEdgeDist = 0;

        if (AmodSimulator.IS_VISUAL) {
            this.sprite = sprite;
            sprite.setAttribute("ui.class", "unoccupied");
            sprite.addAttribute("ui.label", id); // todo label is positioned weirdly atm, should be fixed
            addObserver(sprite);
        }

    }



    public void advance() {
        //todo

        setChanged();
        notifyObservers();
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
