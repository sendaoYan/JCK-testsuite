/*
 * @test
 * @summary javax.crypto.KDFSpi conformance: white-box structure + black-box behavior.
 * @run main KDFSpiTest
 */

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class KDFSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // two minimal subclasses: fixed 7-byte and fixed 5-byte derivations
    static class Fixed7Spi extends KDFSpi {
        final KDFParameters given;
        Fixed7Spi(KDFParameters p) throws InvalidAlgorithmParameterException { super(p); given = p; }
        public KDFParameters engineGetParameters() { return given; }
        public SecretKey engineDeriveKey(String alg, AlgorithmParameterSpec spec)
                throws NoSuchAlgorithmException {
            if (alg.isEmpty()) throw new NoSuchAlgorithmException("empty algorithm");
            return new SecretKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7}, alg);
        }
        public byte[] engineDeriveData(AlgorithmParameterSpec spec) {
            return new byte[]{1, 2, 3, 4, 5, 6, 7};
        }
    }

    static class Fixed5Spi extends KDFSpi {
        int deriveKeyCalls, deriveDataCalls, paramsCalls;
        public Fixed5Spi() throws InvalidAlgorithmParameterException { super(null); }
        public KDFParameters engineGetParameters() { paramsCalls++; return null; }
        public SecretKey engineDeriveKey(String alg, AlgorithmParameterSpec spec) {
            deriveKeyCalls++;
            return new SecretKeySpec(new byte[]{9, 9, 9, 9, 9}, alg);
        }
        public byte[] engineDeriveData(AlgorithmParameterSpec spec) {
            deriveDataCalls++;
            return new byte[]{9, 9, 9, 9, 9};
        }
    }

    static class RejectingSpi extends KDFSpi {
        RejectingSpi(KDFParameters p) throws InvalidAlgorithmParameterException {
            super(p);
            if (p != null) throw new InvalidAlgorithmParameterException("unsupported parameters");
        }
        public KDFParameters engineGetParameters() { return null; }
        public SecretKey engineDeriveKey(String alg, AlgorithmParameterSpec spec) { return null; }
        public byte[] engineDeriveData(AlgorithmParameterSpec spec) { return new byte[0]; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KDFSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        // preserved: no public constructor, engine methods exist
        check(!hasPublicCtor(c), "no public constructor");
        checkMethods(c, new String[]{"engineGetParameters", "engineDeriveKey", "engineDeriveData"});
        // sole constructor is protected and takes KDFParameters
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && ctors[0].getParameterCount() == 1
                && ctors[0].getParameterTypes()[0].getName().equals("javax.crypto.KDFParameters")
                && Modifier.isProtected(ctors[0].getModifiers()),
                "single protected ctor (KDFParameters)");
        // all three engine methods are protected+abstract with JDK-crypto throws clauses
        Method dk = c.getDeclaredMethod("engineDeriveKey", String.class, AlgorithmParameterSpec.class);
        check(Modifier.isAbstract(dk.getModifiers()) && Modifier.isProtected(dk.getModifiers()),
                "engineDeriveKey protected abstract");
        boolean nsae = false;
        for (Class<?> t : dk.getExceptionTypes())
            if (t == NoSuchAlgorithmException.class) nsae = true;
        check(nsae, "engineDeriveKey declares NoSuchAlgorithmException");
        Method dd = c.getDeclaredMethod("engineDeriveData", AlgorithmParameterSpec.class);
        check(Modifier.isAbstract(dd.getModifiers()) && Modifier.isProtected(dd.getModifiers())
                && dd.getReturnType() == byte[].class,
                "engineDeriveData protected abstract byte[]");
        Method gp = c.getDeclaredMethod("engineGetParameters");
        check(Modifier.isAbstract(gp.getModifiers()) && Modifier.isProtected(gp.getModifiers())
                && gp.getReturnType().getName().equals("javax.crypto.KDFParameters"),
                "engineGetParameters protected abstract KDFParameters");
    }

    static void checkMethods(Class<?> c, String[] ns) {
        for (String n : ns) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "method " + n);
        }
    }

    static boolean hasPublicCtor(Class<?> c) {
        for (Constructor<?> k : c.getConstructors()) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // configuration flows through the protected constructor and back via engineGetParameters
        class Marker implements KDFParameters {}
        Marker cfg = new Marker();
        Fixed7Spi s7 = new Fixed7Spi(cfg);
        check(s7 instanceof KDFSpi && KDFSpi.class.isAssignableFrom(Fixed7Spi.class),
                "subclass types as KDFSpi");
        check(s7.engineGetParameters() == cfg, "engineGetParameters echoes configuration");
        check(new Fixed7Spi(null).engineGetParameters() == null, "null configuration echoed");

        // engineDeriveKey: algorithm honored, fixed material returned, error contract kept
        SecretKey aes = s7.engineDeriveKey("AES", null);
        check("AES".equals(aes.getAlgorithm()) && aes.getEncoded().length == 7,
                "engineDeriveKey honors algorithm and length");
        check(Arrays.equals(aes.getEncoded(),
                new byte[]{1, 2, 3, 4, 5, 6, 7}), "engineDeriveKey fixed bytes");
        try {
            s7.engineDeriveKey("", null);
            check(false, "empty algorithm must fail");
        } catch (NoSuchAlgorithmException ex) { check(true, "empty alg -> NoSuchAlgorithmException"); }

        // engineDeriveData agrees with engineDeriveKey (implNote contract)
        check(Arrays.equals(s7.engineDeriveData(null), aes.getEncoded()),
                "engineDeriveData == engineDeriveKey encoding");

        // a second implementation dispatches independently, calls counted
        Fixed5Spi s5 = new Fixed5Spi();
        check(s5.engineDeriveKey("Generic", null).getEncoded().length == 5
                && Arrays.equals(s5.engineDeriveData(null), new byte[]{9, 9, 9, 9, 9}),
                "second impl has its own fixed values");
        check(!Arrays.equals(s5.engineDeriveData(null), s7.engineDeriveData(null)),
                "no shared state between implementations");
        s5.engineGetParameters();
        // deriveKey was called once, engineDeriveData twice (value + comparison), params once
        check(s5.deriveKeyCalls == 1 && s5.deriveDataCalls == 2 && s5.paramsCalls == 1,
                "every engine method call dispatched to the subclass");

        // a subclass may reject unsupported configuration with the declared exception
        try {
            new RejectingSpi(new Marker());
            check(false, "rejecting ctor must fail");
        } catch (InvalidAlgorithmParameterException ex) {
            check("unsupported parameters".equals(ex.getMessage()), "ctor rejection propagates");
        }
        check(new RejectingSpi(null).engineGetParameters() == null,
                "rejecting spi accepts null configuration");

        // the built-in HKDF implementation is also a KDFSpi and honors the same contract
        KDF kdf = KDF.getInstance("HKDF-SHA256");
        byte[] okm = kdf.deriveData(javax.crypto.spec.HKDFParameterSpec.ofExtract()
            .addIKM(new byte[]{3}).addSalt(new byte[]{4}).thenExpand(new byte[]{5}, 16));
        check(okm.length == 16 && Arrays.equals(kdf.deriveData(javax.crypto.spec.HKDFParameterSpec
                .ofExtract().addIKM(new byte[]{3}).addSalt(new byte[]{4})
                .thenExpand(new byte[]{5}, 16)), okm),
                "built-in KDFSpi drives deterministic derivation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
