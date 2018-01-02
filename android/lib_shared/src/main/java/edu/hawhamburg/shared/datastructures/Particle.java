package edu.hawhamburg.shared.datastructures;

import edu.hawhamburg.shared.math.Vector;

public class Particle {

    private Vector position;
    private Vector velocity;
    private final Vector acceleration;
    // private final double weight;

    public Particle(Vector startPosition, Vector startVelocity, Vector acceleration) {
        this.position = startPosition;
        this.velocity = startVelocity;
        this.acceleration = acceleration;
    }

    public Vector getNextPosition() {
        position.addSelf(velocity);
        velocity.addSelf(acceleration);
        return position;
    }
}
