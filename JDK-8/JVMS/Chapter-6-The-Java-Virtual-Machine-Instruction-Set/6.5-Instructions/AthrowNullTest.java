/*
 * @test
 * @summary JVMS 6.5: athrow with a null reference throws
 *          NullPointerException rather than an arbitrary exception.
 * @compile AthrowNull.jasm
 * @run main AthrowNullTest
 */

public class AthrowNullTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        try {
            AthrowNull.throwNull();
            throw new AssertionError("athrow null did not throw");
        } catch (NullPointerException e) { }
        check(true, "athrow null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
