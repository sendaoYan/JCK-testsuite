/*
 * @test
 * @summary javax.security.auth.kerberos.KeyTab conformance: getUnboundInstance/isBound.
 * @run main KeyTabTest
 */

public class KeyTabTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.kerberos.KeyTab kt = javax.security.auth.kerberos.KeyTab.getUnboundInstance();
        check(kt != null, "getUnboundInstance non-null");
        check(!kt.isBound(), "unbound");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
