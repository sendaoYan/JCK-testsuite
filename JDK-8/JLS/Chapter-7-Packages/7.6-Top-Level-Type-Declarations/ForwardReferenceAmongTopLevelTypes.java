/*
 * @test
 * @summary JLS 7.6 (Java SE 8): top level types in a compilation unit share the
 *          whole package as scope, so they may reference each other regardless
 *          of declaration order (Example 7.6-2).
 * @run main points.ForwardReferenceAmongTopLevelTypes
 */
package points;

public class ForwardReferenceAmongTopLevelTypes {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Point p = new Point();
        p.color = new PointColor(1);
        check(p.nPoints == 0, "static field default");
        check(p.color != null, "forward reference to PointColor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class Point {
    int x, y;
    PointColor color;
    static int nPoints;
}

class PointColor {
    Point first;
    private int color;
    PointColor(int c) { this.color = c; }
}
