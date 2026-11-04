/*
 * @test
 * @summary java.security.cert.X509CertSelector conformance: setters and getters.
 * @run main X509CertSelectorTest
 */

public class X509CertSelectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.X509CertSelector s = new java.security.cert.X509CertSelector();
        s.setSubject("CN=Test");
        check("CN=Test".equals(s.getSubjectAsString()), "set/getSubject");
        java.math.BigInteger ser = new java.math.BigInteger("123456");
        s.setSerialNumber(ser);
        check(ser.equals(s.getSerialNumber()), "set/getSerialNumber");
        s.setIssuer("CN=Issuer");
        check("CN=Issuer".equals(s.getIssuerAsString()), "set/getIssuer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
