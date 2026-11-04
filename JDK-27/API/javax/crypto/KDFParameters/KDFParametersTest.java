/*
 * @test
 * @summary javax.crypto.KDFParameters conformance: white-box structure + black-box behavior.
 * @run main KDFParametersTest
 */

import javax.crypto.*;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class KDFParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // a user-defined implementation of the marker interface
    static class MyParameters implements KDFParameters {
        final String label;
        MyParameters(String label) { this.label = label; }
        @Override public String toString() { return "MyParameters[" + label + "]"; }
    }

    // a KDFSpi that echoes the parameters it was configured with
    static class EchoSpi extends KDFSpi {
        final KDFParameters given;
        EchoSpi(KDFParameters p) throws InvalidAlgorithmParameterException { super(p); given = p; }
        public KDFParameters engineGetParameters() { return given; }
        public SecretKey engineDeriveKey(String alg, AlgorithmParameterSpec spec) {
            return new SecretKeySpec(new byte[]{9}, alg);
        }
        public byte[] engineDeriveData(AlgorithmParameterSpec spec) { return new byte[]{9}; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KDFParameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        // marker interface with no members of its own
        check(c.getDeclaredMethods().length == 0, "no declared methods (marker interface)");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(!AlgorithmParameterSpec.class.isAssignableFrom(c),
                "does not extend AlgorithmParameterSpec");
        // HKDF derivation specs are AlgorithmParameterSpec, not KDFParameters
        Class<?> hkdf = Class.forName("javax.crypto.spec.HKDFParameterSpec");
        check(AlgorithmParameterSpec.class.isAssignableFrom(hkdf) && !c.isAssignableFrom(hkdf),
                "HKDFParameterSpec is a derivation spec, not KDFParameters");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // user implementations are instantiable and type as KDFParameters
        MyParameters p1 = new MyParameters("config-a");
        MyParameters p2 = new MyParameters("config-b");
        KDFParameters asInterface = p1;
        check(asInterface instanceof KDFParameters && p1 == asInterface,
                "custom params usable as KDFParameters");
        check(p1 != p2 && !p1.equals(p2), "distinct configurations stay distinct");

        // null parameters are accepted by KDF.getInstance and observable via getParameters
        KDF kdf = KDF.getInstance("HKDF-SHA256", (KDFParameters) null);
        check(kdf.getParameters() == null, "null KDFParameters -> getParameters null");
        check("HKDF-SHA256".equals(kdf.getAlgorithm()), "configured KEM keeps algorithm");
        byte[] okm = kdf.deriveData(HKDFParameterSpec.ofExtract()
            .addIKM(new byte[]{1}).addSalt(new byte[]{2}).thenExpand(new byte[]{3}, 16));
        check(okm.length == 16, "KDF built with null params derives normally");
        check(kdf.getParameters() == null, "getParameters still null after derivation");

        // non-null unsupported parameters are rejected at configuration time
        try {
            KDF.getInstance("HKDF-SHA256", p1);
            check(false, "HKDF must reject non-null KDFParameters");
        } catch (InvalidAlgorithmParameterException ex) {
            check(true, "unsupported KDFParameters -> InvalidAlgorithmParameterException");
        }

        // parameters flow into a custom KDFSpi through its constructor and come back unchanged
        EchoSpi s1 = new EchoSpi(p2);
        check(s1.engineGetParameters() == p2, "configured instance echoed by engineGetParameters");
        EchoSpi s2 = new EchoSpi(null);
        check(s2.engineGetParameters() == null, "null configuration echoed as null");
        check(KDFSpi.class.isAssignableFrom(EchoSpi.class)
                && s1 instanceof KDFSpi && s2 instanceof KDFSpi,
                "config carriers are KDFSpi-configurable");
        // marker-configured SPI remains fully functional for derivation
        check("AES".equals(s1.engineDeriveKey("AES", null).getAlgorithm())
                && Arrays.equals(s1.engineDeriveData(null), new byte[]{9}),
                "marker-configured spi derives keys and data");

        // derivation specs used by KDF.derive* are never KDFParameters instances
        HKDFParameterSpec spec = HKDFParameterSpec.ofExtract()
            .addIKM(new byte[]{5}).addSalt(new byte[0]).thenExpand(new byte[0], 8);
        check(!(spec instanceof KDFParameters),
                "derivation spec is not KDFParameters (orthogonal concepts)");
        check(KDF.getInstance("HKDF-SHA256").deriveData(spec).length == 8,
                "derivation works without any KDFParameters");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
