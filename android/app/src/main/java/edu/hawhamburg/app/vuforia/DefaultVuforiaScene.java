package edu.hawhamburg.app.vuforia;

import java.util.Arrays;
import java.util.List;

import edu.hawhamburg.shared.curves.Orientation;
import edu.hawhamburg.shared.curves.Spline;
import edu.hawhamburg.shared.datastructures.mesh.ITriangleMesh;
import edu.hawhamburg.shared.datastructures.mesh.ObjReader;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMeshTools;
import edu.hawhamburg.shared.math.Matrix;
import edu.hawhamburg.shared.math.Vector;
import edu.hawhamburg.shared.misc.Scene;
import edu.hawhamburg.shared.scenegraph.INode;
import edu.hawhamburg.shared.scenegraph.InnerNode;
import edu.hawhamburg.shared.scenegraph.TransformationNode;
import edu.hawhamburg.shared.scenegraph.TranslationNode;
import edu.hawhamburg.shared.scenegraph.TriangleMeshNode;
import edu.hawhamburg.vuforia.VuforiaMarkerNode;

/**
 * Dummy implementation of a scene with a Vuforia marker
 *
 * @author Philipp Jenke
 */
public class DefaultVuforiaScene extends Scene {

    private final static int MILLI_SECONDS_PRO_SPLINE = 10000;

    private TranslationNode translationNode;
    private TransformationNode transformationNode;
    private Spline spline;

    public DefaultVuforiaScene() {
        super(100, INode.RenderMode.REGULAR);
    }

    @Override
    public void onSetup(InnerNode rootNode) {
        // create scene graph
        // ... object to draw
        List<ITriangleMesh> objectMeshes = new ObjReader().read("meshes/plane.obj");
        TriangleMeshTools.fitToUnitBox(objectMeshes);
        TriangleMeshTools.placeOnXZPlane(objectMeshes);

        // ... orientation
        transformationNode = new TransformationNode();
        for (ITriangleMesh mesh : objectMeshes) {
            transformationNode.addChild(new TriangleMeshNode(mesh));
        }

        // ... position
        translationNode = new TranslationNode(new Vector(3));
        translationNode.addChild(transformationNode);

        // ... action place
        VuforiaMarkerNode markerNode = new VuforiaMarkerNode("elphi");
        markerNode.addChild(translationNode);

        rootNode.addChild(markerNode);

        // any trajectory
        List<Vector> controlPoints = Arrays.asList(
                new Vector(-1, -1, 0),
                new Vector(0, -1.5, 0.5),
                new Vector(1, -1, 0.5),
                new Vector(1, 0, 1),
                new Vector(1.5, 1, 1),
                new Vector(3, 1, 0.75),
                new Vector(3, -1, 0.5),
                new Vector(0, 0, 0),
                new Vector(-1, 1, -0.25),
                new Vector(-2, 1, -0.25),
                new Vector(-2, 0, 0));
        spline = new Spline(controlPoints);
    }

    @Override
    public void onTimerTick(int counter) {
        // Timer tick event
    }

    @Override
    public void onSceneRedraw() {
        // current position in spline
        double tSpline = (System.currentTimeMillis() % MILLI_SECONDS_PRO_SPLINE)
                / (double) MILLI_SECONDS_PRO_SPLINE;
        Orientation currentOrientation = spline.getOrientation(tSpline);

        // update object position
        translationNode.setTranslation(currentOrientation.getPosition());

        // update object direction
        Vector objectAxleX = currentOrientation.getDirection()
                .multiply(-1);
        Vector objectAxleZ = objectAxleX.cross(Vector.VECTOR_3_Y)
                .getNormalized();
        Vector objectAxleY = objectAxleZ.cross(objectAxleX)
                .getNormalized();
        Matrix transformation = new Matrix(
                objectAxleX,
                objectAxleY,
                objectAxleZ);
        transformation = Matrix.makeHomogenious(transformation);
        transformationNode.setTransformation(transformation);
    }
}

