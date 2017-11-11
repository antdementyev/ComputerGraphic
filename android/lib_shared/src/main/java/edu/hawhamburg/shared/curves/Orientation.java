package edu.hawhamburg.shared.curves;

import edu.hawhamburg.shared.math.Vector;

public class Orientation {

    private Vector position;
    private Vector direction;

    public Orientation(Vector position, Vector direction) {
        this.position = position;
        this.direction = direction;
    }

    public Vector getPosition() {
        return position;
    }

    public Vector getDirection() {
        return direction;
    }
}
