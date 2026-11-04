/*
 * @test
 * @summary JVMS 4.x: JVMS 4.9: a store/load of a local whose index exceeds max_locals-1 is rejected by the verifier with VerifyError
 * @compile LocalVariableOverflow.jasm
 * @run main LocalVariableOverflowTest
 */

public class LocalVariableOverflowTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("LocalVariableOverflow");
            check(false, "LocalVariableOverflow not rejected");
        } catch (VerifyError e) {
            check(true, "LocalVariableOverflow rejected with VerifyError: " + e.getMessage());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
