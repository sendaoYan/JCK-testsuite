/*
 * @test
 * @summary JLS 6.6.2.1 Example 6.6.2-1: a protected member cannot be accessed via
 *          a reference whose type is the base class (not the subclass), because
 *          the subclass is not involved in the implementation of a Point.
 * @compile Point.java
 * @compile/fail Point3d.java
 */
package threePoint;

import points.Point;

public class Point3d extends Point {
    protected int z;
    public void sum(Point p) {
        p.x += 0;   // error: cannot access protected x via a Point reference
        p.y += 0;   // error: cannot access protected y via a Point reference
    }
}
