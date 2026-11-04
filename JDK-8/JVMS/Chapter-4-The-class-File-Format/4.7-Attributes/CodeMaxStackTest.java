/*
 * @test
 * @summary JVMS 4.x: JVMS 4.7.3: max_stack smaller than the actual operand stack depth is rejected by the verifier with VerifyError
 * @compile CodeMaxStack.jasm
 * @run main CodeMaxStackTest
 */

public class CodeMaxStackTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("CodeMaxStack");
            check(false, "CodeMaxStack not rejected");
        } catch (VerifyError e) {
            check(true, "CodeMaxStack rejected with VerifyError: " + e.getMessage());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
