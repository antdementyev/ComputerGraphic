package edu.hawhamburg.app.vuforia;

import java.util.List;

import edu.hawhamburg.shared.datastructures.CollisionUtils;
import edu.hawhamburg.shared.datastructures.Particle;
import edu.hawhamburg.shared.datastructures.mesh.ITriangleMesh;
import edu.hawhamburg.shared.datastructures.mesh.ObjReader;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMesh;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMeshFactory;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMeshTools;
import edu.hawhamburg.shared.math.AxisAlignedBoundingBox;
import edu.hawhamburg.shared.math.Vector;
import edu.hawhamburg.shared.misc.Button;
import edu.hawhamburg.shared.misc.ButtonHandler;
import edu.hawhamburg.shared.misc.Scene;
import edu.hawhamburg.shared.scenegraph.BoundingBoxNode;
import edu.hawhamburg.shared.scenegraph.InnerNode;
import edu.hawhamburg.shared.scenegraph.TranslationNode;
import edu.hawhamburg.shared.scenegraph.TriangleMeshNode;
import edu.hawhamburg.vuforia.VuforiaMarkerNode;

public class CannonScene extends Scene {

    private final static Vector GRAVITY = new Vector(0, -0.01, 0);
    private final static Vector SHOT_VELOCITY = new Vector(0, 0.1, -0.2);
    private final static double CANNON_BALL_RADIUS = 0.1;

    private final static Vector COLOR_GREEN = new Vector(0.25, 0.75, 0.25, 1);
    private final static Vector COLOR_RED = new Vector(1, 0, 0, 1);

    private TranslationNode cannonBallTranslationNode;
    private Particle cannonBall;
    private BoundingBoxNode targetBoxNode;      // just to show collisions with ball

    @Override
    public void onSetup(InnerNode rootNode) {
        // place the cannon on its marker
        VuforiaMarkerNode cannonMarker = new VuforiaMarkerNode("elphi");
        rootNode.addChild(cannonMarker);
        List<ITriangleMesh> cannonMeshes = new ObjReader().read("meshes/cannon.obj");
        TriangleMeshTools.fitToUnitBox(cannonMeshes);
        TriangleMeshTools.placeOnXZPlane(cannonMeshes);
        for (ITriangleMesh cannonMesh : cannonMeshes) {
            TriangleMeshNode cannonMeshNode = new TriangleMeshNode(cannonMesh);
            cannonMarker.addChild(cannonMeshNode);
        }

        // set ball into the cannon coordinate system
        ITriangleMesh sphereMesh = new TriangleMesh();
        TriangleMeshFactory.createSphere(sphereMesh, CANNON_BALL_RADIUS, 10);
        TriangleMeshTools.placeOnXZPlane(sphereMesh);
        TriangleMeshNode sphereMeshNode = new TriangleMeshNode(sphereMesh);
        // ... with a translation to be able to move it
        cannonBallTranslationNode = new TranslationNode(new Vector(0, 0, 0));
        cannonBallTranslationNode.addChild(sphereMeshNode);
        cannonMarker.addChild(cannonBallTranslationNode);

        // place a target on its marker
        VuforiaMarkerNode targetMarker = new VuforiaMarkerNode("campus");
        rootNode.addChild(targetMarker);
        ITriangleMesh targetMesh = new ObjReader().read("meshes/chest.obj").get(0);
        TriangleMeshTools.fitToUnitBox(targetMesh);
        TriangleMeshTools.placeOnXZPlane(targetMesh);
        TriangleMeshNode targetMeshNode = new TriangleMeshNode(targetMesh);
        targetMarker.addChild(targetMeshNode);
        // ... with a bounding box to show collisions with the cannon ball
        AxisAlignedBoundingBox targetBox = targetMesh.getBoundingBox();
        targetBoxNode = new BoundingBoxNode(targetBox);
        targetMarker.addChild(targetBoxNode);

        // shoot button
        Button button = new Button("skeleton.png",
                -0.7, -0.7, 0.2, new ButtonHandler() {
            @Override
            public void handle() {
                shoot();
            }
        });
        addButton(button);
    }

    @Override
    public void onTimerTick(int counter) {
    }

    @Override
    public void onSceneRedraw() {
        if (cannonBall == null) {
            return;
        }
        // update the ball position
        cannonBallTranslationNode.setTranslation(cannonBall.getNextPosition());

        // check collision via bounding boxes
        boolean doBoxesCollide = CollisionUtils.doBoxesCollide(
                targetBoxNode.getBoundingBox(),
                targetBoxNode.getTransformation(),
                cannonBallTranslationNode.getBoundingBox(),
                cannonBallTranslationNode.getTransformation());
        targetBoxNode.setColor(doBoxesCollide ? COLOR_RED : COLOR_GREEN);
    }

    private void shoot() {
        // set ball position to the start position
        cannonBall = new Particle(new Vector(0, 0, 0), new Vector(SHOT_VELOCITY), new Vector(GRAVITY));
    }
}
