package edu.hawhamburg.shared.misc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.hawhamburg.shared.datastructures.mesh.ITriangleMesh;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMesh;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMeshFactory;
import edu.hawhamburg.shared.datastructures.mesh.Vertex;
import edu.hawhamburg.shared.datastructures.skeleton.Bone;
import edu.hawhamburg.shared.datastructures.skeleton.Skeleton;
import edu.hawhamburg.shared.datastructures.skeleton.SkeletonNode;
import edu.hawhamburg.shared.math.Matrix;
import edu.hawhamburg.shared.math.Vector;
import edu.hawhamburg.shared.scenegraph.InnerNode;
import edu.hawhamburg.shared.scenegraph.TriangleMeshNode;

/**
 * Test scene for sceleton applications.
 *
 * @author Philipp Jenke
 */

public class SkeletonScene extends Scene {

    private final static boolean IS_TRANSFORMATION_WITH_NEAREST_BONE = false;
    private double animationAlpha = 0;

    private Bone trunkBone;
    private Bone cylinderBottom;
    private Bone cylinderMiddle;
    private Bone cylinderTop;
    private List<Bone> allBones;

    private TriangleMesh mesh;
    private TriangleMesh meshAtRestState;
    private TriangleMeshNode meshNode;

    private ShowBones showBones = ShowBones.BONES_ONLY;
    private SkeletonNode skeletonNode;

    // structures with constant values to reduce computations at each frame
    /**
     * Map with the nearest bone for each vertex of mesh
     */
    private Map<Vertex, Bone> vertexToNearestBoneMap;
    /**
     * Map with translations to get a vertex position at a bone coordinate system.
     */
    private Map<Bone, Map<Vertex, Vector>> boneToVertexToTranslationMap;
    /**
     * Map with weights of each bone for each vertex
     */
    private Map<Vertex, Map<Bone, Double>> vertexToBoneToWeightMap;

    private enum ShowBones {
        BONES_ONLY, MESH_AND_BONES, MESH_ONLY;

        public ShowBones next() {
            int index = ordinal() + 1;
            if (index >= values().length) {
                index = 0;
            }
            return values()[index];
        }
    }

    public SkeletonScene() {
    }

    @Override
    public void onSetup(InnerNode rootNode) {

        Button button = new Button("skeleton.png",
                -0.7, -0.7, 0.2, new ButtonHandler() {
            @Override
            public void handle() {
                showBones = showBones.next();
                updateRenderSettings();
            }
        });
        addButton(button);

        // Skeleton
        Skeleton skeleton = new Skeleton();
        trunkBone = new Bone(skeleton, 0.2);
        trunkBone.setRotation(Matrix.createRotationMatrix4(new Vector(0, 0, 1), 90 * Math.PI / 180.0));
        cylinderBottom = new Bone(trunkBone, 0.26);
        cylinderMiddle = new Bone(cylinderBottom, 0.26);
        cylinderTop = new Bone(cylinderMiddle, 0.26);
        // Preserve the current state of all bones as the rest state
        skeleton.setRestState();
        skeletonNode = new SkeletonNode(skeleton);
        getRoot().addChild(skeletonNode);

        // Plane
        ITriangleMesh planeMesh = new TriangleMesh();
        TriangleMeshFactory.createPlane(planeMesh, new Vector(0, 0, 0), new Vector(0, 1, 0), 1);
        rootNode.addChild(new TriangleMeshNode(planeMesh));

        // Pine-Tree
        //ObjReader reader = new ObjReader();
        //List<ITriangleMesh> meshes = reader.read("meshes/pinetree.obj");
        //TriangleMesh mesh = (TriangleMesh) TriangleMeshTools.unite(meshes);

        // Cylinder
        mesh = new TriangleMesh();
        TriangleMeshFactory.createCylinder(mesh, 0.025, 1, 8, 20);

        meshNode = new TriangleMeshNode(mesh);
        rootNode.addChild(meshNode);

        updateRenderSettings();
        getRoot().setLightPosition(new Vector(1, 2, 1));

        // help structures with constant values for each frame
        allBones = Arrays.asList(trunkBone, cylinderTop, cylinderMiddle, cylinderBottom);
        meshAtRestState = new TriangleMesh(mesh);
        vertexToNearestBoneMap = defineNearestBones();
        boneToVertexToTranslationMap = defineTranslationsToBoneCoordinateSystems();
        vertexToBoneToWeightMap = defineWeights();
    }

    /**
     * Defines for each vertex of the mesh its nearest bone.
     */
    private Map<Vertex, Bone> defineNearestBones() {
        Map<Vertex, Bone> vertexToNearestBoneMap = new HashMap<>();
        for (int i = 0; i < meshAtRestState.getNumberOfVertices(); i++) {
            Vertex vertex = meshAtRestState.getVertex(i);
            Bone nearestBone = getNearestBone(vertex.getPosition(), allBones);
            vertexToNearestBoneMap.put(vertex, nearestBone);
        }
        return vertexToNearestBoneMap;
    }

    private Map<Bone, Map<Vertex, Vector>> defineTranslationsToBoneCoordinateSystems() {
        Map<Bone, Map<Vertex, Vector>> boneToVertexToTranslationMap = new HashMap<>();
        for (Bone bone : allBones) {
            HashMap<Vertex, Vector> vertexToTranslationMap = new HashMap<>();
            boneToVertexToTranslationMap.put(bone, vertexToTranslationMap);

            for (int i = 0; i < meshAtRestState.getNumberOfVertices(); i++) {
                Vertex vertex = meshAtRestState.getVertex(i);
                // translation to get a vertex position at the coordinate system of the current bone
                Matrix inverseTransformation = bone.getRestStateTransformationAtEnd()
                        .getInverse();
                Vector homogeniousPosition = Vector.makeHomogenious(vertex.getPosition());
                Vector translation = inverseTransformation.multiply(homogeniousPosition);

                vertexToTranslationMap.put(vertex, translation);
            }
        }
        return boneToVertexToTranslationMap;
    }

    private Map<Vertex, Map<Bone, Double>> defineWeights() {
        double deviation = 0.1;

        Map<Vertex, Map<Bone, Double>> vertexToBoneToWeightMap = new HashMap<>();
        for (int i = 0; i < meshAtRestState.getNumberOfVertices(); i++) {
            Vertex vertex = meshAtRestState.getVertex(i);
            Map<Bone, Double> boneToWidthMap = new HashMap<>();
            vertexToBoneToWeightMap.put(vertex, boneToWidthMap);

            for (Bone bone : allBones) {
                double distance = getDistanceBetween(vertex.getPosition(), bone);
                double weight = 1 / Math.sqrt(2 * Math.PI * deviation * deviation)
                        * Math.exp(- distance * distance / (2 * deviation * deviation));
                boneToWidthMap.put(bone, weight);
            }
        }
        return vertexToBoneToWeightMap;
    }

    private void updateRenderSettings() {
        switch (showBones) {
            case MESH_ONLY:
                skeletonNode.setActive(false);
                meshNode.setTransparency(1.0);
                meshNode.setActive(true);
                break;
            case MESH_AND_BONES:
                skeletonNode.setActive(true);
                meshNode.setTransparency(0.5);
                meshNode.setActive(true);
                break;
            case BONES_ONLY:
                skeletonNode.setActive(true);
                meshNode.setTransparency(0.5);
                meshNode.setActive(false);
                break;
        }
    }

    @Override
    public void onTimerTick(int counter) {
    }

    @Override
    public void onSceneRedraw() {
        // Animate skeleton
        double DELTA_ANIMATION_ALPHA = 0.03;
        animationAlpha += DELTA_ANIMATION_ALPHA;
        double angle = Math.cos(animationAlpha) * Math.PI / 180.0 * 8;
        trunkBone.setRotation(Matrix.createRotationMatrix4(new Vector(0, 0, 1), 90 * Math.PI / 180.0));
        cylinderTop.setRotation(Matrix.createRotationMatrix4(new Vector(0, 1, 0), angle));
        cylinderMiddle.setRotation(Matrix.createRotationMatrix4(new Vector(0, 1, 0), 2 * angle));
        cylinderBottom.setRotation(Matrix.createRotationMatrix4(new Vector(0, 1, 0), 3 * angle));

        // Update mesh based on skeleton
        for (int i = 0; i < meshAtRestState.getNumberOfVertices(); i++) {
            Vertex vertex = meshAtRestState.getVertex(i);
            Vector newPosition = IS_TRANSFORMATION_WITH_NEAREST_BONE
                    ? getVertexPositionNearest(vertex)
                    : getVertexPositionWeighted(vertex);
            mesh.getVertex(i)
                    .getPosition()
                    .copy(newPosition);
        }

        mesh.computeTriangleNormals();
        meshNode.updateVbo();
    }

    private Vector getVertexPositionNearest(Vertex vertex) {
        Bone nearestBone = vertexToNearestBoneMap.get(vertex);
        return getVertexPosition(vertex, nearestBone);
    }

    private Vector getVertexPositionWeighted(Vertex vertex) {
        double weightSum = 0;
        Vector vertexPositionSum = new Vector(3);

        Map<Bone, Double> boneToWeightMap = vertexToBoneToWeightMap.get(vertex);
        for (Bone bone : allBones) {
            Vector vertexPositionFromBone = getVertexPosition(vertex, bone);
            double weight = boneToWeightMap.get(bone);

            weightSum += weight;
            vertexPositionSum = vertexPositionSum.add(
                    vertexPositionFromBone.multiply(weight));
        }

        return vertexPositionSum.multiply(1/weightSum);
    }

    private Vector getVertexPosition(Vertex vertex, Bone bone) {
        Vector translationToBoneSystem = boneToVertexToTranslationMap
                .get(bone)
                .get(vertex);
        return bone.getTransformationAtEnd()
                .multiply(translationToBoneSystem)
                .xyz();
    }

    private Bone getNearestBone(Vector point, List<Bone> bones) {
        Bone nearestBone = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Bone bone : bones) {
            double distanceToBone = getDistanceBetween(point, bone);
            if (distanceToBone < nearestDistance) {
                nearestBone = bone;
                nearestDistance = distanceToBone;
            }
        }
        return nearestBone;
    }

    // package visible and static just to test easier
    static double getDistanceBetween(Vector point, Bone bone) {
        // define the length between the bone start and projection point
        Vector boneDirection = bone.getEnd()
                .subtract(bone.getStart())
                .getNormalized();
        double lengthFromBoneStartToPointProjection = point.subtract(bone.getStart())
                .multiply(boneDirection);

        // define the nearest point on the bone line to the given point
        Vector nearestPoint;
        if (lengthFromBoneStartToPointProjection < 0) {
            nearestPoint = bone.getStart();
        } else if (lengthFromBoneStartToPointProjection > bone.getLength()) {
            nearestPoint = bone.getEnd();
        } else {
            nearestPoint = boneDirection.multiply(lengthFromBoneStartToPointProjection)
                    .add(bone.getStart());
        }

        // distance from point to bone
        return point.subtract(nearestPoint)
                .getNorm();
    }
}
