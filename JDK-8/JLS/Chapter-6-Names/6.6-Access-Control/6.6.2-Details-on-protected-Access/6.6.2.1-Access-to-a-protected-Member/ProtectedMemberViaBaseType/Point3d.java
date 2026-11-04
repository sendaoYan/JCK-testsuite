/*
 * @test
 * @summary JLS 6.6.2.1 Example 6.6.2-1 (Java SE 8): a protected member cannot be
 *          accessed via a reference whose type is the base class (not the
 *          subclass), because the subclass is not involved in the
 *          implementation of the base class.
 * @compile Point.java
 * @compile/fail Point3d.java
 */
package pbt2;

import pbt1.Point;

public class Point3d extends Point {
    protected int z;
    public void sum(Point p) {
        p.x += 0;   // error: x has protected access in Point
        p.y += 0;   // error: y has protected access in Point
    }
}
