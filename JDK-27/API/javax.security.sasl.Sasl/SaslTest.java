/*
 * @test
 * @summary javax.security.sasl.Sasl conformance: SASL constants and factory lookup.
 * @run main SaslTest
 */

import javax.security.sasl.*;

public class SaslTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check("javax.security.sasl.qop".equals(Sasl.QOP), "QOP");
        check("javax.security.sasl.strength".equals(Sasl.STRENGTH), "STRENGTH");
        check("javax.security.sasl.server.authentication".equals(Sasl.SERVER_AUTH), "SERVER_AUTH");
        check("javax.security.sasl.maxbuffer".equals(Sasl.MAX_BUFFER), "MAX_BUFFER");
        check(Sasl.getSaslClientFactories() != null, "client factories non-null");
        check(Sasl.getSaslServerFactories() != null, "server factories non-null");
        SaslClient sc = Sasl.createSaslClient(new String[]{"NO-SUCH-MECH"}, null, null, null, null, null);
        check(sc == null, "unknown client mechanism null");
        SaslServer ss = Sasl.createSaslServer("NO-SUCH-MECH", null, null, null, null);
        check(ss == null, "unknown server mechanism null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
