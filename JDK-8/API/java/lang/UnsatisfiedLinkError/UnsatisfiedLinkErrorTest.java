/*
 * @test
 * @summary java.lang.UnsatisfiedLinkError API conformance: constructors and inheritance.
 * @run main UnsatisfiedLinkErrorTest
 */

public class UnsatisfiedLinkErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnsatisfiedLinkError() {
        UnsatisfiedLinkError e = new UnsatisfiedLinkError("m");
        check(e instanceof LinkageError, "extends LinkageError");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new UnsatisfiedLinkError().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testUnsatisfiedLinkError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
