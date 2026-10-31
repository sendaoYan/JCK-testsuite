/*
 * @test
 * @summary java.lang.StackOverflowError API conformance: constructors and inheritance.
 * @run main StackOverflowErrorTest
 */

public class StackOverflowErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStackOverflowError() {
        StackOverflowError e = new StackOverflowError("m");
        check(e instanceof VirtualMachineError, "extends VirtualMachineError");
        check(e instanceof Error, "extends Error");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new StackOverflowError().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testStackOverflowError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
