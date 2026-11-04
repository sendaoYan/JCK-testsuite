/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosTicket conformance: constructor, getters, destroy.
 * @run main KerberosTicketTest
 */

public class KerberosTicketTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.kerberos.KerberosPrincipal client = new javax.security.auth.kerberos.KerberosPrincipal("client@REALM");
        javax.security.auth.kerberos.KerberosPrincipal server = new javax.security.auth.kerberos.KerberosPrincipal("krbtgt/REALM@REALM");
        long now = System.currentTimeMillis();
        javax.security.auth.kerberos.KerberosTicket t = new javax.security.auth.kerberos.KerberosTicket(
            new byte[]{1,2,3}, client, server, new byte[16], 18, new boolean[11],
            new java.util.Date(now - 1000), new java.util.Date(now - 1000),
            new java.util.Date(now + 100000), new java.util.Date(now + 100000), null);
        check(client.equals(t.getClient()), "getClient");
        check(server.equals(t.getServer()), "getServer");
        check(t.getSessionKeyType() == 18, "getSessionKeyType");
        check(t.getFlags().length >= 11, "getFlags");
        check(!t.isDestroyed(), "not destroyed");
        t.destroy();
        check(t.isDestroyed(), "destroyed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
