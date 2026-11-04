package pt2;

import pt1.Point;

public class Point3d extends Point {
    protected int z;

    public void setAll(int x, int y, int z) {
        this.x = x;   // inherited protected member, accessed via this (a Point3d)
        this.y = y;
        this.z = z;
    }

    // Qualifying type of q is Point3d (a subclass of Point), so the inherited
    // protected members x, y and the declared protected member z are accessible.
    public int sum(Point3d q) {
        return q.x + q.y + q.z;
    }
}
