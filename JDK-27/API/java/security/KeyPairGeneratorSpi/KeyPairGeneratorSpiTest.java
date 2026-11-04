/*
 * @test
 * @summary java.security.KeyPairGeneratorSpi conformance: white-box structure + black-box behavior.
 * @run main KeyPairGeneratorSpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class KeyPairGeneratorSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyPairGeneratorSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"generateKeyPair", "initialize"});
    }

    static void testBlackBox() throws Exception {
        MyKpgSpi g = new MyKpgSpi();
        g.doInitialize(1024, null);
        KeyPair kp = g.doGenerateKeyPair();
        check(kp != null, "generateKeyPair non-null");
        check(kp.getPublic() != null, "public key non-null");
        check(kp.getPrivate() != null, "private key non-null");
        check("Dummy".equals(kp.getPublic().getAlgorithm()), "public algorithm");
        check("Dummy".equals(kp.getPublic().getFormat()), "public format");
        check(Arrays.equals(kp.getPublic().getEncoded(), new byte[]{1, 2, 3}), "public encoded");
        check("Dummy".equals(kp.getPrivate().getAlgorithm()), "private algorithm");
        checkThrows(UnsupportedOperationException.class, () -> g.doInitialize(new DummySpec(), null));
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



    static class MyKpgSpi extends KeyPairGeneratorSpi {
        public void initialize(int keysize, SecureRandom random) { }
        public KeyPair generateKeyPair() { return new KeyPair(new DummyPublicKey(), new DummyPrivateKey()); }
        void doInitialize(int keysize, SecureRandom random) { initialize(keysize, random); }
        void doInitialize(AlgorithmParameterSpec p, SecureRandom r) {
            try { initialize(p, r); } catch (java.security.InvalidAlgorithmParameterException e) { throw new RuntimeException(e); }
        }
        KeyPair doGenerateKeyPair() { return generateKeyPair(); }
    }

    static class DummyPublicKey implements PublicKey {
        public String getAlgorithm() { return "Dummy"; }
        public String getFormat() { return "Dummy"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
    }

    static class DummyPrivateKey implements PrivateKey {
        public String getAlgorithm() { return "Dummy"; }
        public String getFormat() { return "Dummy"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
    }

    static class DummySpec implements AlgorithmParameterSpec { }

    static void checkThrows(Class<? extends Throwable> type, Runnable r) {
        try { r.run(); } catch (Throwable t) { check(type.isInstance(t), "throws " + type.getSimpleName()); return; }
        throw new AssertionError("expected " + type.getSimpleName());
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
