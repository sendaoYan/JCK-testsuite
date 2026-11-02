/*
 * @test
 * @summary javax.security.auth.kerberos.KeyTab conformance: unbound keytab state.
 * @run main KeyTabTest
 */

import javax.security.auth.kerberos.*;
import java.io.File;

public class KeyTabTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyTab kt = KeyTab.getUnboundInstance();
        check(!kt.exists(), "unbound does not exist");
        check(!kt.isBound(), "unbound not bound");
        check(kt.getPrincipal() == null, "unbound principal null");
        KerberosPrincipal p = new KerberosPrincipal("a@EXAMPLE.COM");
        check(kt.getKeys(p).length == 0, "no keys for unbound");
        KeyTab kt2 = KeyTab.getInstance(new File("/nonexistent/keytab"));
        check(!kt2.exists(), "nonexistent file not exists");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
