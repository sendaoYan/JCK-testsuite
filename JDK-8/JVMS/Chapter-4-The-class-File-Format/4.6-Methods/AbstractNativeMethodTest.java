/*
 * @test
 * @summary JVMS 4.6: a method must not have both the ACC_ABSTRACT and
 *          ACC_NATIVE flags set; such a method_info is rejected with
 *          ClassFormatError when the class is derived.
 * @compile AbstractNativeMethod.jasm
 * @run main AbstractNativeMethodTest
 */

public class AbstractNativeMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("AbstractNativeMethod");
            check(false, "abstract native method must be rejected");
        } catch (ClassFormatError e) {
            String msg = String.valueOf(e.getMessage());
            check(msg.toLowerCase().contains("illegal") || msg.toLowerCase().contains("modifier"),
                    "rejection cites the illegal flag combination (got: " + msg + ")");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
