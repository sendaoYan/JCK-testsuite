/*
 * @test
 * @summary java.security.AlgorithmParameters conformance: white-box structure + black-box behavior.
 * @run main AlgorithmParametersTest
 */

import java.lang.reflect.Modifier;
import java.security.AlgorithmParameters;
import java.util.Arrays;
import javax.crypto.spec.IvParameterSpec;

public class AlgorithmParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmParameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        checkMethods(c, new String[]{"getAlgorithm", "getInstance", "getProvider",
            "init", "getParameterSpec", "getEncoded", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] iv = new byte[16];
        for (int i = 0; i < iv.length; i++) iv[i] = (byte) i;

        AlgorithmParameters ap = AlgorithmParameters.getInstance("AES");
        check("AES".equals(ap.getAlgorithm()), "getAlgorithm AES");
        check(ap.getProvider() != null, "getProvider non-null");

        ap.init(new IvParameterSpec(iv));
        IvParameterSpec spec = ap.getParameterSpec(IvParameterSpec.class);
        check(Arrays.equals(iv, spec.getIV()), "init(IvParameterSpec) -> getParameterSpec roundtrip");

        // getEncoded roundtrip (encoded form -> new params -> same IV)
        byte[] encoded = ap.getEncoded();
        check(encoded != null && encoded.length > 0, "getEncoded non-empty");
        AlgorithmParameters ap2 = AlgorithmParameters.getInstance("AES");
        ap2.init(encoded);
        check(Arrays.equals(iv, ap2.getParameterSpec(IvParameterSpec.class).getIV()),
            "getEncoded -> init(byte[]) -> getParameterSpec roundtrip");

        // getEncoded(String format) with default format
        byte[] encodedDefault = ap.getEncoded("ASN.1");
        check(encodedDefault != null && encodedDefault.length > 0, "getEncoded(ASN.1) non-empty");

        // toString non-null
        check(ap.toString() != null, "toString non-null");

        // getInstance(alg, provider)
        AlgorithmParameters apViaProvider = AlgorithmParameters.getInstance("AES", "SunJCE");
        check("AES".equals(apViaProvider.getAlgorithm()), "getInstance(alg,provider)");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
