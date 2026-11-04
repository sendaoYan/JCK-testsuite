/*
 * @test
 * @summary javax.crypto.SecretKeyFactorySpi conformance: white-box structure + black-box behavior.
 * @run main SecretKeyFactorySpiTest
 */

import java.lang.reflect.*;
import java.security.Provider;
import java.security.Security;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKeyFactorySpi;
import javax.crypto.spec.SecretKeySpec;

public class SecretKeyFactorySpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.SecretKeyFactorySpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getInterfaces().length == 0, "implements no interfaces");
        boolean pubCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 0) pubCtor = true;
        check(pubCtor, "public no-arg constructor");
        // the three SPI methods: protected abstract with exact signatures
        Method gen = c.getDeclaredMethod("engineGenerateSecret", KeySpec.class);
        check(Modifier.isProtected(gen.getModifiers()) && Modifier.isAbstract(gen.getModifiers()),
                "protected abstract engineGenerateSecret(KeySpec)");
        check(gen.getReturnType().getName().equals("javax.crypto.SecretKey"),
                "engineGenerateSecret returns SecretKey");
        Method spec = c.getDeclaredMethod("engineGetKeySpec", SecretKey.class, Class.class);
        check(Modifier.isProtected(spec.getModifiers()) && Modifier.isAbstract(spec.getModifiers()),
                "protected abstract engineGetKeySpec(SecretKey, Class)");
        check(java.security.spec.KeySpec.class.isAssignableFrom(spec.getReturnType()),
                "engineGetKeySpec returns KeySpec");
        Method tr = c.getDeclaredMethod("engineTranslateKey", SecretKey.class);
        check(Modifier.isProtected(tr.getModifiers()) && Modifier.isAbstract(tr.getModifiers()),
                "protected abstract engineTranslateKey(SecretKey)");
        check(tr.getReturnType().getName().equals("javax.crypto.SecretKey"),
                "engineTranslateKey returns SecretKey");
        check(c.getDeclaredMethods().length == 3, "exactly 3 declared SPI methods");
        // declared exceptions of the SPI contract
        boolean genThrows = false;
        for (Class<?> ex : gen.getExceptionTypes())
            if (ex.getName().equals("java.security.spec.InvalidKeySpecException")) genThrows = true;
        check(genThrows, "engineGenerateSecret declares InvalidKeySpecException");
        boolean trThrows = false;
        for (Class<?> ex : tr.getExceptionTypes())
            if (ex.getName().equals("java.security.InvalidKeyException")) trThrows = true;
        check(trThrows, "engineTranslateKey declares InvalidKeyException");
    }

    // ---- Part 2: black-box (behavioral) ----

    // minimal concrete subclass implementing the full SPI surface
    static class FixedSpi extends SecretKeyFactorySpi {
        int gens, specs, translates;
        KeySpec lastGenSpec;
        SecretKey lastSpecKey;
        Class<?> lastSpecClass;
        SecretKey lastTranslated;

        // public adapters so the test can drive the protected engine methods directly
        public SecretKey adaptGenerate(KeySpec ks) throws java.security.spec.InvalidKeySpecException {
            return engineGenerateSecret(ks);
        }
        public KeySpec adaptGetSpec(SecretKey k, Class<?> c)
                throws java.security.spec.InvalidKeySpecException {
            return engineGetKeySpec(k, c);
        }
        public SecretKey adaptTranslate(SecretKey k) throws java.security.InvalidKeyException {
            return engineTranslateKey(k);
        }

        protected SecretKey engineGenerateSecret(KeySpec spec)
                throws java.security.spec.InvalidKeySpecException {
            if (!(spec instanceof SecretKeySpec))
                throw new java.security.spec.InvalidKeySpecException("unsupported spec");
            gens++;
            lastGenSpec = spec;
            SecretKeySpec in = (SecretKeySpec) spec;
            return new SecretKeySpec(in.getEncoded(), "MOCK");       // fresh key, same material
        }
        protected KeySpec engineGetKeySpec(SecretKey key, Class<?> specClass)
                throws java.security.spec.InvalidKeySpecException {
            if (specClass != SecretKeySpec.class)
                throw new java.security.spec.InvalidKeySpecException("unsupported spec class");
            specs++;
            lastSpecKey = key; lastSpecClass = specClass;
            return new SecretKeySpec(key.getEncoded(), key.getAlgorithm());
        }
        protected SecretKey engineTranslateKey(SecretKey key) throws java.security.InvalidKeyException {
            if (!"MOCK".equals(key.getAlgorithm()))
                throw new java.security.InvalidKeyException("foreign key");
            translates++;
            lastTranslated = key;
            return key;                                             // identity translation
        }
    }

    // engine wrapper built through the protected SecretKeyFactory constructor
    static class WrapperSKF extends SecretKeyFactory {
        WrapperSKF(SecretKeyFactorySpi spi, Provider p, String a) { super(spi, p, a); }
    }

    static void testBlackBox() throws Exception {
        FixedSpi spi = new FixedSpi();

        // direct invocation of each implemented engine method
        SecretKeySpec input = new SecretKeySpec(new byte[16], "MOCK");
        SecretKey generated = spi.adaptGenerate(input);
        check(spi.gens == 1 && spi.lastGenSpec == input, "engineGenerateSecret receives the KeySpec");
        check("MOCK".equals(generated.getAlgorithm())
                && java.util.Arrays.equals(generated.getEncoded(), input.getEncoded()),
                "engineGenerateSecret returns our key with the spec material");
        KeySpec outSpec = spi.adaptGetSpec(generated, SecretKeySpec.class);
        check(spi.specs == 1 && spi.lastSpecKey == generated,
                "engineGetKeySpec receives key and target class");
        check(outSpec instanceof SecretKeySpec
                && java.util.Arrays.equals(((SecretKeySpec) outSpec).getEncoded(), input.getEncoded()),
                "engineGetKeySpec returns our spec with the key material");
        SecretKey translated = spi.adaptTranslate(generated);
        check(spi.translates == 1 && translated == generated,
                "engineTranslateKey receives the key and returns our result");

        // our implementation rejects bad input with its declared exceptions
        try {
            spi.adaptGenerate(new javax.crypto.spec.DESedeKeySpec(new byte[24]));
            check(false, "engineGenerateSecret must be able to throw InvalidKeySpecException");
        } catch (java.security.spec.InvalidKeySpecException e) {
            check(true, "engineGenerateSecret InvalidKeySpecException contract exercised");
        }
        try {
            spi.adaptTranslate(new SecretKeySpec(new byte[16], "Foreign"));
            check(false, "engineTranslateKey must be able to throw InvalidKeyException");
        } catch (java.security.InvalidKeyException e) {
            check(true, "engineTranslateKey InvalidKeyException contract exercised");
        }
        try {
            spi.adaptGetSpec(generated, javax.crypto.spec.DESedeKeySpec.class);
            check(false, "engineGetKeySpec must be able to throw InvalidKeySpecException");
        } catch (java.security.spec.InvalidKeySpecException e) {
            check(true, "engineGetKeySpec InvalidKeySpecException contract exercised");
        }

        // full dispatch through the SecretKeyFactory engine wrapper (protected ctor)
        FixedSpi wrapperSpi = new FixedSpi();
        Provider p = Security.getProvider("SunJCE");
        WrapperSKF skf = new WrapperSKF(wrapperSpi, p, "MOCKSKF");
        check("MOCKSKF".equals(skf.getAlgorithm()), "wrapper reports constructor algorithm");
        check(skf.getProvider() == p, "wrapper reports constructor provider");
        SecretKey wKey = skf.generateSecret(input);
        check(wrapperSpi.gens == 1, "SecretKeyFactory.generateSecret() reaches engineGenerateSecret");
        check("MOCK".equals(wKey.getAlgorithm()), "wrapper returns the SPI-produced key");
        KeySpec wSpec = skf.getKeySpec(wKey, SecretKeySpec.class);
        check(wrapperSpi.specs == 1, "SecretKeyFactory.getKeySpec() reaches engineGetKeySpec");
        check(java.util.Arrays.equals(((SecretKeySpec) wSpec).getEncoded(), input.getEncoded()),
                "wrapper returns the SPI-produced spec");
        SecretKey wTr = skf.translateKey(wKey);
        check(wrapperSpi.translates == 1 && wTr == wKey,
                "SecretKeyFactory.translateKey() reaches engineTranslateKey");
        try {
            skf.translateKey(new SecretKeySpec(new byte[16], "Foreign"));
            check(false, "wrapper must surface SPI InvalidKeyException");
        } catch (java.security.InvalidKeyException e) {
            check(true, "SecretKeyFactory.translateKey propagates SPI InvalidKeyException");
        }
        try {
            skf.generateSecret(new javax.crypto.spec.DESedeKeySpec(new byte[24]));
            check(false, "wrapper must surface SPI InvalidKeySpecException");
        } catch (java.security.spec.InvalidKeySpecException e) {
            check(true, "SecretKeyFactory.generateSecret propagates SPI InvalidKeySpecException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
