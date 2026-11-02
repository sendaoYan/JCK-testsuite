/*
 * @test
 * @summary javax.xml.crypto.dsig.spec.HMACParameterSpec conformance: HMAC parameter spec.
 * @run main HMACParameterSpecTest
 */


public class HMACParameterSpecTest {
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
        javax.xml.crypto.dsig.spec.HMACParameterSpec s = new javax.xml.crypto.dsig.spec.HMACParameterSpec(128);
        check(s.getOutputLength() == 128, "output length");
        check(s instanceof javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec, "is SignatureMethodParameterSpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
