/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosTicket conformance: kerberos ticket accessors.
 * @run main KerberosTicketTest
 */

import javax.security.auth.kerberos.*;
import java.util.Date;

public class KerberosTicketTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KerberosPrincipal c = new KerberosPrincipal("c@EXAMPLE.COM");
        KerberosPrincipal s = new KerberosPrincipal("s@EXAMPLE.COM");
        Date now = new Date();
        KerberosTicket kt = new KerberosTicket(new byte[]{1}, c, s, new byte[]{2}, 17, new boolean[12], now, now, now, now, null);
        check("c@EXAMPLE.COM".equals(kt.getClient().getName()), "getClient");
        check("s@EXAMPLE.COM".equals(kt.getServer().getName()), "getServer");
        check(kt.getSessionKeyType() == 17, "getSessionKeyType");
        check(kt.getFlags().length > 0, "getFlags non-empty");
        check(kt.getEncoded().length > 0, "getEncoded non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
