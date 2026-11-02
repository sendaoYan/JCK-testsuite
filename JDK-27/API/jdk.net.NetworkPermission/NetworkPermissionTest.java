/*
 * @test
 * @summary jdk.net.NetworkPermission conformance: network permission.
 * @run main NetworkPermissionTest
 */

public class NetworkPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        jdk.net.NetworkPermission p = new jdk.net.NetworkPermission("unixDomainSocket");
        check("unixDomainSocket".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "actions empty");
        jdk.net.NetworkPermission p2 = new jdk.net.NetworkPermission("unixDomainSocket", "accept");
        check("".equals(p2.getActions()), "actions empty with actions arg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
