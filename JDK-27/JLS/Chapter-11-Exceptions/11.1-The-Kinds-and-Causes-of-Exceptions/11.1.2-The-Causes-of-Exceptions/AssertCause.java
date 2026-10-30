/*
 * @test
 * @summary JLS 11.1.2: an enabled assert statement whose boolean expression is
 *          false throws an AssertionError.
 * @run main/othervm -ea AssertCause
 */

public class AssertCause {
    public static void main(String[] args) {
        try {
            assert false : "assertion message";
            throw new AssertionError("assert did not throw");
        } catch (AssertionError e) {
            if (!"assertion message".equals(e.getMessage()))
                throw new AssertionError("wrong assertion message");
        }
        System.out.println("PASS");
    }
}
