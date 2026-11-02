/*
 * @test
 * @summary java.security.cert.CertStore conformance: getInstance for Collection type.
 * @run main CertStoreTest
 */

import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;

public class CertStoreTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertStore cs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(new ArrayList<>()));
        check("Collection".equals(cs.getType()), "getType");
        check(cs.getProvider() != null, "getProvider non-null");
        check(cs.getCertStoreParameters() != null, "getCertStoreParameters non-null");
        check(cs.getCertificates(null).isEmpty(), "getCertificates empty");
        check(cs.getCRLs(null).isEmpty(), "getCRLs empty");
        check(CertStore.getDefaultType() != null, "getDefaultType non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
