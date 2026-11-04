/*
 * @test
 * @summary JVMS 6.5: jsr/jsr_w push the address of the following instruction
 *          and ret returns to that address.  jsr/ret are not permitted in
 *          class files of version 51.0 or later, so this uses version 50.0.
 * @compile JsrOps.jasm
 * @run main JsrOpsTest
 */

public class JsrOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(JsrOps.jsr() == 7, "jsr round-trip");
        check(JsrOps.jsrW() == 9, "jsr_w round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
