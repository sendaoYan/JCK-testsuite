/*
 * @test
 * @summary java.security.AlgorithmParameterGeneratorSpi conformance: white-box structure + black-box behavior.
 * @run main AlgorithmParameterGeneratorSpiTest
 */

import java.lang.reflect.Modifier;
import java.security.AlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public class AlgorithmParameterGeneratorSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmParameterGeneratorSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
    }

    static void testBlackBox() throws Exception {
        MyApgSpi g = new MyApgSpi();
        g.doInit(1024, null);
        g.doInit(new DummySpec(), null);
        AlgorithmParameters p = g.doGenerateParameters();
        check(p != null, "engineGenerateParameters non-null");
        check(p.getAlgorithm() != null, "generated parameters algorithm");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static class MyApgSpi extends AlgorithmParameterGeneratorSpi {
        protected void engineInit(int size, SecureRandom random) { }
        protected void engineInit(AlgorithmParameterSpec genParamSpec, SecureRandom random) throws java.security.InvalidAlgorithmParameterException { }
        protected AlgorithmParameters engineGenerateParameters() {
            try { return AlgorithmParameters.getInstance("DSA"); }
            catch (GeneralSecurityException e) { throw new RuntimeException(e); }
        }
        void doInit(int s, SecureRandom r) { engineInit(s, r); }
        void doInit(AlgorithmParameterSpec ps, SecureRandom r) throws java.security.InvalidAlgorithmParameterException { engineInit(ps, r); }
        AlgorithmParameters doGenerateParameters() { return engineGenerateParameters(); }
    }

    static class DummySpec implements AlgorithmParameterSpec { }
}
