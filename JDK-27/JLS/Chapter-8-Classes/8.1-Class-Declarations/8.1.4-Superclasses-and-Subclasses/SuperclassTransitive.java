/*
 * @test
 * @summary JLS 8.1.4: the superclass relationship is the transitive closure of
 *          the direct superclass relationship.
 * @run main SuperclassTransitive
 */

class Point { int x, y; }
class ColoredPoint extends Point { int color; }
class Colored3dPoint extends ColoredPoint { int z; }

public class SuperclassTransitive {
    public static void main(String[] args) {
        // Point is a superclass of ColoredPoint and of Colored3dPoint.
        if (!Point.class.isAssignableFrom(ColoredPoint.class))
            throw new AssertionError("Point superclass of ColoredPoint");
        if (!Point.class.isAssignableFrom(Colored3dPoint.class))
            throw new AssertionError("Point superclass of Colored3dPoint");
        if (!ColoredPoint.class.isAssignableFrom(Colored3dPoint.class))
            throw new AssertionError("ColoredPoint superclass of Colored3dPoint");
        // The default direct superclass of a class is Object.
        if (Point.class.getSuperclass() != Object.class)
            throw new AssertionError("default superclass is Object");
        System.out.println("PASS");
    }
}
