/*
 * @test
 * @summary java.lang.UnknownError API conformance: constructors and inheritance.
 * @run main UnknownErrorTest
 */

public class UnknownErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUnknownError() {
        UnknownError e = new UnknownError("m");
        check(e instanceof VirtualMachineError, "extends VirtualMachineError");
        check(e instanceof Error, "extends Error");
        check("m".equals(e.getMessage()), "message from (String) ctor");
    }

    public static void main(String[] args) {
        testUnknownError();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
