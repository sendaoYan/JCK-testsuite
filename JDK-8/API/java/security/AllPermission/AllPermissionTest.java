/*
 * @test
 * @summary java.security.AllPermission conformance: constructors and implies.
 * @run main AllPermissionTest
 */

public class AllPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.AllPermission ap = new java.security.AllPermission();
        check(ap.implies(ap), "implies self");
        check(ap.implies(new java.security.AllPermission()), "implies other AllPermission");
        check(ap.getName() != null, "getName");
        check(ap.getActions() != null, "getActions");
        java.security.AllPermission ap2 = new java.security.AllPermission("n", "a");
        check(ap2.implies(ap), "2-arg ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
