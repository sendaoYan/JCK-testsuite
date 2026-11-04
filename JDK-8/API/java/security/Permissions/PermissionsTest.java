/*
 * @test
 * @summary java.security.Permissions conformance: add and implies.
 * @run main PermissionsTest
 */

public class PermissionsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.Permissions perms = new java.security.Permissions();
        check(!perms.implies(new java.security.AllPermission()), "implies false initially");
        perms.add(new java.security.AllPermission());
        check(perms.implies(new java.security.AllPermission()), "implies after add");
        check(perms.elements() != null, "elements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
