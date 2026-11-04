/*
 * @test
 * @summary java.security.cert.X509CRLSelector conformance: setters and getters.
 * @run main X509CRLSelectorTest
 */

public class X509CRLSelectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.X509CRLSelector s = new java.security.cert.X509CRLSelector();
        java.util.Date d = new java.util.Date(1000L);
        s.setDateAndTime(d);
        check(d.equals(s.getDateAndTime()), "set/getDateAndTime");
        s.setMinCRLNumber(new java.math.BigInteger("5"));
        check(new java.math.BigInteger("5").equals(s.getMinCRL()), "set/getMinCRL");
        s.setIssuerNames(java.util.Arrays.asList("CN=Issuer"));
        check(s.getIssuerNames() != null, "set/getIssuerNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
