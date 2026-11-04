/*
 * @test
 * @summary java.security.SignedObject conformance: constructor, getObject, verify.
 * @run main SignedObjectTest
 */

public class SignedObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.Signature s = java.security.Signature.getInstance("SHA256withRSA");
        s.initSign(kp.getPrivate());
        java.security.SignedObject so = new java.security.SignedObject("data", kp.getPrivate(), s);
        check("data".equals(so.getObject()), "getObject");
        check("SHA256withRSA".equals(so.getAlgorithm()), "getAlgorithm");
        java.security.Signature v = java.security.Signature.getInstance("SHA256withRSA");
        v.initVerify(kp.getPublic());
        check(so.verify(kp.getPublic(), v), "verify");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
