package threePoint;

import points.Point;

public class Point3d extends Point {
    protected int z;

    public void setAll(int x, int y, int z) {
        this.x = x;   // inherited protected member, accessed via this (a Point3d)
        this.y = y;
        this.z = z;
    }

    // Qualifying type of q is Point3d (a subclass of Point), so the protected
    // members x, y (inherited) and z are all accessible.
    public int sum(Point3d q) {
        return q.x + q.y + q.z;
    }
}
