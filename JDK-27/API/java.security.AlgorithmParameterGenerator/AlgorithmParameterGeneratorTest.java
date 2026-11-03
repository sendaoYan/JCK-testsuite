/*
 * @test
 * @summary java.security.AlgorithmParameterGenerator conformance: white-box structure + black-box behavior.
 * @run main AlgorithmParameterGeneratorTest
 */

import java.lang.reflect.Modifier;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.spec.DSAParameterSpec;

public class AlgorithmParameterGeneratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmParameterGenerator");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        checkMethods(c, new String[]{"getAlgorithm", "getInstance", "getProvider",
            "init", "generateParameters"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AlgorithmParameterGenerator apg = AlgorithmParameterGenerator.getInstance("DSA");
        check("DSA".equals(apg.getAlgorithm()), "getAlgorithm DSA");
        check(apg.getProvider() != null, "getProvider non-null");

        apg.init(2048);
        AlgorithmParameters params = apg.generateParameters();
        check(params != null, "generateParameters non-null");
        check("DSA".equals(params.getAlgorithm()), "generated params algorithm DSA");

        DSAParameterSpec spec = params.getParameterSpec(DSAParameterSpec.class);
        check(spec != null, "DSA parameter spec non-null");
        check(spec.getP() != null && spec.getQ() != null && spec.getG() != null, "DSA p/q/g non-null");
        check(spec.getP().bitLength() == 2048, "DSA p bitLength 2048");

        // init(int, SecureRandom) variant
        AlgorithmParameterGenerator apg2 = AlgorithmParameterGenerator.getInstance("DSA");
        apg2.init(2048, new java.security.SecureRandom());
        AlgorithmParameters params2 = apg2.generateParameters();
        check(params2 != null, "generateParameters (with SecureRandom) non-null");

        // getInstance(alg, provider)
        AlgorithmParameterGenerator apgViaProvider = AlgorithmParameterGenerator.getInstance("DSA", "SUN");
        check("DSA".equals(apgViaProvider.getAlgorithm()), "getInstance(alg,provider)");
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
