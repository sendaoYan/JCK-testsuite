/*
 * @test
 * @summary JVMS 6.5 / 4.10: an instruction whose operand-stack types do not
 *          match its operands (lload then iadd) is rejected by the verifier
 *          with VerifyError when the class is linked.
 * @compile TypeMismatchVerify.jasm
 * @run main TypeMismatchVerifyTest
 */

public class TypeMismatchVerifyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("TypeMismatchVerify");
            throw new AssertionError("no VerifyError thrown");
        } catch (VerifyError e) {
            // expected per JVMS 4.10 (operand stack type mismatch)
        }
        check(true, "VerifyError on type mismatch");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
