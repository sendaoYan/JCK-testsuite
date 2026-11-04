/*
 * @test
 * @summary javax.net.ssl.SSLPermission conformance: constructors, getName, getActions, implies.
 * @run main SSLPermissionTest
 */

public class SSLPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLPermission p = new javax.net.ssl.SSLPermission("setHostnameVerifier");
        check("setHostnameVerifier".equals(p.getName()), "getName");
        check(p.implies(p), "implies self");
        javax.net.ssl.SSLPermission p2 = new javax.net.ssl.SSLPermission("a", "b");
        check("".equals(p2.getActions()), "getActions empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
