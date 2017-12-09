package edu.hawhamburg.shared.datastructures;

import edu.hawhamburg.shared.datastructures.mesh.ITriangleMesh;
import edu.hawhamburg.shared.math.AxisAlignedBoundingBox;
import edu.hawhamburg.shared.math.Matrix;
import edu.hawhamburg.shared.math.Vector;

public class CollisionUtils {

    public static boolean doBoxesCollide(AxisAlignedBoundingBox box1, Matrix transformation1,
                                   AxisAlignedBoundingBox box2, Matrix transformation2) {
        // translate box2 to the box1 coordinate system
        Matrix transformationBox2ToBox1System = transformation1.getInverse()
                .multiply(transformation2);
        AxisAlignedBoundingBox box2InBox1System = new AxisAlignedBoundingBox(box2);
        box2InBox1System.transform(transformationBox2ToBox1System);

        // check if boxes overlap each other
        Vector upperRightCornerBox1 = box1.getUR();
        Vector upperRightCornerBox2 = box2InBox1System.getUR();
        Vector lowerLeftCornerBox1 = box1.getLL();
        Vector lowerLeftCornerBox2 = box2InBox1System.getLL();
        for (int dimension = 0; dimension < upperRightCornerBox1.getDimension(); dimension++) {
            boolean overlapsInDimension = upperRightCornerBox2.get(dimension) >= lowerLeftCornerBox1.get(dimension)
                    && upperRightCornerBox1.get(dimension) >= lowerLeftCornerBox2.get(dimension);
            if (!overlapsInDimension) {
                return false;
            }
        }

        return true;
    }

    public static boolean doesMeshCollideWithSphere(ITriangleMesh mesh, Matrix meshTransformation,
                                              Vector sphereCenterInSphereSystem, double radius, Matrix sphereTransformation) {
        Matrix transformationToSphereSystem = sphereTransformation.getInverse()
                .multiply(meshTransformation);
        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            Vector vertexPosition = mesh.getVertex(i)
                    .getPosition();
            Vector vertexPositionInSphereSystem = transformationToSphereSystem
                    .multiply(Vector.makeHomogenious(vertexPosition))
                    .xyz();
            double distanceFromCenter = vertexPositionInSphereSystem.subtract(sphereCenterInSphereSystem)
                    .getNorm();
            if (distanceFromCenter <= radius) {
                return true;
            }
        }

        return false;
    }
}
