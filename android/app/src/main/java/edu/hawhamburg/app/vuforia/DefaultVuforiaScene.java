package edu.hawhamburg.app.vuforia;

import java.util.List;

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

    private TranslationNode targetTranslationNode;
    private VuforiaMarkerNode targetMarkerNode;

    private TranslationNode observerTranslationNode;
    private TransformationNode observerTransformationNode;
    private VuforiaMarkerNode observerMarkerNode;

    public DefaultVuforiaScene() {
        super(100, INode.RenderMode.REGULAR);
    }

    @Override
    public void onSetup(InnerNode rootNode) {
        setupObserver(rootNode);
        setupObservable(rootNode);
    }

    private void setupObserver(InnerNode rootNode) {
        observerTransformationNode = new TransformationNode();

        List<ITriangleMesh> meshes = new ObjReader().read("meshes/max_planck.obj");
        TriangleMeshTools.fitToUnitBox(meshes);
        TriangleMeshTools.placeOnXZPlane(meshes);
        for (ITriangleMesh mesh : meshes) {
            observerTransformationNode.addChild(new TriangleMeshNode(mesh));
        }

        observerTranslationNode = new TranslationNode(new Vector(3));
        observerTranslationNode.addChild(observerTransformationNode);

        observerMarkerNode = new VuforiaMarkerNode("campus");
        observerMarkerNode.addChild(observerTranslationNode);

        rootNode.addChild(observerMarkerNode);
    }

    private void setupObservable(InnerNode rootNode) {
        targetTranslationNode = new TranslationNode(new Vector(3));

        List<ITriangleMesh> meshes = new ObjReader().read("meshes/deer.obj");
        TriangleMeshTools.fitToUnitBox(meshes);
        TriangleMeshTools.placeOnXZPlane(meshes);
        for (ITriangleMesh mesh : meshes) {
            targetTranslationNode.addChild(new TriangleMeshNode(mesh));
        }

        targetMarkerNode = new VuforiaMarkerNode("elphi");
        targetMarkerNode.addChild(targetTranslationNode);

        rootNode.addChild(targetMarkerNode);
    }

    @Override
    public void onTimerTick(int counter) {
        // Timer tick event
    }

    @Override
    public void onSceneRedraw() {
        Matrix inverseObserverTransformation = observerMarkerNode.getTransformation()
                .getInverse();

        // start point of view direction
        Matrix observerTranslation = inverseObserverTransformation
                .multiply(observerTranslationNode.getTransformation());
        Vector observerPositionInObserverSystem = new Vector(
                observerTranslation.get(0, 3),
                observerTranslation.get(1, 3),
                observerTranslation.get(2, 3),
                1);

        // end point of view direction
        Matrix targetTranslation = targetMarkerNode.getTransformation()
                .getInverse()
                .multiply(targetTranslationNode.getTransformation());
        Vector targetPositionInTargetSystem = new Vector(
                targetTranslation.get(0, 3),
                targetTranslation.get(1, 3),
                targetTranslation.get(2, 3),
                1);
        Vector targetPositionInObserverSystem = inverseObserverTransformation
                .multiply(targetMarkerNode.getTransformation())
                .multiply(targetPositionInTargetSystem);

        // new axles of observer system
        Vector directionFromObserverToTarget = targetPositionInObserverSystem
                .subtract(observerPositionInObserverSystem)
                .xyz()              // remove the homogenious coordinate
                .getNormalized();
        Vector newZAxleObserver = directionFromObserverToTarget.cross(Vector.VECTOR_3_Y)
                .getNormalized();
        Vector newYAxleObserver = newZAxleObserver.cross(directionFromObserverToTarget)
                .getNormalized();

        // update observer transformation matrix
        Matrix newObserverTransformation = new Matrix(
                directionFromObserverToTarget,
                newYAxleObserver,
                newZAxleObserver);
        newObserverTransformation = Matrix.makeHomogenious(newObserverTransformation);
        observerTransformationNode.setTransformation(newObserverTransformation);
    }
}

