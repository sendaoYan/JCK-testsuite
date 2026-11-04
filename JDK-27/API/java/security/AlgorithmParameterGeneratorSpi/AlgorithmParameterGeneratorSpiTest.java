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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    interface TR { void run() throws Throwable; }

    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static class RecSpi extends java.security.AlgorithmParameterGeneratorSpi {
        int lastSize = -1; java.security.SecureRandom lastRandom;
        java.security.spec.AlgorithmParameterSpec lastSpec; int generated;
        java.security.AlgorithmParameters result;
        protected void engineInit(int size, java.security.SecureRandom random) { lastSize = size; lastRandom = random; }
        protected void engineInit(java.security.spec.AlgorithmParameterSpec spec, java.security.SecureRandom random)
                throws java.security.InvalidAlgorithmParameterException { lastSpec = spec; lastRandom = random; }
        protected java.security.AlgorithmParameters engineGenerateParameters() { generated++; return result; }
        void engineInitPub(int s, java.security.SecureRandom r) { engineInit(s, r); }
        void engineInitSpecPub(java.security.spec.AlgorithmParameterSpec s, java.security.SecureRandom r)
                throws java.security.InvalidAlgorithmParameterException { engineInit(s, r); }
        java.security.AlgorithmParameters engineGenerateParametersPub() { return engineGenerateParameters(); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.AlgorithmParameterGeneratorSpi.class;

        // ---- structure: exactly 3 abstract protected engine methods ----
        check(c.getConstructor() != null, "public no-arg constructor");
        int abstractCount = 0;
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) { abstractCount++; names.add(m.getName()); }
        }
        check(abstractCount == 3, "exactly 3 declared methods, all abstract");
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(
                "engineInit", "engineGenerateParameters"))), "engine method names exact: " + names);
        java.lang.reflect.Method m = c.getDeclaredMethod("engineInit", int.class, java.security.SecureRandom.class);
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineInit(int,SecureRandom) protected");
        m = c.getDeclaredMethod("engineInit", java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class);
        check(m.getExceptionTypes().length == 1
                && m.getExceptionTypes()[0] == java.security.InvalidAlgorithmParameterException.class,
                "engineInit(spec,random) declares InvalidAlgorithmParameterException");
        m = c.getDeclaredMethod("engineGenerateParameters");
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineGenerateParameters protected");

        // ---- pass-through ----
        RecSpi spi = new RecSpi();
        java.security.SecureRandom rnd = new java.security.SecureRandom(new byte[]{3});
        spi.engineInitPub(1024, rnd);
        check(spi.lastSize == 1024 && spi.lastRandom == rnd, "engineInit(int,SecureRandom) pass-through");
        spi.engineInitSpecPub(new java.security.spec.DSAParameterSpec(
                java.math.BigInteger.TEN, java.math.BigInteger.ONE, java.math.BigInteger.TEN), rnd);
        check(spi.lastSpec instanceof java.security.spec.DSAParameterSpec && spi.lastRandom == rnd,
                "engineInit(spec,random) pass-through");
        spi.result = java.security.AlgorithmParameters.getInstance("DSA");
        // (helpers below expose the protected engine methods)
        check(spi.engineGenerateParametersPub() == spi.result, "engineGenerateParameters pass-through");
        check(spi.generated == 1, "engineGenerateParameters count");

        // ---- facade closed loop: AlgorithmParameterGenerator drives a real SPI (DSA) ----
        java.security.AlgorithmParameterGenerator apg = java.security.AlgorithmParameterGenerator.getInstance("DSA");
        apg.init(1024, null);
        java.security.AlgorithmParameters p = apg.generateParameters();
        check(p != null && "DSA".equals(p.getAlgorithm()), "facade generated DSA parameters");
        check(p.getEncoded() != null && p.getEncoded().length > 0, "facade generated parameters carry ASN.1 encoding");
        java.security.spec.DSAParameterSpec ps = p.getParameterSpec(java.security.spec.DSAParameterSpec.class);
        check(ps.getP().bitLength() == 1024 && ps.getQ().bitLength() == 160,
                "facade generated p=1024 bit, q=160 bit");
        java.security.AlgorithmParameters p2 = apg.generateParameters();
        check(p2 != null, "facade repeated generation works");
        ct(java.security.InvalidParameterException.class, () -> apg.init(0, null),
                "facade init(0) -> InvalidParameterException");
        ct(java.security.InvalidParameterException.class, () -> apg.init(-512, null),
                "facade init(-512) -> InvalidParameterException");
        ct(java.security.InvalidAlgorithmParameterException.class,
                () -> apg.init((java.security.spec.AlgorithmParameterSpec) null, null),
                "facade init(null spec, null) -> InvalidAlgorithmParameterException");
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
