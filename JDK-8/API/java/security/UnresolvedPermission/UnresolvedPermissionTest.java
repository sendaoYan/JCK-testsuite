/*
 * @test
 * @summary java.security.UnresolvedPermission conformance: constructor and getters.
 * @run main UnresolvedPermissionTest
 */

public class UnresolvedPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.UnresolvedPermission up = new java.security.UnresolvedPermission("type", "name", "actions", null);
        check("type".equals(up.getUnresolvedType()), "getUnresolvedType");
        check("type".equals(up.getName()), "getName");
        check("name".equals(up.getUnresolvedName()), "getUnresolvedName");
        check("".equals(up.getActions()), "getActions empty");
        check("actions".equals(up.getUnresolvedActions()), "getUnresolvedActions");
        check(!up.implies(new java.security.AllPermission()), "implies false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
