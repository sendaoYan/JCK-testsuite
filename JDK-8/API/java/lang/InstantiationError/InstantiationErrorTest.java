/*
 * @test
 * @summary java.lang.InstantiationError API conformance: constructors and inheritance.
 * @run main InstantiationErrorTest
 */

public class InstantiationErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInstantiationError() {
        InstantiationError e = new InstantiationError("m");
        check(e instanceof IncompatibleClassChangeError, "extends IncompatibleClassChangeError");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testInstantiationError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
