/*
 * @test
 * @summary java.sql.SQLPermission conformance: name.
 * @run main SQLPermissionTest
 */


public class SQLPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLPermission p = new java.sql.SQLPermission("setLog");
        check("setLog".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");        System.out.println("PASS (" + passed + " assertions)");
    }
}
