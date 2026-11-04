/*
 * @test
 * @summary java.security.AlgorithmParameters conformance: getInstance/init/getEncoded/getParameterSpec.
 * @run main AlgorithmParametersTest
 */

public class AlgorithmParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.AlgorithmParameters ap = java.security.AlgorithmParameters.getInstance("AES");
        ap.init(new javax.crypto.spec.IvParameterSpec(new byte[16]));
        check(ap.getEncoded() != null, "getEncoded");
        check(ap.getProvider() != null, "getProvider");
        check("AES".equals(ap.getAlgorithm()), "getAlgorithm");
        check(ap.getParameterSpec(javax.crypto.spec.IvParameterSpec.class) != null, "getParameterSpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
