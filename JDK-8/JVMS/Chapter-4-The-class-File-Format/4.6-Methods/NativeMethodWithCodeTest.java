/*
 * @test
 * @summary JVMS 4.6 / 4.7.3: a method with the ACC_NATIVE flag set must not
 *          have a Code attribute; a class file that attaches bytecode to a
 *          native method is rejected with ClassFormatError before the class
 *          can be used.
 * @compile NativeMethodWithCode.jasm
 * @run main NativeMethodWithCodeTest
 */

public class NativeMethodWithCodeTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("NativeMethodWithCode");
            check(false, "native method with a Code attribute must be rejected");
        } catch (ClassFormatError e) {
            check(e.getMessage() != null
                            && e.getMessage().toLowerCase().contains("code"),
                    "rejection names the Code attribute (got: " + e.getMessage() + ")");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
