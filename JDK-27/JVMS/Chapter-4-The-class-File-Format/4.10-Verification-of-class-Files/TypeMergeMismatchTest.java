/*
 * @test
 * @summary JVMS 4.10.1: at a control-flow join, an operand-stack type that is
 *          not assignable to the StackMapTable frame's type (an int on one
 *          branch, a reference on the other) is rejected with VerifyError.
 * @compile TypeMergeMismatch.jasm
 * @run main TypeMergeMismatchTest
 */

public class TypeMergeMismatchTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("TypeMergeMismatch");
            throw new AssertionError("inconsistent stack types at join not rejected");
        } catch (VerifyError e) {
            // expected per JVMS 4.10.1 (int cannot merge with a reference)
        }

        System.out.println("PASS");
    }
}
