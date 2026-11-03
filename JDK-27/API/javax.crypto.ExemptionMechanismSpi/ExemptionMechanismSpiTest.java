/*
 * @test
 * @summary javax.crypto.ExemptionMechanismSpi conformance: white-box structure + black-box behavior.
 * @run main ExemptionMechanismSpiTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.ExemptionMechanismException;
import javax.crypto.ExemptionMechanismSpi;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

public class ExemptionMechanismSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

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

    // minimal deterministic implementation of the six abstract engine methods
    static class MinSpi extends ExemptionMechanismSpi {
        Key recKey; AlgorithmParameterSpec recSpec; AlgorithmParameters recParams;
        int lastInputLen = Integer.MIN_VALUE; int blobCalls; boolean failBlob;
        static final byte[] BLOB = {(byte) 0xAA, 0x55, 0x0F};

        @Override protected int engineGetOutputSize(int inputLen) { lastInputLen = inputLen; return inputLen * 2 + 1; }
        @Override protected void engineInit(Key key) { recKey = key; }
        @Override protected void engineInit(Key key, AlgorithmParameterSpec params) { recKey = key; recSpec = params; }
        @Override protected void engineInit(Key key, AlgorithmParameters params) { recKey = key; recParams = params; }
        @Override protected byte[] engineGenExemptionBlob() throws ExemptionMechanismException {
            if (failBlob) throw new ExemptionMechanismException("spi: blob failure");
            blobCalls++;
            return BLOB.clone();
        }
        @Override protected int engineGenExemptionBlob(byte[] output, int outputOffset) throws ShortBufferException {
            if (output.length - outputOffset < BLOB.length) throw new ShortBufferException("spi: need 3 bytes");
            System.arraycopy(BLOB, 0, output, outputOffset, BLOB.length);
            blobCalls++;
            return BLOB.length;
        }

        // public bridges to invoke the protected engine contract directly
        int bridgeSize(int n) { return engineGetOutputSize(n); }
        void bridgeInit(Key k) { engineInit(k); }
        void bridgeInit(Key k, AlgorithmParameterSpec p) { engineInit(k, p); }
        void bridgeInit(Key k, AlgorithmParameters p) { engineInit(k, p); }
        byte[] bridgeBlob() throws ExemptionMechanismException { return engineGenExemptionBlob(); }
        int bridgeBlob(byte[] b, int off) throws ShortBufferException { return engineGenExemptionBlob(b, off); }
    }

    static boolean hasAbstractMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getDeclaredMethod(name, params);
            return Modifier.isAbstract(m.getModifiers());
        } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.ExemptionMechanismSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct superclass java.lang.Object");
        boolean publicCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 0) publicCtor = true;
        check(publicCtor, "public no-arg constructor");
        check(hasAbstractMethod(c, "engineGetOutputSize", int.class), "abstract engineGetOutputSize(int)");
        check(hasAbstractMethod(c, "engineInit", Key.class), "abstract engineInit(Key)");
        check(hasAbstractMethod(c, "engineInit", Key.class, AlgorithmParameterSpec.class),
                "abstract engineInit(Key,AlgorithmParameterSpec)");
        check(hasAbstractMethod(c, "engineInit", Key.class, AlgorithmParameters.class),
                "abstract engineInit(Key,AlgorithmParameters)");
        check(hasAbstractMethod(c, "engineGenExemptionBlob"), "abstract engineGenExemptionBlob()");
        check(hasAbstractMethod(c, "engineGenExemptionBlob", byte[].class, int.class),
                "abstract engineGenExemptionBlob(byte[],int)");
        for (Method m : c.getDeclaredMethods())
            if (m.getName().startsWith("engine"))
                check(Modifier.isProtected(m.getModifiers()), m.getName() + " is protected");
        expect(InstantiationException.class, () -> Class.forName("javax.crypto.ExemptionMechanismSpi")
                .getDeclaredConstructor().newInstance());
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MinSpi spi = new MinSpi();
        Key key = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");

        // engineGetOutputSize: identity-style mapping recorded verbatim
        check(spi.bridgeSize(0) == 1, "engineGetOutputSize(0)");
        check(spi.bridgeSize(5) == 11, "engineGetOutputSize(5)");
        check(spi.lastInputLen == 5, "engineGetOutputSize receives the input length");

        // the three engineInit overloads each record their own argument shape
        spi.bridgeInit(key);
        check(spi.recKey == key && spi.recSpec == null && spi.recParams == null, "engineInit(Key)");
        AlgorithmParameterSpec spec = new IvParameterSpec(new byte[16]);
        spi.bridgeInit(key, spec);
        check(spi.recSpec == spec, "engineInit(Key,AlgorithmParameterSpec)");
        AlgorithmParameters ap = AlgorithmParameters.getInstance("AES");
        ap.init(spec);
        spi.bridgeInit(key, ap);
        check(spi.recParams == ap, "engineInit(Key,AlgorithmParameters)");

        // engineGenExemptionBlob() returns a fresh copy each call
        byte[] b1 = spi.bridgeBlob();
        byte[] b2 = spi.bridgeBlob();
        check(Arrays.equals(b1, MinSpi.BLOB) && Arrays.equals(b2, MinSpi.BLOB), "engineGenExemptionBlob() content");
        check(b1 != b2, "engineGenExemptionBlob() returns independent arrays");
        check(spi.blobCalls == 2, "engine blob actually invoked per call");

        // engineGenExemptionBlob(byte[], int) honors the offset and detects short buffers
        byte[] buf = new byte[6];
        check(spi.bridgeBlob(buf, 1) == 3, "engineGenExemptionBlob(byte[],int) returns 3");
        check(buf[0] == 0 && buf[1] == (byte) 0xAA && buf[3] == 0x0F && buf[4] == 0, "blob written at offset 1");
        expect(ShortBufferException.class, () -> spi.bridgeBlob(new byte[2], 0));
        expect(ShortBufferException.class, () -> spi.bridgeBlob(new byte[8], 6));

        // engine failures surface as the documented exception types
        spi.failBlob = true;
        expect(ExemptionMechanismException.class, () -> spi.bridgeBlob());
        spi.failBlob = false;

        // subclasses compose: an SPI written for a provider can be driven to completion
        spi.bridgeInit(key);
        check(Arrays.equals(spi.bridgeBlob(), MinSpi.BLOB), "init-then-generate composition");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
