/*
 * @test
 * @summary JLS 12.3.1: a verification error is reported as a VerifyError, a
 *          subclass of LinkageError.
 * @run main VerifyErrorIsLinkageError
 */

public class VerifyErrorIsLinkageError {
    public static void main(String[] args) {
        if (!LinkageError.class.isAssignableFrom(VerifyError.class))
            throw new AssertionError("VerifyError is not a LinkageError");
        System.out.println("PASS");
    }
}
