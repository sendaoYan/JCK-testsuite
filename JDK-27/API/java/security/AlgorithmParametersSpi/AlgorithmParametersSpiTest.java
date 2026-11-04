/*
 * @test
 * @summary java.security.AlgorithmParametersSpi conformance: white-box structure + black-box behavior.
 * @run main AlgorithmParametersSpiTest
 */

import java.lang.reflect.Modifier;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class AlgorithmParametersSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmParametersSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
    }

    static void testBlackBox() throws Exception {
        MyApSpi ap = new MyApSpi();
        ap.doInit(new DummySpec());
        ap.doInit(new byte[]{1});
        ap.doInit(new byte[]{1}, "RAW");
        check(Arrays.equals(ap.doGetEncoded(), new byte[]{7}), "engineGetEncoded");
        check(Arrays.equals(ap.doGetEncoded("RAW"), new byte[]{7}), "engineGetEncoded(format)");
        check("MyApSpi".equals(ap.doToString()), "engineToString");
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

    static class RecSpi extends java.security.AlgorithmParametersSpi {
        java.security.spec.AlgorithmParameterSpec lastInitSpec; byte[] lastInitBytes; String lastInitFormat, lastGetFormat;
        int encodedCalls; Class<?> lastSpecClass;
        protected void engineInit(java.security.spec.AlgorithmParameterSpec spec) { lastInitSpec = spec; }
        protected void engineInit(byte[] params) { lastInitBytes = params; }
        protected void engineInit(byte[] params, String format) { lastInitBytes = params; lastInitFormat = format; }
        @SuppressWarnings("unchecked")
        protected <T extends java.security.spec.AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec) {
            lastSpecClass = paramSpec; return (T) lastInitSpec;
        }
        protected byte[] engineGetEncoded() { encodedCalls++; return new byte[]{5}; }
        protected byte[] engineGetEncoded(String format) { lastGetFormat = format; return new byte[]{6}; }
        protected String engineToString() { return "RecSpi-params"; }
        void doInitSpec(java.security.spec.AlgorithmParameterSpec s) throws java.security.spec.InvalidParameterSpecException { engineInit(s); }
        void doInitBytes(byte[] b) throws java.io.IOException { engineInit(b); }
        void doInitBytes(byte[] b, String f) throws java.io.IOException { engineInit(b, f); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.AlgorithmParametersSpi.class;

        // ---- structure: exactly 7 abstract protected engine methods, no defaults ----
        check(c.getConstructor() != null, "public no-arg constructor");
        int abstractCount = 0;
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) { abstractCount++; names.add(m.getName()); }
        }
        check(abstractCount == 7, "exactly 7 declared methods, all abstract");
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(
                "engineInit", "engineGetParameterSpec", "engineGetEncoded", "engineToString"))),
                "engine method names exact: " + names);
        java.lang.reflect.Method m = c.getDeclaredMethod("engineInit", java.security.spec.AlgorithmParameterSpec.class);
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineInit(spec) protected");
        check(m.getExceptionTypes().length == 1
                && m.getExceptionTypes()[0] == java.security.spec.InvalidParameterSpecException.class,
                "engineInit(spec) declares InvalidParameterSpecException");
        m = c.getDeclaredMethod("engineInit", byte[].class);
        check(m.getExceptionTypes()[0] == java.io.IOException.class, "engineInit(byte[]) declares IOException");
        m = c.getDeclaredMethod("engineInit", byte[].class, String.class);
        check(m.getExceptionTypes()[0] == java.io.IOException.class, "engineInit(byte[],String) declares IOException");

        // ---- pass-through ----
        RecSpi spi = new RecSpi();
        java.security.spec.DSAParameterSpec spec = new java.security.spec.DSAParameterSpec(
                java.math.BigInteger.TEN, java.math.BigInteger.ONE, java.math.BigInteger.TEN);
        spi.doInitSpec(spec);
        check(spi.lastInitSpec == spec, "engineInit(spec) pass-through");
        spi.doInitBytes(new byte[]{9});
        check(spi.lastInitBytes != null && spi.lastInitBytes[0] == 9, "engineInit(byte[]) pass-through");
        spi.doInitBytes(new byte[]{8}, "ASN.1");
        check(spi.lastInitBytes[0] == 8 && "ASN.1".equals(spi.lastInitFormat), "engineInit(byte[],String) pass-through");
        check(spi.engineGetParameterSpec(java.security.spec.DSAParameterSpec.class) == spec,
                "engineGetParameterSpec returns initialized spec");
        check(spi.lastSpecClass == java.security.spec.DSAParameterSpec.class, "engineGetParameterSpec class pass-through");
        byte[] e1 = spi.engineGetEncoded();
        byte[] e2 = spi.engineGetEncoded("RAW");
        check(e1 != null && e1.length == 1 && e1[0] == 5, "engineGetEncoded() pass-through");
        check(e2 != null && e2[0] == 6 && "RAW".equals(spi.lastGetFormat), "engineGetEncoded(format) pass-through");
        check(spi.encodedCalls == 1, "engineGetEncoded() and engineGetEncoded(String) are distinct methods");
        check("RecSpi-params".equals(spi.engineToString()), "engineToString pass-through");

        // ---- facade closed loop: AlgorithmParameters drives a real SPI (DSA) ----
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("DSA");
        java.security.spec.DSAPublicKeySpec pubSpec =
                kf.getKeySpec(kp.getPublic(), java.security.spec.DSAPublicKeySpec.class);
        java.security.AlgorithmParameters ap = java.security.AlgorithmParameters.getInstance("DSA");
        ap.init(new java.security.spec.DSAParameterSpec(pubSpec.getP(), pubSpec.getQ(), pubSpec.getG()));
        byte[] enc = ap.getEncoded();
        check(enc != null && enc.length > 0, "facade getEncoded() non-null ASN.1");
        java.security.AlgorithmParameters ap2 = java.security.AlgorithmParameters.getInstance("DSA");
        ap2.init(enc);
        java.security.spec.DSAParameterSpec back = ap2.getParameterSpec(java.security.spec.DSAParameterSpec.class);
        check(back.getP().equals(pubSpec.getP()) && back.getQ().equals(pubSpec.getQ()) && back.getG().equals(pubSpec.getG()),
                "facade encoded round-trip restores p/q/g");
        check(ap.toString().contains("p:") && ap.toString().contains("q:") && ap.toString().contains("g:"),
                "facade toString lists p/q/g sections");

        // ---- facade state machine: uninitialized AlgorithmParameters ----
        java.security.AlgorithmParameters uninit = java.security.AlgorithmParameters.getInstance("DSA");
        ct(java.io.IOException.class, () -> uninit.getEncoded(), "facade getEncoded before init -> IOException");
        ct(java.security.spec.InvalidParameterSpecException.class,
                () -> uninit.getParameterSpec(java.security.spec.DSAParameterSpec.class),
                "facade getParameterSpec before init -> InvalidParameterSpecException");
        check(uninit.toString() == null, "facade toString before init returns null (prints as \"null\")");
        ct(java.security.spec.InvalidParameterSpecException.class,
                () -> ap.init((java.security.spec.AlgorithmParameterSpec) null),
                "facade init(null spec) -> InvalidParameterSpecException");
    }



    static class MyApSpi extends AlgorithmParametersSpi {
        protected void engineInit(AlgorithmParameterSpec paramSpec) throws java.security.spec.InvalidParameterSpecException { }
        protected void engineInit(byte[] params) throws java.io.IOException { }
        protected void engineInit(byte[] params, String format) throws java.io.IOException { }
        protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec) throws java.security.spec.InvalidParameterSpecException { throw new java.security.spec.InvalidParameterSpecException(); }
        protected byte[] engineGetEncoded() throws java.io.IOException { return new byte[]{7}; }
        protected byte[] engineGetEncoded(String format) throws java.io.IOException { return new byte[]{7}; }
        protected String engineToString() { return "MyApSpi"; }
        void doInit(AlgorithmParameterSpec ps) throws java.security.spec.InvalidParameterSpecException { engineInit(ps); }
        void doInit(byte[] b) throws java.io.IOException { engineInit(b); }
        void doInit(byte[] b, String f) throws java.io.IOException { engineInit(b, f); }
        byte[] doGetEncoded() throws java.io.IOException { return engineGetEncoded(); }
        byte[] doGetEncoded(String f) throws java.io.IOException { return engineGetEncoded(f); }
        String doToString() { return engineToString(); }
    }

    static class DummySpec implements AlgorithmParameterSpec { }
}
