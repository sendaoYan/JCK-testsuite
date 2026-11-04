/*
 * @test
 * @summary JLS 6.6.2.1 Example 6.6.2-1 (Java SE 8): a protected instance member
 *          is accessible from a subclass in another package when the qualifying
 *          type is that subclass (or a subclass of it).
 * @compile Point.java Point3d.java Main.java
 * @run main pt2.Main
 */

package pt2;

public class Main {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        Point3d q = new Point3d();
        q.setAll(1, 2, 3);
        check(q.sum(q) == 6, "protected member via subclass-qualified name");
        check(q.z == 3, "own protected member via subclass reference");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
