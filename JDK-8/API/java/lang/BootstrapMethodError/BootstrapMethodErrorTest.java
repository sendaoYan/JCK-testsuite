/*
 * @test
 * @summary java.lang.BootstrapMethodError API conformance: constructors and inheritance.
 * @run main BootstrapMethodErrorTest
 */

public class BootstrapMethodErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBootstrapMethodError() {
        BootstrapMethodError e = new BootstrapMethodError("m");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new BootstrapMethodError().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testBootstrapMethodError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
