/*
 * @test
 * @summary java.security.CodeSigner conformance: constructor and accessors.
 * @run main CodeSignerTest
 */

import java.security.CodeSigner;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.Date;

public class CodeSignerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CertPath cp = CertificateFactory.getInstance("X.509").generateCertPath(Collections.emptyList());
        Timestamp ts = new Timestamp(new Date(), cp);
        CodeSigner signer = new CodeSigner(cp, ts);
        check(cp.equals(signer.getSignerCertPath()), "getSignerCertPath");
        check(ts.equals(signer.getTimestamp()), "getTimestamp");
        CodeSigner signer2 = new CodeSigner(cp, ts);
        check(signer.equals(signer2), "equals");
        check(signer.hashCode() == signer2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
