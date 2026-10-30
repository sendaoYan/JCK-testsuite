/*
 * @test
 * @summary JLS 8.2: the members of a class include members inherited from its
 *          direct superclass type; public and protected members are inherited
 *          by subclasses in other packages.
 * @run main InheritedMembersViaReflection
 */

class Point {
    public int x, y;
    protected int useCount;
    public void move(int dx, int dy) { x += dx; y += dy; useCount++; }
}

class Point3d extends Point {
    int z;
}

public class InheritedMembersViaReflection {
    public static void main(String[] args) throws Exception {
        // Inherited public field x and public method move appear via reflection.
        if (Point3d.class.getField("x") == null) throw new AssertionError("inherited field x");
        if (Point3d.class.getMethod("move", int.class, int.class) == null)
            throw new AssertionError("inherited method move");
        System.out.println("PASS");
    }
}
