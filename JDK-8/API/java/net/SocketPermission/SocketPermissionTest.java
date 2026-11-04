/*
 * @test
 * @summary java.net.SocketPermission conformance: name/actions/implies.
 * @run main SocketPermissionTest
 */


public class SocketPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.net.SocketPermission p = new java.net.SocketPermission("localhost:80", "connect,resolve");
        check("localhost:80".equals(p.getName()), "getName");
        check("connect,resolve".equals(p.getActions()), "getActions");
        java.net.SocketPermission p2 = new java.net.SocketPermission("localhost", "resolve");
        check(p.implies(p2), "implies resolve");        System.out.println("PASS (" + passed + " assertions)");
    }
}
