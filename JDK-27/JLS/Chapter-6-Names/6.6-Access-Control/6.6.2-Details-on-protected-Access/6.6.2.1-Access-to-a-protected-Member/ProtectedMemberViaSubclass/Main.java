/*
 * @test
 * @summary JLS 6.6.2.1 Example 6.6.2-1: a protected instance member is
 *          accessible from a subclass in another package when the qualifying
 *          type is that subclass (or a subclass of it).
 * @compile Point.java Point3d.java Main.java
 * @run main threePoint.Main
 */
package threePoint;

public class Main {
    public static void main(String[] args) {
        Point3d q = new Point3d();
        q.setAll(1, 2, 3);
        if (q.sum(q) != 6) throw new AssertionError("protected member via subclass");

        System.out.println("PASS");
    }
}
