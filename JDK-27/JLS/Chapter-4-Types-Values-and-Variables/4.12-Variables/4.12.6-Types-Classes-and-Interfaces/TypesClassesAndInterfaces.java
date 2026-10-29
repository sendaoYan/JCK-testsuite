/*
 * @test
 * @summary JLS 4.12.6: every variable and expression has a compile-time type;
 *          every object has a class (its run-time type). A variable of an
 *          interface type has no instances but can reference any object whose
 *          class implements the interface. Compile-time type limits the
 *          possible run-time values.
 * @run main TypesClassesAndInterfaces
 */

interface Colorable {
    void setColor(byte r, byte g, byte b);
}

class Point {
    int x, y;
}

class ColoredPoint extends Point implements Colorable {
    byte r, g, b;
    public void setColor(byte rv, byte gv, byte bv) {
        r = rv; g = gv; b = bv;
    }
}

public class TypesClassesAndInterfaces {
    public static void main(String[] args) {
        Point p = new Point();
        ColoredPoint cp = new ColoredPoint();

        // The compile-time type limits what a variable may reference.
        p = cp;                       // ColoredPoint <: Point
        if (!(p instanceof ColoredPoint)) throw new AssertionError("run-time type is ColoredPoint");

        // A variable of interface type references an object whose class implements it.
        Colorable c = cp;
        c.setColor((byte) 1, (byte) 2, (byte) 3);
        if (cp.r != 1 || cp.g != 2 || cp.b != 3) {
            throw new AssertionError("interface variable invokes implementation");
        }

        // There are no instances of an interface.
        if (Colorable.class.isInterface() == false) throw new AssertionError("Colorable is interface");
        if (Colorable.class.isInterface() && false) {
            // Interfaces cannot be instantiated directly (compile-time error).
        }

        // The class of an object is returned by getClass (run-time type).
        Object o = cp;
        if (o.getClass() != ColoredPoint.class) {
            throw new AssertionError("class of object is its run-time class");
        }

        // Compile-time type of an expression is deduced; run-time type is the class.
        if (!(o.getClass() == ColoredPoint.class)) throw new AssertionError("run-time class");

        System.out.println("PASS");
    }
}
