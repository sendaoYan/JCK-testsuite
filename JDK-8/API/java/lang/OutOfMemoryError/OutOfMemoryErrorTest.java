/*
 * @test
 * @summary java.lang.OutOfMemoryError API conformance: constructors and inheritance.
 * @run main OutOfMemoryErrorTest
 */

public class OutOfMemoryErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOutOfMemoryError() {
        OutOfMemoryError e = new OutOfMemoryError("m");
        check(e instanceof VirtualMachineError, "extends VirtualMachineError");
        check(e instanceof Error, "extends Error");
        check("m".equals(e.getMessage()), "message from (String) ctor");
        check(new OutOfMemoryError().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testOutOfMemoryError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
