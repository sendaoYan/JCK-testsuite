/*
 * @test
 * @summary java.security.cert.PolicyQualifierInfo conformance: constructor and getters.
 * @run main PolicyQualifierInfoTest
 */

public class PolicyQualifierInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] der = {0x30, 0x07, 0x06, 0x03, 0x2A, 0x03, 0x04, 0x05, 0x00};
        java.security.cert.PolicyQualifierInfo p = new java.security.cert.PolicyQualifierInfo(der);
        check("1.2.3.4".equals(p.getPolicyQualifierId()), "getPolicyQualifierId");
        check(java.util.Arrays.equals(der, p.getEncoded()), "getEncoded");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
