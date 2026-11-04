/*
 * @test
 * @summary java.lang.InternalError API conformance: constructors and inheritance.
 * @run main InternalErrorTest
 */

public class InternalErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInternalError() {
        InternalError e = new InternalError("m");
        check(e instanceof VirtualMachineError, "extends VirtualMachineError");
        check(e instanceof Error, "extends Error");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testInternalError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
