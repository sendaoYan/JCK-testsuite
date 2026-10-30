/*
 * @test
 * @summary JLS 14.30.2: pattern matching initializes pattern variables only on
 *          success. The null reference matches a null-matching type pattern but
 *          not a record pattern; a record component accessor that throws causes
 *          a MatchException whose cause is that exception.
 * @run main PatternMatching
 */

record NullBox(Object value) {}
record ThrowingBox(int x) {
    @Override public int x() { throw new IllegalStateException("accessor"); }
}

public class PatternMatching {
    public static void main(String[] args) {
        // null matches a null-matching (nested var) type pattern and is initialized to null.
        Object nb = new NullBox(null);
        boolean matched = false;
        if (nb instanceof NullBox(var v)) {
            matched = true;
            if (v != null) throw new AssertionError("null-matching should yield null, got " + v);
        }
        if (!matched) throw new AssertionError("null-matching record pattern did not match");

        // null does not match a record pattern.
        Object n = null;
        if (n instanceof NullBox(var v))
            throw new AssertionError("null should not match a record pattern");

        // A throwing accessor causes a MatchException with the exception as cause.
        Object tb = new ThrowingBox(1);
        try {
            boolean m = tb instanceof ThrowingBox(int x);
            throw new AssertionError("should have thrown MatchException, got " + m);
        } catch (MatchException e) {
            if (!(e.getCause() instanceof IllegalStateException))
                throw new AssertionError("MatchException cause: " + e.getCause());
        }

        System.out.println("PASS");
    }
}
