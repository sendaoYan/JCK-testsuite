/*
 * @test
 * @summary java.lang.AbstractMethodError API conformance: constructors and inheritance.
 * @run main AbstractMethodErrorTest
 */

public class AbstractMethodErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractMethodError() {
        AbstractMethodError e = new AbstractMethodError("m");
        check(e instanceof IncompatibleClassChangeError, "extends IncompatibleClassChangeError");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testAbstractMethodError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
