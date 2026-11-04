/*
 * @test
 * @summary JVMS 4.x: JVMS 4.9: an instruction whose opcode is undefined is rejected by the verifier with VerifyError
 * @compile IllegalOpcode.jasm
 * @run main IllegalOpcodeTest
 */

public class IllegalOpcodeTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("IllegalOpcode");
            check(false, "IllegalOpcode not rejected");
        } catch (VerifyError e) {
            check(true, "IllegalOpcode rejected with VerifyError: " + e.getMessage());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
