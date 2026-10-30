/*
 * @test
 * @summary JLS 8.4.9: overloading lets methods share a name and differ only in
 *          signature; the most specific applicable method is chosen at compile
 *          time.
 * @run main Overloading
 */

class Point {
    float x, y;
    void move(int dx, int dy) { x += dx; y += dy; }
    void move(float dx, float dy) { x += dx; y += dy; }
    void move(double dx, double dy) { x += dx; y += dy; }
}

class Point3D extends Point {
    float z;
    void move(int dx, int dy, int dz) { x += dx; y += dy; z += dz; }
    void move(float dx, float dy, float dz) { x += dx; y += dy; z += dz; }
    void move(double dx, double dy, double dz) { x += dx; y += dy; z += dz; }
}

public class Overloading {
    public static void main(String[] args) {
        Point3D p = new Point3D();
        p.move(1, 2, 3);              // move(int,int,int)
        if (p.x != 1 || p.y != 2 || p.z != 3) throw new AssertionError("int overload");
        p.move(1.5f, 2.5f, 3.5f);     // move(float,float,float)
        if (p.x != 2.5f || p.y != 4.5f || p.z != 6.5f) throw new AssertionError("float overload");
        p.move(1.5, 2.5, 3.5);        // move(double,double,double)
        if (p.x != 4.0 || p.y != 7.0 || p.z != 10.0) throw new AssertionError("double overload");
        System.out.println("PASS");
    }
}
