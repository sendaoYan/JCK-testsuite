/*
 * @test
 * @summary java.lang.VerifyError API conformance: constructors and inheritance.
 * @run main VerifyErrorTest
 */

public class VerifyErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testVerifyError() {
        VerifyError e = new VerifyError("m");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testVerifyError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
