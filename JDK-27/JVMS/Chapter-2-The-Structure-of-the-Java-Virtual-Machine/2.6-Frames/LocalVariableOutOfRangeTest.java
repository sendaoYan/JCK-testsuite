/*
 * @test
 * @summary JVMS 2.6.1: a local variable index must be within the local variable
 *          array; a method with max_locals=1 that reads index 1 is rejected
 *          with VerifyError when verified.
 * @compile LocalVariableOutOfRange.jasm
 * @run main LocalVariableOutOfRangeTest
 */

public class LocalVariableOutOfRangeTest {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        boolean threw = false;
        try {
            Class<?> c = Class.forName("LocalVariableOutOfRange");
            c.getMethod("bad").invoke(null);
        } catch (VerifyError e) {
            threw = true;
        }
        check(threw, "out-of-range local variable rejected with VerifyError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
