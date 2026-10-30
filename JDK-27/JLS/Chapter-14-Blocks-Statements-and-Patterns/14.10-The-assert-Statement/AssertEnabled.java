/*
 * @test
 * @summary JLS 14.10: with assertions enabled, an assert statement whose
 *          expression is false throws AssertionError, using Expression2 (if
 *          present) as the detail message.
 * @run main/othervm -ea AssertEnabled
 */

public class AssertEnabled {
    public static void main(String[] args) {
        assert true;            // completes normally

        try {
            assert false;
            throw new AssertionError("assert false did not throw");
        } catch (AssertionError e) {
            if (e.getMessage() != null)
                throw new AssertionError("no-message assert had detail: " + e.getMessage());
        }

        try {
            assert false : "custom-" + 42;
            throw new AssertionError("assert with Expression2 did not throw");
        } catch (AssertionError e) {
            if (!"custom-42".equals(e.getMessage()))
                throw new AssertionError("Expression2 message wrong: " + e.getMessage());
        }

        System.out.println("PASS");
    }
}
