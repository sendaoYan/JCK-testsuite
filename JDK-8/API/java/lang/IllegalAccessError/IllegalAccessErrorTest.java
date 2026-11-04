/*
 * @test
 * @summary java.lang.IllegalAccessError API conformance: constructors and inheritance.
 * @run main IllegalAccessErrorTest
 */

public class IllegalAccessErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalAccessError() {
        IllegalAccessError e = new IllegalAccessError("m");
        check(e instanceof IncompatibleClassChangeError, "extends IncompatibleClassChangeError");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testIllegalAccessError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
