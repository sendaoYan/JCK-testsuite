/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosCredMessage conformance: kerberos credential message.
 * @run main KerberosCredMessageTest
 */

import javax.security.auth.kerberos.*;
import java.util.Arrays;

public class KerberosCredMessageTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KerberosPrincipal s = new KerberosPrincipal("a@EXAMPLE.COM");
        KerberosPrincipal r = new KerberosPrincipal("b@EXAMPLE.COM");
        KerberosCredMessage m = new KerberosCredMessage(s, r, new byte[]{1, 2, 3});
        check("a@EXAMPLE.COM".equals(m.getSender().getName()), "getSender");
        check("b@EXAMPLE.COM".equals(m.getRecipient().getName()), "getRecipient");
        check(Arrays.equals(new byte[]{1, 2, 3}, m.getEncoded()), "getEncoded");
        m.destroy();
        check(m.isDestroyed(), "destroyed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
