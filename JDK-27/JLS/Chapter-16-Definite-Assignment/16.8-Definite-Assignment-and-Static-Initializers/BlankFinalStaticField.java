/*
 * @test
 * @summary JLS 16.8: a blank final static field must be definitely assigned by the
 *          end of a static initializer (or by its own initializer).
 * @run main BlankFinalStaticField
 */

public class BlankFinalStaticField {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final int V;

    static {
        V = 42;   // blank final static assigned exactly once
    }

    public static void main(String[] args) {
        check(V == 42, "blank final static field assigned in static initializer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
