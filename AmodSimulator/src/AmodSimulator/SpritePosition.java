package AmodSimulator;

import org.graphstream.graph.Element;

/**
 * Created by Jens on 09/02/2018.
 */
public class SpritePosition {
    private Element element;
    private double position;
    private String status;

    public SpritePosition(Element element, double position, String status) {
        this.element = element;
        this.position = position;
        this.status = status;
    }

    public Element getElement() {
        return element;
    }

    public double getPosition() {
        return position;
    }

    public String getStatus() {
        return status;
    }
}
