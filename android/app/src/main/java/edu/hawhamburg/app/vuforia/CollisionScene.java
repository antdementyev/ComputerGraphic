package edu.hawhamburg.app.vuforia;

import java.util.Arrays;
import java.util.List;

import edu.hawhamburg.shared.datastructures.CollisionUtils;
import edu.hawhamburg.shared.datastructures.mesh.ITriangleMesh;
import edu.hawhamburg.shared.datastructures.mesh.ObjReader;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMesh;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMeshFactory;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMeshTools;
import edu.hawhamburg.shared.math.AxisAlignedBoundingBox;
import edu.hawhamburg.shared.math.Matrix;
import edu.hawhamburg.shared.math.Vector;
import edu.hawhamburg.shared.misc.Scene;
import edu.hawhamburg.shared.scenegraph.BoundingBoxNode;
import edu.hawhamburg.shared.scenegraph.InnerNode;
import edu.hawhamburg.shared.scenegraph.TranslationNode;
import edu.hawhamburg.shared.scenegraph.TriangleMeshNode;
import edu.hawhamburg.vuforia.VuforiaMarkerNode;

public class CollisionScene extends Scene {

    private final static Vector GREEN = new Vector(0.25, 0.75, 0.25, 1);
    private final static Vector YELLOW = new Vector(1, 1, 0, 1);
    private final static Vector RED = new Vector(1, 0, 0, 1);

    private BoundingBoxNode objectBoxNode;
    private ITriangleMesh objectMesh;

    private BoundingBoxNode sphereBoxNode;
    private double sphereRadius;

    @Override
    public void onSetup(InnerNode rootNode) {
        // bind two mesh objects with their markers
        // ... any mesh object
        objectMesh = new ObjReader().read("meshes/max_planck.obj")
                .get(0);
        TriangleMeshTools.fitToUnitBox(objectMesh);
        TriangleMeshTools.placeOnXZPlane(objectMesh);
        objectBoxNode = addObjectWithBoxToRoot(rootNode, objectMesh, "campus");

        // ... sphere
        ITriangleMesh sphereMesh = new TriangleMesh();
        sphereRadius = 0.3;
        TriangleMeshFactory.createSphere(sphereMesh, sphereRadius, 100);
        TriangleMeshTools.placeOnXZPlane(sphereMesh);
        sphereBoxNode = addObjectWithBoxToRoot(rootNode, sphereMesh, "elphi");
    }

    private BoundingBoxNode addObjectWithBoxToRoot(InnerNode rootNode, ITriangleMesh mesh, String markerName) {
        // node for object
        TriangleMeshNode meshNode = new TriangleMeshNode(mesh);
        // ... and its box
        List<Vector> boxCorners = getBoxCorners(mesh);
        AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(boxCorners.get(0), boxCorners.get(1));
        BoundingBoxNode boxNode = new BoundingBoxNode(box);

        // position in relation to marker
        TranslationNode translationNode = new TranslationNode(new Vector(0, 0, -1.3));    // move object some forward
        translationNode.addChild(meshNode);
        translationNode.addChild(boxNode);

        // marker
        VuforiaMarkerNode markerNode = new VuforiaMarkerNode(markerName);
        markerNode.addChild(translationNode);

        rootNode.addChild(markerNode);
        return boxNode;
    }

    private List<Vector> getBoxCorners(ITriangleMesh mesh) {
        Vector lowerLeftCorner = new Vector(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Vector upperRightCorner = new Vector(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            Vector vertexPosition = mesh.getVertex(i).getPosition();
            for (int dimension = 0; dimension < 3; dimension++) {
                double dimensionCoordinate = vertexPosition.get(dimension);
                lowerLeftCorner.set(dimension,
                        Math.min(lowerLeftCorner.get(dimension), dimensionCoordinate));
                upperRightCorner.set(dimension,
                        Math.max(upperRightCorner.get(dimension), dimensionCoordinate));
            }
        }

        return Arrays.asList(lowerLeftCorner, upperRightCorner);
    }

    @Override
    public void onTimerTick(int counter) {
        // Timer tick event
    }

    @Override
    public void onSceneRedraw() {
        Vector boxColor = getBoxColor();
        objectBoxNode.setColor(boxColor);
        sphereBoxNode.setColor(boxColor);
    }

    private Vector getBoxColor() {
        // check bounding box collision
        AxisAlignedBoundingBox objectBox = objectBoxNode.getBoundingBox();
        AxisAlignedBoundingBox sphereBox = sphereBoxNode.getBoundingBox();
        Matrix objectTransformation = objectBoxNode.getTransformation();
        Matrix sphereTransformation = sphereBoxNode.getTransformation();
        if (!CollisionUtils.doBoxesCollide(objectBox, objectTransformation, sphereBox, sphereTransformation)) {
            return GREEN;
        }

        // check mesh collision
        if (CollisionUtils.doesMeshCollideWithSphere(objectMesh, objectTransformation, sphereBox.getCenter(), sphereRadius, sphereTransformation)) {
            return RED;
        }

        return YELLOW;
    }
}
