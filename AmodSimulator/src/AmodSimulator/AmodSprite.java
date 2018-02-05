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
            setAttribute("ui.class", "occupied");
            return;
        }

        Vehicle veh = (Vehicle) observable;

        switch ((VehicleEvent) o) {
            //case TRIP_STARTED:
            //case PICKED_UP:
            case ADVANCE_NEW_EDGE:
                attachToEdge(veh.getCurrentEdge().getId());
            case ADVANCE_SAME_EDGE:
                setPosition(calcPositionPercent(veh));
//                System.out.println("attached to: " + this.getAttachment().getId() + " and position is: " + calcPositionPercent(veh));
                break;
            case TRIP_COMPLETED: //todo NB: Should only be used when the vehicle has no more trips
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
