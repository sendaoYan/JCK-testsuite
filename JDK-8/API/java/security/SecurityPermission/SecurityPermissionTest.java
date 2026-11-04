/*
 * @test
 * @summary java.security.SecurityPermission conformance: constructors, getName, getActions, implies.
 * @run main SecurityPermissionTest
 */

public class SecurityPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.SecurityPermission sp = new java.security.SecurityPermission("getProperty.security.provider");
        check("getProperty.security.provider".equals(sp.getName()), "getName");
        check(sp.implies(sp), "implies self");
        java.security.SecurityPermission sp2 = new java.security.SecurityPermission("setPolicy", "read");
        check("".equals(sp2.getActions()), "getActions empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
