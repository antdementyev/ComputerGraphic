package edu.hawhamburg.shared.curves;

import java.util.ArrayList;
import java.util.List;

import edu.hawhamburg.shared.math.Vector;

public class Spline {

    private final static int MIN_NUMBER_POINTS = 3;

    private List<HermiteCurve> curves;

    public Spline(List<Vector> points) {
        if (points.size() < MIN_NUMBER_POINTS) {
            throw new IllegalArgumentException("List of points must have at least " + MIN_NUMBER_POINTS + " control points");
        }
        curves = createCurves(points);
    }

    private List<HermiteCurve> createCurves(List<Vector> points) {
        // compute tangent for each control point
        List<Orientation> pointOrientations = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            Vector previousPoint = points.get(i > 0 ? i - 1 : points.size() - 1);
            Vector nextPoint = points.get(i < points.size() - 1 ? i + 1 : 0);
            Vector pointTangent = nextPoint.subtract(previousPoint)
                    .getNormalized();
            pointOrientations.add(new Orientation(points.get(i), pointTangent));
        }

        List<HermiteCurve> curves = new ArrayList<>();
        for (int i = 0; i < pointOrientations.size(); i++) {
            Orientation currentPoint = pointOrientations.get(i);
            Orientation nextPoint = pointOrientations.get(i < pointOrientations.size() - 1 ? i + 1 : 0);
            curves.add(new HermiteCurve(
                    currentPoint.getPosition(),
                    currentPoint.getDirection(),
                    nextPoint.getPosition(),
                    nextPoint.getDirection()));
        }

        return curves;
    }

    public Orientation getOrientation(double t) {
        if (t < 0 || t > 1) {
            throw new IllegalArgumentException("Parameter t must be in the range [0, 1]: " + t);
        }

        double tProCurveInWholeSpline = 1d / curves.size();
        int curveIndex = (int) (t / tProCurveInWholeSpline);
        double tCurve = (t - curveIndex * tProCurveInWholeSpline) / tProCurveInWholeSpline;

        return curves.get(curveIndex)
                .getOrientation(tCurve);
    }
}
