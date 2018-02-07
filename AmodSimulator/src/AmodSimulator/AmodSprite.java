package AmodSimulator;

import org.graphstream.ui.spriteManager.Sprite;

import java.util.Observable;
import java.util.Observer;

public class AmodSprite extends Sprite implements Observer{

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Observable observable, Object o) {

        if (o instanceof VehicleStatus) {
            switch ((VehicleStatus) o) {
                case IDLE: setAttribute("ui.class", "idle");
                break;
                case MOVING_TOWARDS_REQUEST: setAttribute("ui.class", "moving");
                break;
                case OCCUPIED: setAttribute("ui.class", "occupied");
//                setAttribute("ui.label", "1"); // fixme: doesn't work, label flies around crazily, completely ignoring what we (think) we told it to do..
                break;
            }
            return;
        }

        Vehicle veh = (Vehicle) observable;

        switch ((VehicleEvent) o) {
            case ADVANCE_NEW_EDGE:
                attachToEdge(veh.getCurrentEdge().getId());
            case ADVANCE_SAME_EDGE:
                setPosition(calcPositionPercent(veh));
                break;
            case TRIP_COMPLETED:
                attachToNode(veh.getLastNode().getId());
                setPosition(0.0);
                break;
        }

    }


    private double calcPositionPercent(Vehicle veh) {
        // calculating how far an edge has been traversed
        double positionPercent = (double) veh.getCurrentEdgeDist() / (double) veh.getCurrentEdge().getAttribute("layout.weight");

        // if the last node is the same as source on the edge, then calculate percentage traversed in the normal way
        // otherwise we have to reverse the percentage
        if ( veh.getLastNode() != veh.getCurrentEdge().getSourceNode()) {
            positionPercent = (1.0 - positionPercent);
        }
        return positionPercent;
    }
}
