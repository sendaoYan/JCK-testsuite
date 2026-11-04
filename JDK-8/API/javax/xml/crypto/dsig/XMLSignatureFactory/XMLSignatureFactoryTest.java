/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignatureFactory conformance: XML signature factory.
 * @run main XMLSignatureFactoryTest
 */


public class XMLSignatureFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.crypto.dsig.XMLSignatureFactory sf = javax.xml.crypto.dsig.XMLSignatureFactory.getInstance();
        check(sf != null, "factory non-null");
        check(sf.getMechanismType() != null, "mechanism type");
        check(sf.getProvider() != null, "provider non-null");
        javax.xml.crypto.dsig.DigestMethod dm = sf.newDigestMethod("http://www.w3.org/2000/09/xmldsig#sha1", null);
        check(dm != null, "digest method non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
