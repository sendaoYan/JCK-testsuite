/*
 * @test
 * @summary com.sun.management.VMOption conformance: behavior of constructors, accessors and value semantics.
 * @run main VMOptionTest
 */

public class VMOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.management.VMOption.Origin origin = com.sun.management.VMOption.Origin.VM_CREATION;
        com.sun.management.VMOption v = new com.sun.management.VMOption("UseG1GC", "true", false, origin);
        check("UseG1GC".equals(v.getName()), "name");
        check("true".equals(v.getValue()), "value");
        check(v.getOrigin() == origin, "origin");
        check(!v.isWriteable(), "isWriteable");
        check(v.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
