/*
 * @test
 * @summary java.lang.NoSuchMethodError API conformance: constructors and inheritance.
 * @run main NoSuchMethodErrorTest
 */

public class NoSuchMethodErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNoSuchMethodError() {
        NoSuchMethodError e = new NoSuchMethodError("m");
        check(e instanceof IncompatibleClassChangeError, "extends IncompatibleClassChangeError");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testNoSuchMethodError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
