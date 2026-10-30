/*
 * @test
 * @summary JVMS 3.12: exception handlers are matched by exact class or subclass,
 *          in table order; an unmatched exception propagates to the invoker.
 * @run main CompilingExceptions
 */

public class CompilingExceptions {
    public static void main(String[] args) {
        // exact type match
        try {
            throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) { /* matched */ }

        // subclass match: NumberFormatException is an IllegalArgumentException
        try {
            throw new NumberFormatException();
        } catch (IllegalArgumentException e) { /* matched by subclass */ }

        // unmatched handler: propagates to an outer catch
        boolean propagated = false;
        try {
            try {
                throw new RuntimeException();
            } catch (IllegalArgumentException e) {
                throw new AssertionError("should not match");
            }
        } catch (RuntimeException e) {
            propagated = true;
        }
        if (!propagated) throw new AssertionError("exception did not propagate");

        System.out.println("PASS");
    }
}
