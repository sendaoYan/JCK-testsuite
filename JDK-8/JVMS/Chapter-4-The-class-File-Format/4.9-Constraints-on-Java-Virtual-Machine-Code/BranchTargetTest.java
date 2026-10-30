/*
 * @test
 * @summary JVMS 4.9: a branch instruction whose target lies outside the code
 *          array violates a static constraint and must be rejected when the
 *          class is loaded (with VerifyError or ClassFormatError).
 * @compile BadBranch.jasm
 * @run main BranchTargetTest
 */

public class BranchTargetTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("BadBranch");
            throw new AssertionError("bad branch target not rejected");
        } catch (VerifyError | ClassFormatError e) {
            // expected per JVMS 4.9
        }

        System.out.println("PASS");
    }
}
