/*
 * @test
 * @summary java.lang.NoSuchFieldError API conformance: constructors and inheritance.
 * @run main NoSuchFieldErrorTest
 */

public class NoSuchFieldErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNoSuchFieldError() {
        NoSuchFieldError e = new NoSuchFieldError("m");
        check(e instanceof IncompatibleClassChangeError, "extends IncompatibleClassChangeError");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testNoSuchFieldError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
