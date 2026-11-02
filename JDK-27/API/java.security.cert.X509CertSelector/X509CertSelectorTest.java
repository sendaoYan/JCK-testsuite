/*
 * @test
 * @summary java.security.cert.X509CertSelector conformance: constructor and accessors.
 * @run main X509CertSelectorTest
 */

import java.security.cert.X509CertSelector;
import java.math.BigInteger;
import java.util.Date;

public class X509CertSelectorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        X509CertSelector sel = new X509CertSelector();
        check(sel.getBasicConstraints() == -1, "getBasicConstraints default -1");
        sel.setBasicConstraints(0);
        check(sel.getBasicConstraints() == 0, "setBasicConstraints/getBasicConstraints");
        BigInteger sn = new BigInteger("1234567890");
        sel.setSerialNumber(sn);
        check(sn.equals(sel.getSerialNumber()), "setSerialNumber/getSerialNumber");
        Date d = new Date();
        sel.setCertificateValid(d);
        check(d.equals(sel.getCertificateValid()), "setCertificateValid/getCertificateValid");
        Object clone = sel.clone();
        check(clone instanceof X509CertSelector, "clone type");
        check(sn.equals(((X509CertSelector) clone).getSerialNumber()), "clone serialNumber preserved");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
