/*
 * @test
 * @summary JVMS 4.10.1.9: invokespecial on a public method of an unrelated
 *          class (not <init>, not private, not a superclass method) is rejected
 *          by the verifier with VerifyError.
 * @compile InvokeSpecialConstraint.jasm
 * @run main InvokeSpecialConstraintTest
 */

public class InvokeSpecialConstraintTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("InvokeSpecialConstraint");
            throw new AssertionError("invalid invokespecial target not rejected");
        } catch (VerifyError e) {
            // expected per JVMS 4.10.1.9 (invokespecial constraint)
        }

        System.out.println("PASS");
    }
}
