package edu.hawhamburg.app.vuforia;

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
    private final static double SPHERE_RADIUS = 0.3;

    private BoundingBoxNode objectBoxNode;
    private ITriangleMesh objectMesh;

    private BoundingBoxNode sphereBoxNode;

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
        TriangleMeshFactory.createSphere(sphereMesh, SPHERE_RADIUS, 100);
        TriangleMeshTools.placeOnXZPlane(sphereMesh);
        sphereBoxNode = addObjectWithBoxToRoot(rootNode, sphereMesh, "elphi");
    }

    private BoundingBoxNode addObjectWithBoxToRoot(InnerNode rootNode, ITriangleMesh mesh, String markerName) {
        // object with box
        TriangleMeshNode meshNode = new TriangleMeshNode(mesh);
        AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(mesh.getBoundingBox());
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
        if (CollisionUtils.doesMeshCollideWithSphere(objectMesh, objectTransformation,
                sphereBox.getCenter(), SPHERE_RADIUS, sphereTransformation)) {
            return RED;
        }

        return YELLOW;
    }
}
