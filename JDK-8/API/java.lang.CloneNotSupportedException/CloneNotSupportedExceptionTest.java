/*
 * @test
 * @summary java.lang.CloneNotSupportedException API conformance: constructors and inheritance.
 * @run main CloneNotSupportedExceptionTest
 */

public class CloneNotSupportedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCloneNotSupportedException() {
        CloneNotSupportedException e = new CloneNotSupportedException("m");
        check(e instanceof Exception, "extends Exception");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new CloneNotSupportedException().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testCloneNotSupportedException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
