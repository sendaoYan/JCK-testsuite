/*
 * @test
 * @summary JVMS 6.5: ireturn/lreturn/freturn/dreturn/areturn return the value
 *          on the operand stack to the caller, and return returns from a void
 *          method.
 * @compile ReturnOps.jasm
 * @run main ReturnOpsTest
 */

public class ReturnOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(ReturnOps.retInt() == 42, "ireturn");
        check(ReturnOps.retLong() == 99L, "lreturn");
        check(ReturnOps.retFloat() == 1.5f, "freturn");
        check(ReturnOps.retDouble() == 2.5, "dreturn");
        check("x".equals(ReturnOps.retRef()), "areturn");
        ReturnOps.retVoid(7);   // returns normally, no value
        check(true, "return (void)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
