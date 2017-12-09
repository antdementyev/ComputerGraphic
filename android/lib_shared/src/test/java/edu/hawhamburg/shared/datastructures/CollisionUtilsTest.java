package edu.hawhamburg.shared.datastructures;

import org.junit.Test;

import edu.hawhamburg.shared.math.AxisAlignedBoundingBox;
import edu.hawhamburg.shared.math.Matrix;
import edu.hawhamburg.shared.math.Vector;

import static org.junit.Assert.*;

public class CollisionUtilsTest {

    private Matrix transformation1 = new Matrix( 0, 1, 0, 2,
                                                -1, 0, 0, 6,
                                                 0, 0, 1, 0,
                                                 0, 0, 0, 1);
    private Matrix transformation2 = new Matrix(-1, 0, 0, 9,
                                                 0,-1, 0, 4,
                                                 0, 0, 1, 0,
                                                 0, 0, 0, 1);
    private AxisAlignedBoundingBox box1 = new AxisAlignedBoundingBox(
                new Vector(1, 1, 0),
                new Vector(2, 2, 0));

    @Test
    public void doBoxesCollide_noOverlap() {
        AxisAlignedBoundingBox box2 = new AxisAlignedBoundingBox(
                new Vector(3, 0, 0),
                new Vector(4.999, 2, 0));
        assertFalse(CollisionUtils.doBoxesCollide(box1, transformation1, box2, transformation2));
    }

    @Test
    public void doBoxesCollide_boardsTouch() {
        AxisAlignedBoundingBox box2 = new AxisAlignedBoundingBox(
                new Vector(3, 0, 0),
                new Vector(5, 2, 0));
        assertTrue(CollisionUtils.doBoxesCollide(box1, transformation1, box2, transformation2));
    }

    @Test
    public void doBoxesCollide_overlap() {
        AxisAlignedBoundingBox box2 = new AxisAlignedBoundingBox(
                new Vector(5.999, -3, 0),
                new Vector(8, 4, 0));
        assertTrue(CollisionUtils.doBoxesCollide(box1, transformation1, box2, transformation2));
    }

    @Test
    public void doBoxesCollide_box2ContainsBox1() {
        AxisAlignedBoundingBox box2 = new AxisAlignedBoundingBox(
                new Vector(4, -2, 0),
                new Vector(7, 1, 0));
        assertTrue(CollisionUtils.doBoxesCollide(box1, transformation1, box2, transformation2));
    }
}
