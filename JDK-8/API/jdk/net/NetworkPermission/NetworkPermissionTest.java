/*
 * @test
 * @summary jdk.net.NetworkPermission conformance: network permission.
 * @run main NetworkPermissionTest
 */

public class NetworkPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        jdk.net.NetworkPermission p = new jdk.net.NetworkPermission("a", "b");
        check("a".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions is empty (BasicPermission ignores actions)");
        check(p.implies(new jdk.net.NetworkPermission("a", "b")), "implies");
        jdk.net.NetworkPermission p2 = new jdk.net.NetworkPermission("a", "b");
        check(p.equals(p2), "equals");
        check(p.hashCode() == p2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
