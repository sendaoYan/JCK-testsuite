/*
 * @test
 * @summary java.net.NetPermission conformance: name/actions.
 * @run main NetPermissionTest
 */


public class NetPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.NetPermission p = new java.net.NetPermission("setDefaultAuthenticator");
        check("setDefaultAuthenticator".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");        System.out.println("PASS (" + passed + " assertions)");
    }
}
