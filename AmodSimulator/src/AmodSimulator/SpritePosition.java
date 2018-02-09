package AmodSimulator;

import org.graphstream.graph.Element;

/**
 * Created by Jens on 09/02/2018.
 */
public class SpritePosition {
    private Element element;
    private double position;

    public SpritePosition(Element element, double position) {
        this.element = element;
        this.position = position;
    }

    public Element getElement() {
        return element;
    }

    public double getPosition() {
        return position;
    }
}
