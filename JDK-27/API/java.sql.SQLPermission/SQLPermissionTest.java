/*
 * @test
 * @summary java.sql.SQLPermission conformance: permission name and actions
 * @run main SQLPermissionTest
 */

@SuppressWarnings({"deprecation", "removal"})
public class SQLPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.SQLPermission p = new java.sql.SQLPermission("setLog");
        check("setLog".equals(p.getName()), "name");
        check("".equals(p.getActions()), "empty actions");
        java.sql.SQLPermission p2 = new java.sql.SQLPermission("setLog", "x");
        check("setLog".equals(p2.getName()), "2-arg name");
        check("".equals(p2.getActions()), "getActions always empty for BasicPermission");
        check(p.implies(p2), "implies");
        check(p2.implies(p), "implies symmetric");        System.out.println("PASS (" + passed + " assertions)");
    }
}
