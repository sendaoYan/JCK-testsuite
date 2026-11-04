/*
 * @test
 * @summary javax.crypto.KeyGeneratorSpi conformance: white-box structure + black-box behavior.
 * @run main KeyGeneratorSpiTest
 */

import java.lang.reflect.*;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGenerator;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyGeneratorSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KeyGeneratorSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getInterfaces().length == 0, "implements no interfaces");
        boolean pubCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 0) pubCtor = true;
        check(pubCtor, "public no-arg constructor");
        // all four SPI methods are protected abstract with exact signatures
        Method mInitSR = c.getDeclaredMethod("engineInit", SecureRandom.class);
        check(Modifier.isProtected(mInitSR.getModifiers()) && Modifier.isAbstract(mInitSR.getModifiers()),
                "protected abstract engineInit(SecureRandom)");
        Method mInitSpec = c.getDeclaredMethod("engineInit", AlgorithmParameterSpec.class, SecureRandom.class);
        check(Modifier.isProtected(mInitSpec.getModifiers()) && Modifier.isAbstract(mInitSpec.getModifiers()),
                "protected abstract engineInit(AlgorithmParameterSpec, SecureRandom)");
        Method mInitInt = c.getDeclaredMethod("engineInit", int.class, SecureRandom.class);
        check(Modifier.isProtected(mInitInt.getModifiers()) && Modifier.isAbstract(mInitInt.getModifiers()),
                "protected abstract engineInit(int, SecureRandom)");
        Method mGen = c.getDeclaredMethod("engineGenerateKey");
        check(Modifier.isProtected(mGen.getModifiers()) && Modifier.isAbstract(mGen.getModifiers()),
                "protected abstract engineGenerateKey");
        check(mGen.getReturnType().getName().equals("javax.crypto.SecretKey"),
                "engineGenerateKey returns SecretKey");
        check(mGen.getParameterCount() == 0, "engineGenerateKey takes no arguments");
        check(c.getDeclaredMethods().length == 4, "exactly 4 declared SPI methods");
    }

    // ---- Part 2: black-box (behavioral) ----

    // minimal concrete subclass producing deterministic fixed keys
    static class FixedKeySpi extends KeyGeneratorSpi {
        int keySizeBits = 128;
        SecureRandom random;
        int specInits, intInits, srInits, gens;
        AlgorithmParameterSpec lastSpec;

        // public adapters so the test can exercise the protected engine methods directly
        public void adaptInitSR(SecureRandom r) { engineInit(r); }
        public void adaptInitSpec(AlgorithmParameterSpec s, SecureRandom r) throws java.security.InvalidAlgorithmParameterException { engineInit(s, r); }
        public void adaptInitInt(int size, SecureRandom r) { engineInit(size, r); }
        public SecretKey adaptGenerate() { return engineGenerateKey(); }

        protected void engineInit(SecureRandom random) { srInits++; this.random = random; }
        protected void engineInit(AlgorithmParameterSpec spec, SecureRandom random)
                throws java.security.InvalidAlgorithmParameterException {
            if (!(spec instanceof javax.crypto.spec.IvParameterSpec))
                throw new java.security.InvalidAlgorithmParameterException("unsupported spec");
            specInits++; this.random = random; this.lastSpec = spec;
        }
        protected void engineInit(int keysize, SecureRandom random) {
            intInits++; this.keySizeBits = keysize; this.random = random;
        }
        protected SecretKey engineGenerateKey() {
            gens++;
            byte[] b = new byte[keySizeBits / 8];
            for (int i = 0; i < b.length; i++) b[i] = (byte) (0xF0 + i);   // deterministic
            return new SecretKeySpec(b, "MOCKKG");
        }
    }

    // engine wrapper built through the protected KeyGenerator constructor
    static class WrapperKG extends KeyGenerator {
        WrapperKG(KeyGeneratorSpi spi, Provider p, String a) { super(spi, p, a); }
    }

    static void testBlackBox() throws Exception {
        FixedKeySpi spi = new FixedKeySpi();

        // direct invocation of each implemented engine method
        SecureRandom sr = new SecureRandom(new byte[]{1, 2, 3});
        spi.adaptInitSR(sr);
        check(spi.srInits == 1 && spi.random == sr, "engineInit(SecureRandom) receives the random source");
        spi.adaptInitInt(256, sr);
        check(spi.intInits == 1 && spi.keySizeBits == 256, "engineInit(int, SecureRandom) captures key size");
        SecretKey k = spi.adaptGenerate();
        check(spi.gens == 1 && k.getEncoded().length == 32, "engineGenerateKey honors last engineInit size");
        check("MOCKKG".equals(k.getAlgorithm()), "engineGenerateKey returns our fixed algorithm");
        byte[] again = spi.adaptGenerate().getEncoded();
        check(java.util.Arrays.equals(k.getEncoded(), again), "SPI key generation is deterministic per our implementation");
        AlgorithmParameterSpec spec = new javax.crypto.spec.IvParameterSpec(new byte[16]);
        spi.adaptInitSpec(spec, sr);
        check(spi.specInits == 1 && spi.lastSpec == spec, "engineInit(AlgorithmParameterSpec, SecureRandom) captures spec");
        // our engineInit may reject a spec with its declared InvalidAlgorithmParameterException
        javax.crypto.spec.GCMParameterSpec bad = new javax.crypto.spec.GCMParameterSpec(128, new byte[12]);
        try {
            spi.adaptInitSpec(bad, sr);
            check(false, "engineInit(spec,...) must be able to throw InvalidAlgorithmParameterException");
        } catch (java.security.InvalidAlgorithmParameterException e) {
            check(true, "engineInit(spec,...) declared throws contract exercised");
        }
        check(spi.specInits == 1, "rejected spec did not count as an accepted init");

        // full dispatch through the KeyGenerator engine wrapper (protected ctor)
        FixedKeySpi wrapperSpi = new FixedKeySpi();
        Provider p = Security.getProvider("SunJCE");
        WrapperKG kg = new WrapperKG(wrapperSpi, p, "MOCKKG");
        check("MOCKKG".equals(kg.getAlgorithm()), "wrapper reports constructor algorithm");
        check(kg.getProvider() == p, "wrapper reports constructor provider");
        kg.init(256);                                     // -> engineInit(256, non-null random)
        check(wrapperSpi.intInits == 1 && wrapperSpi.keySizeBits == 256,
                "KeyGenerator.init(int) reaches engineInit(int, SecureRandom)");
        check(wrapperSpi.random != null, "wrapper supplies a non-null SecureRandom when none given");
        SecretKey wrapped = kg.generateKey();
        check(wrapperSpi.gens == 1, "KeyGenerator.generateKey() reaches engineGenerateKey");
        check(wrapped.getEncoded().length == 32 && "MOCKKG".equals(wrapped.getAlgorithm()),
                "wrapper returns the SPI-produced key");
        kg.init(sr);
        check(wrapperSpi.srInits == 1 && wrapperSpi.random == sr,
                "KeyGenerator.init(SecureRandom) reaches engineInit(SecureRandom)");
        SecretKey afterSR = kg.generateKey();
        check(wrapperSpi.gens == 2 && afterSR.getEncoded().length == 32,
                "generateKey after init(SecureRandom) uses the SPI default size");
        kg.init(spec, sr);
        check(wrapperSpi.specInits == 1 && wrapperSpi.lastSpec == spec,
                "KeyGenerator.init(AlgorithmParameterSpec, SecureRandom) reaches engineInit(spec, ...)");
        try {
            kg.init(new javax.crypto.spec.GCMParameterSpec(128, new byte[12]), sr);
            check(false, "wrapper must surface SPI InvalidAlgorithmParameterException");
        } catch (java.security.InvalidAlgorithmParameterException e) {
            check(true, "KeyGenerator.init(spec,...) propagates SPI InvalidAlgorithmParameterException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
