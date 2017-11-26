package edu.hawhamburg.shared.misc;

import org.junit.Test;

import edu.hawhamburg.shared.datastructures.skeleton.Bone;
import edu.hawhamburg.shared.datastructures.skeleton.Skeleton;
import edu.hawhamburg.shared.math.Matrix;
import edu.hawhamburg.shared.math.Vector;

import static org.junit.Assert.assertEquals;

public class SkeletonSceneTest {

    @Test
    public void getDistanceBetween_ProjectionAfterEndPoint() {
        Vector start = new Vector(2, 2, 0);
        Vector end = new Vector(6, 4, 0);
        Bone bone = createBone(start, end);

        Vector point = new Vector(9, 3, 0);

        // nearest point is end point
        assertEquals(point.subtract(end).getNorm(),
                SkeletonScene.getDistanceBetween(point, bone),
                0.000001);
    }

    @Test
    public void getDistanceBetween_ProjectionBeforeStartPoint() {
        Vector start = new Vector(2, 2, 0);
        Vector end = new Vector(6, 4, 0);
        Bone bone = createBone(start, end);

        Vector point = new Vector(-1, 3, 0);

        // nearest point is start point
        assertEquals(point.subtract(start).getNorm(),
                SkeletonScene.getDistanceBetween(point, bone),
                0.000001);
    }

    @Test
    public void getDistanceBetween_ProjectionToEndPoint() {
        Vector start = new Vector(2, 2, 0);
        Vector end = new Vector(6, 4, 0);
        Bone bone = createBone(start, end);

        Vector point = new Vector(7, 2, 0);

        // nearest point is end point
        assertEquals(point.subtract(end).getNorm(),
                SkeletonScene.getDistanceBetween(point, bone),
                0.000001);
    }

    @Test
    public void getDistanceBetween_ProjectionToStartPoint() {
        Vector start = new Vector(2, 2, 0);
        Vector end = new Vector(6, 4, 0);
        Bone bone = createBone(start, end);

        Vector point = new Vector(0, 6, 0);

        // nearest point is start point
        assertEquals(point.subtract(start).getNorm(),
                SkeletonScene.getDistanceBetween(point, bone),
                0.000001);
    }

    @Test
    public void getDistanceBetween_ProjectionToBone() {
        Vector start = new Vector(2, 2, 0);
        Vector end = new Vector(6, 4, 0);
        Bone bone = createBone(start, end);

        Vector point = new Vector(5, 1, 0);

        // nearest point is middle of bone
        Vector middle = end.subtract(start)
                .multiply(0.5)
                .add(start);
        assertEquals(point.subtract(middle).getNorm(),
                SkeletonScene.getDistanceBetween(point, bone),
                0.000001);
    }

    private Bone createBone(Vector start, Vector end) {
        // set start
        Skeleton skeleton = new Skeleton();
        Bone subBone = new Bone(skeleton, start.getNorm());
        double angle = Math.atan(start.y()/start.x());
        subBone.setRotation(Matrix.createRotationMatrix4(new Vector(0, 0, 1), angle));

        // set end
        Vector vector = end.subtract(start);
        double length = vector.getNorm();
        Bone bone = new Bone(subBone, length);
        angle = Math.atan(vector.y()/vector.x()) - angle;
        bone.setRotation(Matrix.createRotationMatrix4(new Vector(0, 0, 1), angle));

        return bone;
    }
}
