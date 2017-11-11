package edu.hawhamburg.shared.curves;

import edu.hawhamburg.shared.math.Vector;

public class HermiteCurve {

    private Vector startPoint;
    private Vector startTangent;
    private Vector endPoint;
    private Vector endTangent;

    public HermiteCurve(Vector startPoint, Vector startTangent, Vector endPoint, Vector endTangent) {
        this.startPoint = startPoint;
        this.startTangent = startTangent.getNormalized();
        this.endPoint = endPoint;
        this.endTangent = endTangent.getNormalized();
    }

    public Orientation getOrientation(double t) {
        if (t < 0 || t > 1) {
            throw new IllegalArgumentException("Parameter t must be in the range [0, 1]: " + t);
        }
        return new Orientation(getPosition(t), getDirection(t));
    }

    private Vector getPosition(double t) {
        double h0 = 2*t*t*t - 3*t*t + 1;
        double h1 = t*t*t - 2*t*t + t;
        double h2 = t*t*t - t*t;
        double h3 = -2*t*t*t + 3*t*t;
        return startPoint.multiply(h0)
                .add(startTangent.multiply(h1))
                .add(endTangent.multiply(h2))
                .add(endPoint.multiply(h3));
    }

    private Vector getDirection(double t) {
//        double deltaT = 0.01;
//        double nextT = t + deltaT <= 1 ? t + deltaT : t;
//        double previousT = t - deltaT >= 0 ? t - deltaT : t;
//
//        return getPosition(nextT)
//                .subtract(getPosition(previousT))
//                .getNormalized();

        double derivativeH0 = 6*t*t - 6*t;
        double derivativeH1 = 3*t*t - 4*t + 1;
        double derivativeH2 = 3*t*t - 2*t;
        double derivativeH3 = -6*t*t + 6*t;
        return startPoint.multiply(derivativeH0)
                .add(startTangent.multiply(derivativeH1))
                .add(endTangent.multiply(derivativeH2))
                .add(endPoint.multiply(derivativeH3))
                .getNormalized();
    }
}
