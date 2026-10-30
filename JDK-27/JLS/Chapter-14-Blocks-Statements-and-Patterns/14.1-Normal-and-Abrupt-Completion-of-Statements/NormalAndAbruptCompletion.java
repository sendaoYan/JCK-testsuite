/*
 * @test
 * @summary JLS 14.1: abrupt completion of an expression or substatement causes
 *          the statement to complete abruptly for the same reason (the thrown
 *          value is preserved, not wrapped or replaced).
 * @run main NormalAndAbruptCompletion
 */

public class NormalAndAbruptCompletion {
    static int throwISE() { throw new IllegalStateException("boom"); }

    public static void main(String[] args) {
        // Normal completion: every step is carried out.
        int x = 1;
        x++;
        if (x != 2) throw new AssertionError("normal completion");

        // Abrupt completion of an expression propagates the same reason (value).
        try {
            int y = throwISE() + 1;
            throw new AssertionError("unreachable after abrupt expression");
        } catch (IllegalStateException e) {
            if (!"boom".equals(e.getMessage()))
                throw new AssertionError("reason not preserved: " + e.getMessage());
        }

        // Abrupt completion of a substatement propagates the same reason.
        try {
            {
                throw new ArithmeticException("sub");
                // unreachable
            }
        } catch (ArithmeticException e) {
            if (!"sub".equals(e.getMessage()))
                throw new AssertionError("substatement reason not preserved");
        }

        System.out.println("PASS");
    }
}
