/*
 * @test
 * @summary JLS 5.5: casting contexts allow more conversions than assignment or
 *          invocation contexts. Primitive casts (identity/widening/narrowing/
 *          widening+narrowing), reference casts (widening/narrowing), boxing
 *          and unboxing are all permitted. A checked or partially unchecked
 *          narrowing reference conversion performs a run-time check that may
 *          throw ClassCastException.
 * @run main CastingContexts
 */

class Point { int x, y; }
interface Colorable { void setColor(int color); }
class ColoredPoint extends Point implements Colorable {
    int color;
    public void setColor(int color) { this.color = color; }
}

public class CastingContexts {
    public static void main(String[] args) {
        // Widening and narrowing reference casts.
        Point p = new ColoredPoint();
        ColoredPoint cp = (ColoredPoint) p;     // narrowing reference cast
        Colorable c = (Colorable) p;            // narrowing reference cast to interface
        c.setColor(7);
        if (cp.color != 7) throw new AssertionError("reference cast");

        // Primitive casts: narrowing.
        int i = (int) 3.9;                       // double -> int (narrowing, toward zero)
        if (i != 3) throw new AssertionError("primitive narrowing cast");

        // Boxing and unboxing via casts.
        Object o = (Object) 5;                   // int -> Integer -> Object (boxing + widening)
        int j = (int) (Integer) o;               // unboxing
        if (j != 5) throw new AssertionError("boxing/unboxing cast");

        // Array cast (narrowing reference conversion, checked at run time).
        Point[] pa = new ColoredPoint[2];
        pa[0] = new ColoredPoint();
        ColoredPoint[] cpa = (ColoredPoint[]) pa;
        if (cpa.length != 2) throw new AssertionError("array cast");

        // A narrowing reference cast that fails throws ClassCastException.
        try {
            Object oi = new Point();
            ColoredPoint bad = (ColoredPoint) oi;
            throw new AssertionError("expected ClassCastException");
        } catch (ClassCastException expected) {
            // expected
        }

        System.out.println("PASS");
    }
}
