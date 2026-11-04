/*
 * @test
 * @summary java.security.KeyPairGeneratorSpi conformance: abstract class hierarchy and key methods.
 * @run main KeyPairGeneratorSpiTest
 */

public class KeyPairGeneratorSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    // Unlike the other engine SPIs, KeyPairGeneratorSpi's methods are public (the facade
    // subclass sits in the same type hierarchy), and initialize(spec, random) has a default.
    static class RecSpi extends java.security.KeyPairGeneratorSpi {
        int lastSize = -1; java.security.SecureRandom lastRandom; boolean randomWasNull = true;
        java.security.spec.AlgorithmParameterSpec lastSpec;
        int generated;
        java.security.KeyPair result = new java.security.KeyPair(null, null);
        public void initialize(int keysize, java.security.SecureRandom random) { lastSize = keysize; lastRandom = random; }
        public java.security.KeyPair generateKeyPair() { generated++; return result; }
        void doInitSpec(java.security.spec.AlgorithmParameterSpec spec, java.security.SecureRandom random)
                throws java.security.InvalidAlgorithmParameterException { initialize(spec, random); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.KeyPairGeneratorSpi.class;

        // ---- structure ----
        check(c.getConstructor() != null, "public no-arg constructor");
        java.lang.reflect.Method m;
        m = c.getDeclaredMethod("initialize", int.class, java.security.SecureRandom.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isPublic(m.getModifiers()),
                "initialize(int,SecureRandom) abstract+public (not protected)");
        m = c.getDeclaredMethod("generateKeyPair");
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isPublic(m.getModifiers()),
                "generateKeyPair abstract+public");
        m = c.getDeclaredMethod("initialize", java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isPublic(m.getModifiers()),
                "initialize(spec,random) public with default impl");
        check(m.getExceptionTypes().length == 1
                && m.getExceptionTypes()[0] == java.security.InvalidAlgorithmParameterException.class,
                "initialize(spec,random) declares InvalidAlgorithmParameterException");

        // ---- pass-through + default initialize(spec, random) ----
        RecSpi spi = new RecSpi();
        java.security.SecureRandom rnd = new java.security.SecureRandom(new byte[]{2});
        spi.initialize(2048, rnd);
        check(spi.lastSize == 2048 && spi.lastRandom == rnd, "initialize(int,SecureRandom) pass-through");
        spi.initialize(512, null);
        check(spi.lastSize == 512 && spi.lastRandom == null, "initialize(int,null) accepted, random recorded as null");
        java.security.spec.RSAKeyGenParameterSpec spec =
                new java.security.spec.RSAKeyGenParameterSpec(1024, java.security.spec.RSAKeyGenParameterSpec.F4);
        ct(UnsupportedOperationException.class, () -> spi.doInitSpec(spec, rnd),
                "default initialize(spec,random) throws UnsupportedOperationException (not IAPE)");
        check(spi.generated == 0, "no generation before generateKeyPair");
        check(spi.generateKeyPair() == spi.result, "generateKeyPair returns subclass value");
        check(spi.generated == 1, "generateKeyPair pass-through count");

        // ---- facade closed loop: KeyPairGenerator drives a real SPI (DSA) ----
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("DSA");
        check(kpg instanceof java.security.KeyPairGeneratorSpi, "facade KeyPairGenerator extends KeyPairGeneratorSpi");
        kpg.initialize(1024, null);
        java.security.KeyPair kp = kpg.generateKeyPair();
        check(kp.getPublic() != null && kp.getPrivate() != null, "facade generated key pair");
        check(kp.getPublic().getAlgorithm().equals("DSA") && kp.getPrivate().getAlgorithm().equals("DSA"),
                "facade pair algorithm DSA");
        check("X.509".equals(kp.getPublic().getFormat()) && "PKCS#8".equals(kp.getPrivate().getFormat()),
                "facade pair encodings X.509 / PKCS#8");
        java.security.KeyPair kp2 = kpg.generateKeyPair();
        check(!kp2.getPrivate().equals(kp.getPrivate()), "facade second generation yields a different key");
        ct(java.security.InvalidParameterException.class, () -> kpg.initialize(-1), "facade initialize(-1) -> InvalidParameterException");
        ct(java.security.InvalidParameterException.class, () -> kpg.initialize(64), "facade initialize(64) too small -> InvalidParameterException");
        java.security.spec.DSAParameterSpec bogus =
                new java.security.spec.DSAParameterSpec(java.math.BigInteger.ONE, java.math.BigInteger.ONE, java.math.BigInteger.ONE);
        ct(java.security.InvalidParameterException.class, () -> kpg.initialize(bogus, null),
                "facade initialize(1-bit DSA params) -> InvalidParameterException (strength checked before values)");
        ct(java.security.InvalidAlgorithmParameterException.class,
                () -> kpg.initialize(new java.security.spec.RSAKeyGenParameterSpec(512, java.security.spec.RSAKeyGenParameterSpec.F4), null),
                "facade initialize(RSA spec on DSA generator) -> InvalidAlgorithmParameterException");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyPairGeneratorSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("generateKeyPair"), "declares generateKeyPair");
        check(ms.contains("initialize"), "declares initialize");

        // ---- contract deepening: default init(spec) UOE + facade DSA loop ----
        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
