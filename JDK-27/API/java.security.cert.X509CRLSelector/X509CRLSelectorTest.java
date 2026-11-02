/*
 * @test
 * @summary java.security.cert.X509CRLSelector conformance: constructor and accessors.
 * @run main X509CRLSelectorTest
 */

import java.security.cert.X509CRLSelector;
import java.math.BigInteger;
import java.util.Date;

public class X509CRLSelectorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        X509CRLSelector sel = new X509CRLSelector();
        BigInteger min = new BigInteger("1");
        sel.setMinCRLNumber(min);
        check(min.equals(sel.getMinCRL()), "setMinCRLNumber/getMinCRL");
        BigInteger max = new BigInteger("999");
        sel.setMaxCRLNumber(max);
        check(max.equals(sel.getMaxCRL()), "setMaxCRLNumber/getMaxCRL");
        Date d = new Date();
        sel.setDateAndTime(d);
        check(d.equals(sel.getDateAndTime()), "setDateAndTime/getDateAndTime");
        Object clone = sel.clone();
        check(clone instanceof X509CRLSelector, "clone type");
        check(min.equals(((X509CRLSelector) clone).getMinCRL()), "clone minCRL preserved");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
