/*
 * @test
 * @summary java.lang.IncompatibleClassChangeError API conformance: constructors and inheritance.
 * @run main IncompatibleClassChangeErrorTest
 */

public class IncompatibleClassChangeErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIncompatibleClassChangeError() {
        IncompatibleClassChangeError e = new IncompatibleClassChangeError("m");
        check(e instanceof LinkageError, "extends LinkageError");
        check(e instanceof Error, "extends Error");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testIncompatibleClassChangeError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
