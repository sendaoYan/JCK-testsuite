/*
 * @test
 * @summary JLS 5.7: testing contexts apply to the operand of a pattern match
 *          operator (instanceof with a pattern) or the selector of a switch
 *          with a pattern label. Unlike casting contexts, they do not permit
 *          unchecked narrowing reference conversions, only checked ones.
 * @run main TestingContexts
 */

class Point {}
class ColoredPoint extends Point {}

public class TestingContexts {
    public static void main(String[] args) {
        // Pattern match instanceof: the operand is converted (widening or a
        // checked narrowing reference conversion) as part of matching.
        Object o = new ColoredPoint();
        if (o instanceof ColoredPoint cp) {
            if (cp == null) throw new AssertionError("pattern variable");
        } else {
            throw new AssertionError("should match ColoredPoint");
        }

        // A failed pattern match performs no throw (testing contexts are conditional).
        Object o2 = new Point();
        if (o2 instanceof ColoredPoint bad) {
            throw new AssertionError("must not match");
        }
        // Flow continues here; no ClassCastException was thrown.

        // Identity and widening reference conversions are also permitted.
        Point p = new ColoredPoint();
        if (p instanceof Point pt) {
            if (pt == null) throw new AssertionError("widening reference in testing context");
        }

        System.out.println("PASS");
    }
}
