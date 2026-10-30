/*
 * @test
 * @summary JVMS 4.10: the verifier rejects a method whose operand-stack type
 *          does not match its descriptor (aconst_null then ireturn), throwing
 *          VerifyError when the class is linked.
 * @compile BadVerify.jasm
 * @run main VerificationTest
 */

public class VerificationTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("BadVerify");
            throw new AssertionError("no VerifyError thrown");
        } catch (VerifyError e) {
            // expected per JVMS 4.10
        }

        System.out.println("PASS");
    }
}
