/*
 * @test
 * @summary JLS 5.7 (Java SE 27): a switch expression whose selector has at least
 *          one pattern case label is a testing context. The selector undergoes
 *          identity, widening reference, or checked narrowing reference
 *          conversion as part of pattern matching, conditionally; a narrowing
 *          reference conversion performs a run-time check (no throw on a
 *          non-matching pattern).
 * @run main PatternSwitchTestingContext
 */

sealed interface Shape permits Circle, Square {}
final class Circle implements Shape { int radius = 1; }
final class Square implements Shape { int side = 2; }

public class PatternSwitchTestingContext {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // Checked narrowing reference conversion in a testing context: the
        // selector (Shape) is converted to Circle only if it matches.
        Shape shape = new Circle();
        int r = switch (shape) {
            case Circle c -> c.radius;
            case Square s -> s.side;
        };
        check(r == 1, "switch pattern testing context performs checked narrowing");

        // Identity conversion in a testing context (Circle -> Circle).
        Circle c = new Circle();
        boolean ident = c instanceof Circle c2 && c2.radius == 1;
        check(ident, "identity conversion in testing context");

        // Widening reference conversion in a testing context: the pattern type
        // is a supertype of the selector's type.
        String s = "x";
        boolean matched = s instanceof Object o && o != null;
        check(matched, "widening reference conversion in testing context");

        // A non-matching checked narrowing performs no ClassCastException.
        Shape sq = new Square();
        int r3 = switch (sq) {
            case Circle cc -> -1;
            case Square ss -> ss.side;
        };
        check(r3 == 2, "non-matching pattern does not throw");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
