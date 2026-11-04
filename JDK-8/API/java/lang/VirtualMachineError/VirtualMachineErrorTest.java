/*
 * @test
 * @summary java.lang.VirtualMachineError API conformance: constructors and inheritance.
 * @run main VirtualMachineErrorTest
 */

public class VirtualMachineErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testVirtualMachineError() {
        VirtualMachineError e = new StackOverflowError("m");
        check(e instanceof Error, "extends Error");
        check("m".equals(e.getMessage()), "message from subclass (String) ctor");
        check(new OutOfMemoryError().getMessage() == null, "no-arg ctor has null message");
    }

    public static void main(String[] args) {
        testVirtualMachineError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
