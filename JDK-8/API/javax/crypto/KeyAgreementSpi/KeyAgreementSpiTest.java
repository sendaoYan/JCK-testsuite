/*
 * @test
 * @summary javax.crypto.KeyAgreementSpi conformance: white-box structure + black-box behavior.
 * @run main KeyAgreementSpiTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public class KeyAgreementSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // deterministic recording SPI: remembers what the engine was asked to do
    static class RecordingSpi extends KeyAgreementSpi {
        Key recKey; SecureRandom recRandom; AlgorithmParameterSpec recSpec;
        Key phaseKey; boolean recLastPhase; Key phaseResult = null;
        boolean failDoPhase; int generateCount;
        static final byte[] SECRET = {9, 8, 7};

        @Override
        protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException { recKey = key; recRandom = random; recSpec = null; }
        @Override
        protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random)
                throws InvalidKeyException, java.security.InvalidAlgorithmParameterException { recKey = key; recRandom = random; recSpec = params; }
        @Override
        protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException {
            if (failDoPhase) throw new IllegalStateException("spi: not initialized");
            phaseKey = key; recLastPhase = lastPhase; return phaseResult;
        }
        @Override
        protected byte[] engineGenerateSecret() { generateCount++; return SECRET.clone(); }
        @Override
        protected int engineGenerateSecret(byte[] sharedSecret, int offset) throws ShortBufferException {
            if (sharedSecret.length - offset < SECRET.length) throw new ShortBufferException("spi: need 3 bytes");
            System.arraycopy(SECRET, 0, sharedSecret, offset, SECRET.length);
            return SECRET.length;
        }
        @Override
        protected SecretKey engineGenerateSecret(String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
            if ("NoSuchSecretAlg".equals(algorithm)) throw new NoSuchAlgorithmException("spi: unknown " + algorithm);
            if (algorithm == null) throw new NoSuchAlgorithmException("spi: null algorithm");
            return new SecretKeySpec(SECRET, algorithm);
        }

        // public bridges so the protected engine contract can be invoked directly
        void bridgeInit(Key k, SecureRandom r) throws InvalidKeyException { engineInit(k, r); }
        void bridgeInit(Key k, AlgorithmParameterSpec p, SecureRandom r)
                throws InvalidKeyException, java.security.InvalidAlgorithmParameterException { engineInit(k, p, r); }
        Key bridgeDoPhase(Key k, boolean last) throws InvalidKeyException { return engineDoPhase(k, last); }
        byte[] bridgeGenerate() { return engineGenerateSecret(); }
        int bridgeGenerate(byte[] b, int off) throws ShortBufferException { return engineGenerateSecret(b, off); }
        SecretKey bridgeGenerate(String alg) throws NoSuchAlgorithmException, InvalidKeyException { return engineGenerateSecret(alg); }
    }

    // wrapper built through the protected KeyAgreement constructor, delegating to RecordingSpi
    static class WrapperKA extends KeyAgreement {
        WrapperKA(KeyAgreementSpi spi, Provider p, String alg) { super(spi, p, alg); }
    }

    interface Thunk { void run() throws Exception; }
    static void expect(Class<? extends Throwable> t, Thunk r) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError("expected " + t.getName() + " but got " + e, e);
        }
        throw new AssertionError("expected " + t.getName() + " but nothing was thrown");
    }

    static boolean hasAbstractMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getDeclaredMethod(name, params);
            return Modifier.isAbstract(m.getModifiers());
        } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KeyAgreementSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct superclass java.lang.Object");
        boolean publicCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 0) publicCtor = true;
        check(publicCtor, "public no-arg constructor");
        check(hasAbstractMethod(c, "engineInit", Key.class, SecureRandom.class), "abstract engineInit(Key,SecureRandom)");
        check(hasAbstractMethod(c, "engineInit", Key.class, AlgorithmParameterSpec.class, SecureRandom.class),
                "abstract engineInit(Key,AlgorithmParameterSpec,SecureRandom)");
        check(hasAbstractMethod(c, "engineDoPhase", Key.class, boolean.class), "abstract engineDoPhase(Key,boolean)");
        check(hasAbstractMethod(c, "engineGenerateSecret"), "abstract engineGenerateSecret()");
        check(hasAbstractMethod(c, "engineGenerateSecret", byte[].class, int.class),
                "abstract engineGenerateSecret(byte[],int)");
        check(hasAbstractMethod(c, "engineGenerateSecret", String.class), "abstract engineGenerateSecret(String)");
        // the six engine methods are protected
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().startsWith("engine"))
                check(Modifier.isProtected(m.getModifiers()), m.getName() + " is protected");
        }
        // the abstract class cannot be instantiated directly
        expect(InstantiationException.class, () -> Class.forName("javax.crypto.KeyAgreementSpi")
                .getDeclaredConstructor().newInstance());
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Provider prov = java.security.Security.getProvider("SunJCE");
        RecordingSpi spi = new RecordingSpi();
        WrapperKA ka = new WrapperKA(spi, prov, "DummyAgreement");

        // wrapper attributes come from the protected constructor
        check("DummyAgreement".equals(ka.getAlgorithm()), "wrapper algorithm from ctor");
        check(ka.getProvider() == prov, "wrapper provider identity from ctor");

        // init(Key) delegates to engineInit(Key, SecureRandom) with a non-null default random
        Key k = new SecretKeySpec(new byte[16], "AES");
        ka.init(k);
        check(spi.recKey == k, "init(Key) forwards key to engineInit");
        check(spi.recRandom != null, "init(Key) supplies a default SecureRandom");
        check(spi.recSpec == null, "init(Key) uses the no-spec engineInit");

        // init(Key, SecureRandom) forwards the exact random
        SecureRandom fixed = new SecureRandom();
        ka.init(k, fixed);
        check(spi.recRandom == fixed, "init(Key,SecureRandom) forwards exact random");

        // init(Key, AlgorithmParameterSpec, SecureRandom) forwards the spec
        AlgorithmParameterSpec spec = new javax.crypto.spec.GCMParameterSpec(128, new byte[12]);
        ka.init(k, spec, fixed);
        check(spi.recSpec == spec, "init(Key,AlgorithmParameterSpec,SecureRandom) forwards spec");
        check(spi.recRandom == fixed, "init(Key,AlgorithmParameterSpec,SecureRandom) forwards random");

        // engineInit may reject keys; the exception passes through unchanged
        RecordingSpi reject = new RecordingSpi() {
            @Override
            protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
                throw new InvalidKeyException("spi: bad key");
            }
        };
        WrapperKA kaReject = new WrapperKA(reject, prov, "DummyAgreement");
        expect(InvalidKeyException.class, () -> kaReject.init(k));

        // doPhase forwards the key and the lastPhase flag, and returns the engine's result
        Key phaseKey = new SecretKeySpec(new byte[16], "AES");
        Key sentinel = new SecretKeySpec(new byte[4], "SENTINEL");
        spi.phaseResult = sentinel;
        Key r = ka.doPhase(phaseKey, true);
        check(r == sentinel, "doPhase returns engineDoPhase result");
        check(spi.phaseKey == phaseKey, "doPhase forwards phase key");
        check(spi.recLastPhase, "doPhase forwards lastPhase=true");
        ka.doPhase(phaseKey, false);
        check(!spi.recLastPhase, "doPhase forwards lastPhase=false");
        spi.failDoPhase = true;
        expect(IllegalStateException.class, () -> ka.doPhase(phaseKey, true));
        spi.failDoPhase = false;

        // generateSecret() delegates and returns the engine bytes verbatim
        byte[] s = ka.generateSecret();
        check(Arrays.equals(s, RecordingSpi.SECRET), "generateSecret() returns engineGenerateSecret() bytes");

        // generateSecret(byte[], int) delegates, honors the offset, and returns the length
        byte[] buf = new byte[8];
        int n = ka.generateSecret(buf, 2);
        check(n == 3, "generateSecret(byte[],int) returns engine length");
        check(buf[0] == 0 && buf[1] == 0 && buf[6] == 0 && buf[7] == 0, "bytes outside offset window untouched");
        check(buf[2] == 9 && buf[3] == 8 && buf[4] == 7, "engine bytes written at offset");
        expect(ShortBufferException.class, () -> ka.generateSecret(new byte[2], 0));

        // generateSecret(String) delegates; unsupported algorithm propagates
        SecretKey sk = ka.generateSecret("Generic");
        check("Generic".equals(sk.getAlgorithm()), "generateSecret(String) returns engine key algorithm");
        check(Arrays.equals(sk.getEncoded(), RecordingSpi.SECRET), "generateSecret(String) returns engine key bytes");
        expect(java.security.NoSuchAlgorithmException.class, () -> ka.generateSecret("NoSuchSecretAlg"));

        // direct engine invocation through the SPI subclass bridges
        RecordingSpi direct = new RecordingSpi();
        direct.bridgeInit(k, fixed);
        check(direct.recKey == k && direct.recRandom == fixed, "engineInit(Key,SecureRandom) records arguments");
        Key dr = direct.bridgeDoPhase(k, true);
        check(dr == null, "engineDoPhase default returns null");
        check(Arrays.equals(direct.bridgeGenerate(), RecordingSpi.SECRET), "engineGenerateSecret() bytes");
        byte[] dbuf = new byte[3];
        check(direct.bridgeGenerate(dbuf, 0) == 3 && dbuf[0] == 9, "engineGenerateSecret(byte[],int) fills buffer");
        try { direct.bridgeGenerate(new byte[1], 0); check(false, "expected ShortBufferException"); }
        catch (ShortBufferException e) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
